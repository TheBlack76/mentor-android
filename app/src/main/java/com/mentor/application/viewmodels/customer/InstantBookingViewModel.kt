package com.mentor.application.viewmodels.customer

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.mentor.application.R
import com.mentor.application.repository.coroutine.customer.BookingRepository
import com.mentor.application.repository.coroutine.customer.InstantBookingRepository

import com.mentor.application.repository.networkrequests.NetworkRequestCallbacks
import com.mentor.application.repository.networkrequests.RetrofitRequest
import com.mentor.application.repository.models.*
import com.mentor.application.repository.networkrequests.WebConstants
import com.mentor.application.utils.ApplicationGlobal
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.MessageViewModel
import com.mentor.application.viewmodels.comman.MessageViewModel.Companion
import com.mentor.application.views.customer.adapters.ReviewsAdapter
import com.mentor.application.views.customer.adapters.UpcomingBookingAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class InstantBookingViewModel @Inject constructor(
    private val repository: InstantBookingRepository, application: Application
) : BaseViewModel(application) {

    companion object {
        private const val EVENT_DID_NOT_RESPOND = "bookingSessionTimeOut"
        private const val EVENT_NOT_RESPOND_BY_PROVIDER = "notRespondByProfessional"
        private const val EVENT_ACCEPTED = "acceptedBooking"
        private const val EVENT_CANCEL_REQUEST = "cancelBooking"
        private const val EVENT_REQUEST_AGAIN = "retryBookingV2"
        private const val EVENT_BOOKING_OFFERS = "bookingOffer"
        private const val EVENT_REJECT_OFFER = "rejectOffer"
        private const val EVENT_ACCEPT_OFFER = "acceptOffer"
        private const val EVENT_PAYMENT_EVENT = "paymentEvent"

        private const val EVENT_PARAM_BOOKING_ID = "bookingId"
        private const val EVENT_PARAM_PROFESSIONAl_ID = "professionalId"
    }

    private var mChatSocket: Socket? = null
    private val mBookingPrice = MutableLiveData<InstantBookingPrice>()
    private val mNoWorkerAvailable = MutableLiveData<Boolean>()
    private val mBookingData = MutableLiveData<CreateBookingData>()
    private val mNotRespond = MutableLiveData<Boolean>()
    private val mNotRespondByProfessional = MutableLiveData<Boolean>()
    private val mBookingOffers = MutableLiveData<BookingOffer>()
    private val mBookingPayment = MutableLiveData<PaymentModel>()
    private val mAcceptBookingData = MutableLiveData<AcceptCallData>()

    fun setUpChatSocket(bookingId: String) {
        // Get Socket
        val opt = IO.Options()
        opt.query =
            "token=${mUserPrefsManager.accessToken}&bookingId=${bookingId}&bookingType=instantBooking"
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
        mChatSocket?.on(EVENT_DID_NOT_RESPOND) {
            if (null != it && it.isNotEmpty()) {
                Log.e("mMessageResponse", "setUpChatSocket: " + it[0].toString())
                mNotRespond.postValue(true)
            }
        }

        // Listen to new messages
        mChatSocket?.on(EVENT_NOT_RESPOND_BY_PROVIDER) {
            if (null != it && it.isNotEmpty()) {
                Log.e("mMessageResponse", "setUpChatSocket: " + it[0].toString())
                mNotRespondByProfessional.postValue(true)
            }
        }

        // Listen to new messages
        mChatSocket?.on(EVENT_BOOKING_OFFERS) {
            if (null != it && it.isNotEmpty()) {
                Log.e("mMessageResponse", "setUpChatSocket: " + it[0].toString())
                val data = Gson().fromJson(it[0].toString(), BookingOffer::class.java)
                mBookingOffers.postValue(data)
            }
        }

        // Listen to new messages
        mChatSocket?.on(EVENT_PAYMENT_EVENT) {
            if (null != it && it.isNotEmpty()) {
                Log.e("mMessageResponse", "setUpChatSocket: " + it[0].toString())
                val data = Gson().fromJson(it[0].toString(), PaymentModel::class.java)
                mBookingPayment.postValue(data)
            }
        }

        // Listen to new messages
        mChatSocket?.on(EVENT_ACCEPTED) {
            if (null != it && it.isNotEmpty()) {
                Log.e("mMessageResponse", "setUpChatSocket: " + it[0].toString())
                val data = Gson().fromJson(it[0].toString(), AcceptCallData::class.java)
                mAcceptBookingData.postValue(data)

            }
        }
    }

    fun disconnectChatSocket() {
        mChatSocket?.disconnect()
        mChatSocket?.off(Socket.EVENT_CONNECT)
        mChatSocket?.off(Socket.EVENT_DISCONNECT)
        mChatSocket?.off(Socket.EVENT_CONNECT_ERROR)
        mChatSocket?.off(EVENT_DID_NOT_RESPOND)
        mChatSocket?.off(EVENT_ACCEPTED)
    }

    fun cancelRequest(bookingId: String) {
        val jsonObject = JSONObject()
        jsonObject.put(EVENT_PARAM_BOOKING_ID, bookingId)
        mChatSocket?.emit(EVENT_CANCEL_REQUEST, jsonObject)
    }

    fun requestAgain(bookingId: String) {
        val jsonObject = JSONObject()
        jsonObject.put(EVENT_PARAM_BOOKING_ID, bookingId)
        mChatSocket?.emit(EVENT_REQUEST_AGAIN, jsonObject)
    }

    fun rejectOffer(bookingId: String,professionalId:String) {
        val jsonObject = JSONObject()
        jsonObject.put(EVENT_PARAM_BOOKING_ID, bookingId)
        jsonObject.put(EVENT_PARAM_PROFESSIONAl_ID, professionalId)
        mChatSocket?.emit(EVENT_REJECT_OFFER, jsonObject)
    }

    fun acceptOffer(bookingId: String,professionalId:String) {
        val jsonObject = JSONObject()
        jsonObject.put(EVENT_PARAM_BOOKING_ID, bookingId)
        jsonObject.put(EVENT_PARAM_PROFESSIONAl_ID, professionalId)
        mChatSocket?.emit(EVENT_ACCEPT_OFFER, jsonObject)
    }

    fun getBookingPrices(
        categoryId: String,
        subCategoryId: String,
    ) {
        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.getBookingPrices(
                categoryId, subCategoryId
            )
        }, object : NetworkRequestCallbacks<InstantBookingPriceResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse = RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as InstantBookingPriceResponseModel
                        mBookingPrice.value = pojoResponse.data

                    }

                    else -> {
                        mNoWorkerAvailable.value = true
//                        retrofitErrorMessage.postValue(
//                            RetrofitErrorMessage(
//                                errorMessage = RetrofitRequest.getErrorMessage(response.errorBody()!!)
//                            )
//                        )
                    }
                }
            }

            override fun onError(errorThrow: Int) {
                mNoWorkerAvailable.value = true
                isShowLoader.value = false
                retrofitErrorMessage.postValue(
                    RetrofitErrorMessage(
                        errorResId = errorThrow
                    )
                )
            }

            override fun onNetworkError(noInternet: Int) {
                isShowLoader.value = false
                retrofitErrorMessage.postValue(
                    RetrofitErrorMessage(
                        errorResId = noInternet
                    )
                )
            }
        })
    }

    fun createInstantBooking(
        professionId: String,
        subCategoryId: String,
        professionalId: String,
        amount: String,
        slot: String,
        describeIssue: String
    ) {
        when {
            amount.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_AMOUNT
            amount.toDouble()!!<=0 -> errorHandler.value = ErrorHandler.INVALID_AMOUNT
            describeIssue.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_ISSUE
            else -> {

                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.createInstantBooking(
                        professionId, subCategoryId, professionalId, amount, slot,
                        describeIssue
                    )
                }, object : NetworkRequestCallbacks<CreateBookingResponseModel> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as CreateBookingResponseModel
                                mBookingData.value = pojoResponse.data

                            }

                            else -> {
                                retrofitErrorMessage.postValue(
                                    RetrofitErrorMessage(
                                        errorMessage = RetrofitRequest.getErrorMessage(response.errorBody()!!)
                                    )
                                )
                            }
                        }
                    }

                    override fun onError(errorThrow: Int) {
                        isShowLoader.value = false
                        retrofitErrorMessage.postValue(
                            RetrofitErrorMessage(
                                errorResId = errorThrow
                            )
                        )
                    }

                    override fun onNetworkError(noInternet: Int) {
                        isShowLoader.value = false
                        retrofitErrorMessage.postValue(
                            RetrofitErrorMessage(
                                errorResId = noInternet
                            )
                        )
                    }

                })
            }
        }
    }

    fun onGetBookingPrice() = mBookingPrice
    fun onGetBookingData() = mBookingData
    fun onGetNotRespond() = mNotRespond
    fun onGetNotRespondByProfessional() = mNotRespondByProfessional
    fun onGetAcceptResponse() = mAcceptBookingData
    fun onGetNoWorkerAvailable() = mNoWorkerAvailable
    fun onGetBookingOffers() = mBookingOffers
    fun onGetBookingPayment() = mBookingPayment
}