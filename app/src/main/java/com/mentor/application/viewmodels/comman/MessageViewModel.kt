package com.mentor.application.viewmodels.comman

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.mentor.application.R
import com.mentor.application.repository.coroutine.comman.MessageRepository
import com.mentor.application.repository.models.JoinSessionResponseModel
import com.mentor.application.repository.models.Message
import com.mentor.application.repository.models.PojoMessage
import com.mentor.application.repository.models.RetrofitErrorMessage
import com.mentor.application.repository.models.SessionData
import com.mentor.application.repository.models.SimpleSuccessResponse
import com.mentor.application.repository.networkrequests.NetworkRequestCallbacks
import com.mentor.application.repository.networkrequests.RetrofitRequest
import com.mentor.application.repository.networkrequests.WebConstants
import com.mentor.application.utils.ApplicationGlobal
import com.mentor.application.utils.Constants
import com.mentor.application.views.comman.adapters.MessagesListAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val repository: MessageRepository,
    application: Application
) : BaseViewModel(application) {

    companion object {
        private const val EVENT_LEAVE_CONVERSATION = "leaveConversation"
        private const val EVENT_SEND_MESSAGE = "sendMessage"
        private const val EVENT_RECEIVE_MESSAGE = "receiveMessage"

        private const val EVENT_PARAM_BOOKING_ID = "bookingId"
        private const val EVENT_PARAM_MESSAGE = "message"
        const val EVENT_PARAM_MESSAGE_TYPE = "type"
        const val EVENT_PARAM_MESSAGE_TYPE_TEXT = "text"
        const val EVENT_PARAM_MESSAGE_TYPE_IMAGE= "image"

    }


    private var mChatSocket: Socket? = null
    private var mSessionData = MutableLiveData<SessionData>()
    private var mNewMessage = MutableLiveData<Message>()
    private var mMessageList = MutableLiveData<List<Message>?>()
    private var isBookingComplete = MutableLiveData<Boolean>()
    var isProgressShow = MutableLiveData<Boolean>()
    private val sdfTime: SimpleDateFormat by lazy {
        SimpleDateFormat(
            Constants.DATE_FORMAT_SERVER,
            Locale.US
        )
    }

    fun saveChatInformation(conversationId: String) {
        ApplicationGlobal.inChatConversationId = conversationId
        setUpChatSocket(conversationId)
    }

    fun eraseChatInformation() {
        disconnectChatSocket()
        ApplicationGlobal.inChatConversationId = null
    }

    private fun setUpChatSocket(conversationId: String) {
        // Get Socket
        val opt = IO.Options()
        opt.query = "token=${mUserPrefsManager.accessToken}&bookingId=${conversationId}"
        mChatSocket = IO.socket("${WebConstants.ACTION_SOCKET_URL}/", opt)

        // Connect Socket
        mChatSocket?.connect()

        // Register for events on Socket
        mChatSocket?.on(Socket.EVENT_CONNECT) {
            Log.e("successMessage", "setUpChatSocket: " + "Connected")
        }

        mChatSocket?.on(Socket.EVENT_DISCONNECT) {
            Log.e("errorMessage", "setUpChatSocket: " + it[0].toString())
        }

        mChatSocket?.on(Socket.EVENT_CONNECT_ERROR) {
            Log.e("errorMessage", "setUpChatSocket: " + it[0].toString())
        }


        // Listen to new messages
        mChatSocket?.on(EVENT_RECEIVE_MESSAGE) {
            if (null != it && it.isNotEmpty()) {
                Log.e("mMessageResponse", "setUpChatSocket: " + it[0].toString())
                val data = Gson().fromJson(it[0].toString(), Message::class.java)
                mNewMessage.postValue(data)

            }
        }
    }

    private fun disconnectChatSocket() {
        mChatSocket?.emit(EVENT_LEAVE_CONVERSATION)
        mChatSocket?.disconnect()
        mChatSocket?.off(Socket.EVENT_CONNECT)
        mChatSocket?.off(Socket.EVENT_DISCONNECT)
        mChatSocket?.off(Socket.EVENT_CONNECT_ERROR)
        mChatSocket?.off(EVENT_RECEIVE_MESSAGE)
    }


    fun sendMessage(
        message: String = ""
    ) {

        // Create message object
        val calender = Calendar.getInstance()
        val messageObject = Message(
            message = message,
            type=EVENT_PARAM_MESSAGE_TYPE_TEXT,
            isSending = true,
            sender = mUserPrefsManager.loginUser?._id.toString(),
            createdAt = sdfTime.format(calender.time)
        )

        // Set value to notify observer
        mNewMessage.value = messageObject

        sendMessageToServer(
            message
        )
    }

    private fun sendMessageToServer(
        message: String = ""
    ) {
        val jsonObject = JSONObject()
        jsonObject.put(EVENT_PARAM_MESSAGE, message)
        jsonObject.put(EVENT_PARAM_MESSAGE_TYPE, EVENT_PARAM_MESSAGE_TYPE_TEXT)
        jsonObject.put(EVENT_PARAM_BOOKING_ID, ApplicationGlobal.inChatConversationId)
        mChatSocket?.emit(EVENT_SEND_MESSAGE, jsonObject)
    }

    fun sendImage(
        image: String
    ) {
        // Create message object
        val calender = Calendar.getInstance()
        val messageObject = Message(
            message = image,
            isSending = true,
            type= EVENT_PARAM_MESSAGE_TYPE_IMAGE,
            sender = mUserPrefsManager.loginUser?._id.toString(),
            createdAt = sdfTime.format(calender.time)
        )
        mNewMessage.value = messageObject
    }

    fun sendImageToServer(
        image: String
    ) {
        val jsonObject = JSONObject()
        jsonObject.put(EVENT_PARAM_BOOKING_ID, ApplicationGlobal.inChatConversationId)
        jsonObject.put(EVENT_PARAM_MESSAGE_TYPE, EVENT_PARAM_MESSAGE_TYPE_IMAGE)
        jsonObject.put(EVENT_PARAM_MESSAGE, image)
        mChatSocket?.emit(EVENT_SEND_MESSAGE, jsonObject)
    }

    fun getChat(bookingId: String, page: Int) {
        if (page == 0) {
            isShowSwipeRefreshLayout.value = true

        }
        apiUtil.fetchResource({
            repository.getChat(
                bookingId,
                page,MessagesListAdapter.LIMIT
            )
        }, object : NetworkRequestCallbacks<PojoMessage> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as PojoMessage

                        mMessageList.value = pojoResponse.data.chatListing

                        if (page==0 && pojoResponse.data.chatListing?.isEmpty() == true) {
                            retrofitErrorDataMessage.postValue(
                                RetrofitErrorMessage(
                                    errorResId = R.string.st_no_data_found
                                )
                            )
                        } else {
                            retrofitErrorDataMessage.postValue(
                                RetrofitErrorMessage(
                                    errorResId = null
                                )
                            )
                        }

                    }

                    else -> {
                        retrofitErrorMessage
                            .postValue(
                                RetrofitErrorMessage(
                                    errorMessage =
                                    RetrofitRequest.getErrorMessage(response.errorBody()!!)
                                )
                            )
                    }
                }
            }

            override fun onError(errorThrow: Int) {
                isShowSwipeRefreshLayout.value = false
                retrofitErrorMessage
                    .postValue(
                        RetrofitErrorMessage(
                            errorResId =
                            errorThrow
                        )
                    )
            }

            override fun onNetworkError(noInternet: Int) {
                isShowSwipeRefreshLayout.value = false
                retrofitErrorMessage
                    .postValue(
                        RetrofitErrorMessage(
                            errorResId =
                            noInternet
                        )
                    )
            }

        })
    }

    fun completeBooking(bookingId: String, resolveStatus: Int,other:String) {
        isProgressShow.value = true
        apiUtil.fetchResource({
            repository.completeBooking(
                bookingId, resolveStatus,other.ifBlank { null }
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isProgressShow.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        isBookingComplete.value=true
                    }

                    else -> {
                        retrofitErrorMessage
                            .postValue(
                                RetrofitErrorMessage(
                                    errorMessage =
                                    RetrofitRequest.getErrorMessage(response.errorBody()!!)
                                )
                            )
                    }
                }
            }

            override fun onError(errorThrow: Int) {
                isProgressShow.value = false
                retrofitErrorMessage
                    .postValue(
                        RetrofitErrorMessage(
                            errorResId =
                            errorThrow
                        )
                    )
            }

            override fun onNetworkError(noInternet: Int) {
                isProgressShow.value = false
                retrofitErrorMessage
                    .postValue(
                        RetrofitErrorMessage(
                            errorResId =
                            noInternet
                        )
                    )
            }

        })
    }

    fun joinSession(bookingId: String) {
        apiUtil.fetchResource({
            repository.joinSession(
                bookingId
            )
        }, object : NetworkRequestCallbacks<JoinSessionResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as JoinSessionResponseModel
                        mSessionData.value = pojoResponse.data
                    }

                    else -> {
                        retrofitErrorMessage
                            .postValue(
                                RetrofitErrorMessage(
                                    errorMessage =
                                    RetrofitRequest.getErrorMessage(response.errorBody()!!)
                                )
                            )
                    }
                }
            }

            override fun onError(errorThrow: Int) {
                isShowLoader.value = false
                retrofitErrorMessage
                    .postValue(
                        RetrofitErrorMessage(
                            errorResId =
                            errorThrow
                        )
                    )
            }

            override fun onNetworkError(noInternet: Int) {
                isShowLoader.value = false
                retrofitErrorMessage
                    .postValue(
                        RetrofitErrorMessage(
                            errorResId =
                            noInternet
                        )
                    )
            }

        })
    }


    fun onGetMessageRealTime() = mNewMessage
    fun onGetMessage() = mMessageList
    fun onBookingComplete() = isBookingComplete
    fun onGetSessionData() = mSessionData


}