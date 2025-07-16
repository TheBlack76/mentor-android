package com.mentor.application.viewmodels.customer

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mentor.application.R
import com.mentor.application.repository.coroutine.customer.ServicesRepository

import com.mentor.application.repository.networkrequests.NetworkRequestCallbacks
import com.mentor.application.repository.networkrequests.RetrofitRequest
import com.mentor.application.repository.models.*
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.customer.adapters.AvailableWorkersAdapter
import com.mentor.application.views.customer.adapters.NotificationAdapter.Companion.LIMIT
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repository: ServicesRepository,
    application: Application
) : BaseViewModel(application) {


    private val mGetServices = MutableLiveData<ProfessionsData>()
    private val mGetProfessional = MutableLiveData<List<User>?>()
    private val mBookingSlots = MutableLiveData<SlotsData>()
    private val mNotification = MutableLiveData<List<NotificationListing>?>()
    private val mBookingCreated = MutableLiveData<CreateBookingResponseModel>()
    private val mBookingConfirm = MutableLiveData<Boolean>()
    private val mQuestions = MutableLiveData<List<String>>()


    fun getServices(
    ) {
        isShowSwipeRefreshLayout.value = true
        apiUtil.fetchResource({
            repository.getServices(
            )
        }, object : NetworkRequestCallbacks<ProfessionsResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as ProfessionsResponseModel
                        mGetServices.value = pojoResponse.data
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

    fun getProfessional(
        professionId: String, subProfessionId: String, page: Int,
        minPrice: Float?, maxPrice: Float?, distance: Float?, rating: Float?
    ) {
        isShowSwipeRefreshLayout.value = true
        apiUtil.fetchResource({
            repository.getProfessional(
                professionId, subProfessionId, page, AvailableWorkersAdapter.LIMIT,
                minPrice, maxPrice, distance, rating
            )
        }, object : NetworkRequestCallbacks<ProfessionalsResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as ProfessionalsResponseModel
                        mGetProfessional.value = pojoResponse.data.professionalList

                        if (page == 0 && pojoResponse.data.professionalList!!.isEmpty()) {
                            retrofitErrorDataMessage.postValue(
                                RetrofitErrorMessage(
                                    errorResId = R.string.st_no_worker_available
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

    fun getSlots(
        professionalId: String,
        bookingId: String,
        date: String
    ) {

        when {
            date.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_DATE
            else -> {
                isShowSwipeRefreshLayout.value = true
                apiUtil.fetchResource({
                    repository.getSlots(
                        professionalId,
                        bookingId.ifBlank { null },
                        date
                    )
                }, object : NetworkRequestCallbacks<BookingSlotsResponseModel> {
                    override fun onSuccess(response: Response<*>) {
                        isShowSwipeRefreshLayout.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as BookingSlotsResponseModel
                                mBookingSlots.value = pojoResponse.data
                            }

                            else -> {
                                mBookingSlots.value=SlotsData()
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
        }
    }


    fun getQuestions(
        professionId:String
    ) {
        isShowSwipeRefreshLayout.value = true
        apiUtil.fetchResource({
            repository.getQuestions(
                professionId
            )
        }, object : NetworkRequestCallbacks<GetQuestionResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as GetQuestionResponseModel
                        mQuestions.value = pojoResponse.data
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

    fun createBooking(
        date: String,
        startTime: String,
        endDate: String,
        professionalId: String,
        professionId: String,
        subProfessionId: String,
        answerList: List<Questions>,
        ) {
        when {
            answerList.any { it.answer.isBlank() } -> errorHandler.value = ErrorHandler.EMPTY_QUESTIONARY
            else -> {
                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.createBooking(
                        CreateBookingRequestModel(
                            date = date,
                            endTime = endDate,
                            generalQuestions = answerList,
                            professionalId = professionalId,
                            professionId = professionId,
                            subProfessionId = subProfessionId,
                            startTime = startTime,
                        )

                    )
                }, object : NetworkRequestCallbacks<CreateBookingResponseModel> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as CreateBookingResponseModel
                                mBookingCreated.value = pojoResponse
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
        }
    }

    fun confirmBooking(
        bookingId: String
    ) {
        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.confirmBooking(
                bookingId,
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        mBookingConfirm.value = true
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

    fun reScheduleBooking(
        bookingId: String,
        startTime: String,
        endTime: String,
        date: String,
    ) {
        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.reScheduleBooking(
                bookingId, startTime, endTime, date
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())

                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        mBookingConfirm.value = true
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

    fun getNotification(
        page: Int,
    ) {
        if (page==0){
            isShowSwipeRefreshLayout.value = true
        }
        apiUtil.fetchResource({
            repository.getNotification(
                page,LIMIT
            )
        }, object : NetworkRequestCallbacks<NotificationResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as NotificationResponseModel
                        mNotification.value = pojoResponse.data.notificationListing

                        if (page==0 && pojoResponse.data.notificationListing?.isEmpty() == true) {
                            retrofitErrorDataMessage.postValue(
                                RetrofitErrorMessage(
                                    errorResId = R.string.st_no_notification_found
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

    fun onGetServices() = mGetServices
    fun onGetProfessional() = mGetProfessional
    fun onGetSlots() = mBookingSlots
    fun onBookingConfirm() = mBookingConfirm
    fun onBookingCreated() = mBookingCreated
    fun onGetNotifications() = mNotification
    fun onGetQuestions() = mQuestions


}