package com.mentor.application.viewmodels.vendor

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mentor.application.R
import com.mentor.application.repository.coroutine.vendor.BookingRepository

import com.mentor.application.repository.networkrequests.NetworkRequestCallbacks
import com.mentor.application.repository.networkrequests.RetrofitRequest
import com.mentor.application.repository.models.*
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.customer.adapters.UpcomingBookingAdapter
import com.mentor.application.views.vendor.fragments.NewRequestFragment
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.NEW_BOOKINGS
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val repository: BookingRepository,
    application: Application
) : BaseViewModel(application) {


    private val mBookings = MutableLiveData<List<BookingRequest>?>()
    private val isBookingResponse = MutableLiveData<Boolean>()
    private val acceptBookingData = MutableLiveData<Pair<AcceptCallData, String>>()
    private val bookingResponseError = MutableLiveData<Boolean>()

    fun getBookings(
        type: String, page: Int, showLoader: Boolean = true
    ) {
        isShowSwipeRefreshLayout.value = showLoader
        apiUtil.fetchResource({
            repository.getProfessionalBookings(
                type, page, UpcomingBookingAdapter.LIMIT
            )
        }, object : NetworkRequestCallbacks<BookingRequestResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as BookingRequestResponseModel
                        mBookings.value = pojoResponse.data.bookings

                        if (page == 0 && pojoResponse.data.bookings?.isEmpty() == true) {
                            retrofitErrorDataMessage.postValue(
                                RetrofitErrorMessage(
                                    errorResId =
                                    when (type) {
                                        NEW_BOOKINGS -> R.string.st_no_new_booking_found
                                        NewRequestFragment.UPCOMING_BOOKINGS -> R.string.st_no_upcoming_booking_found
                                        else -> R.string.st_no_past_booking_found

                                    }
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

    fun bookingResponse(
        bookingId: String,
        type: String,
        amount: String?,
    ) {
        when {
           amount!=null && amount.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_AMOUNT
            amount!=null && amount.toDouble()!! <= 0 -> errorHandler.value = ErrorHandler.INVALID_AMOUNT
            else -> {
                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.bookingResponse(
                        bookingId, type, amount
                    )
                }, object : NetworkRequestCallbacks<AcceptCallResponseModel> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as AcceptCallResponseModel
                                acceptBookingData.value = Pair(pojoResponse.data, type)
                            }

                            else -> {
                                bookingResponseError.value = true
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


    fun onGetBookings() = mBookings
    fun onGetBookingResponse() = isBookingResponse
    fun onGetAcceptBookingData() = acceptBookingData
    fun onBookingResponseError() = bookingResponseError
}