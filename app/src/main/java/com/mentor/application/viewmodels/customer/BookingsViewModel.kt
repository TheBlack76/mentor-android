package com.mentor.application.viewmodels.customer

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mentor.application.R
import com.mentor.application.repository.coroutine.customer.BookingRepository

import com.mentor.application.repository.networkrequests.NetworkRequestCallbacks
import com.mentor.application.repository.networkrequests.RetrofitRequest
import com.mentor.application.repository.models.*
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.customer.adapters.ReviewsAdapter
import com.mentor.application.views.customer.adapters.UpcomingBookingAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val repository: BookingRepository, application: Application
) : BaseViewModel(application) {


    private val mBookings = MutableLiveData<List<Booking>?>()
    private val mBookingInfo = MutableLiveData<BookingInfo>()
    private val mReviewsData = MutableLiveData<List<ReviewData>?>()
    private val isBookingCancel = MutableLiveData<Boolean>()
    private val isReviewAdded = MutableLiveData<Boolean>()


    fun getBookings(
        type: String, page: Int,showLoader: Boolean=true
    ) {
        isShowSwipeRefreshLayout.value = showLoader
        apiUtil.fetchResource({
            repository.getBookings(
                type, page, UpcomingBookingAdapter.LIMIT
            )
        }, object : NetworkRequestCallbacks<CustomerBookingResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse = RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as CustomerBookingResponseModel
                        mBookings.value = pojoResponse.data.bookings

                        if (page==0 && pojoResponse.data.bookings?.isEmpty() == true) {
                            retrofitErrorDataMessage.postValue(
                                RetrofitErrorMessage(
                                    errorResId = R.string.st_no_booking_found
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
                        retrofitErrorMessage.postValue(
                            RetrofitErrorMessage(
                                errorMessage = RetrofitRequest.getErrorMessage(response.errorBody()!!)
                            )
                        )
                    }
                }
            }

            override fun onError(errorThrow: Int) {
                isShowSwipeRefreshLayout.value = false
                retrofitErrorMessage.postValue(
                    RetrofitErrorMessage(
                        errorResId = errorThrow
                    )
                )
            }

            override fun onNetworkError(noInternet: Int) {
                isShowSwipeRefreshLayout.value = false
                retrofitErrorMessage.postValue(
                    RetrofitErrorMessage(
                        errorResId = noInternet
                    )
                )
            }

        })
    }

    fun getBookingInfo(
        bookingId: String,showLoader:Boolean=true
    ) {
        isShowLoader.value = showLoader
        apiUtil.fetchResource({
            repository.getBookingInfo(
                bookingId
            )
        }, object : NetworkRequestCallbacks<BookingInfoResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse = RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as BookingInfoResponseModel
                        mBookingInfo.value = pojoResponse.data
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

    fun cancelBookingByVendor(
        bookingId: String,showLoader:Boolean=true
    ) {
        isShowLoader.value = showLoader
        apiUtil.fetchResource({
            repository.cancelBookingByVendor(
                bookingId
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse = RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        isBookingCancel.value = true
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

    fun getReviews(
        professionalId: String, page: Int,showLoader: Boolean=true
    ) {
        isShowSwipeRefreshLayout.value = showLoader
        apiUtil.fetchResource({
            repository.getReviews(
                professionalId, page, ReviewsAdapter.LIMIT
            )
        }, object : NetworkRequestCallbacks<ReviewsResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse = RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as ReviewsResponseModel
                        mReviewsData.value = pojoResponse.data.reviews

                        if (page==0 && pojoResponse.data.reviews?.isEmpty() == true) {
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
                        retrofitErrorMessage.postValue(
                            RetrofitErrorMessage(
                                errorMessage = RetrofitRequest.getErrorMessage(response.errorBody()!!)
                            )
                        )
                    }
                }
            }

            override fun onError(errorThrow: Int) {
                isShowSwipeRefreshLayout.value = false
                retrofitErrorMessage.postValue(
                    RetrofitErrorMessage(
                        errorResId = errorThrow
                    )
                )
            }

            override fun onNetworkError(noInternet: Int) {
                isShowSwipeRefreshLayout.value = false
                retrofitErrorMessage.postValue(
                    RetrofitErrorMessage(
                        errorResId = noInternet
                    )
                )
            }

        })
    }

    fun addReview(
        bookingId:String,
        professionalId: String,
        message: String,
        star: Float,
        image: String,
    ) {

        when {
            message.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_MESSAGE
            else -> {
                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.addReview(
                        bookingId, professionalId, message, star, image
                    )
                }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as SimpleSuccessResponse
                                isReviewAdded.value=true
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

    fun cancelBooking(
        bookingId: String,
    ) {
        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.cancelBooking(
                bookingId
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse = RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        isBookingCancel.value = true
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


    fun onGetBookings() = mBookings
    fun onGetBookingInfo() = mBookingInfo
    fun onBookingCancel() = isBookingCancel
    fun onReviewAdded() = isReviewAdded
    fun onGetReviews() = mReviewsData
}