package com.mentor.application.repository.coroutine.customer

import com.mentor.application.repository.models.BookingInfoResponseModel
import com.mentor.application.repository.models.CustomerBookingResponseModel
import com.mentor.application.repository.networkrequests.ResultWrapper
import com.mentor.application.repository.networkrequests.SafeCallGenerator
import com.mentor.application.repository.models.ReviewsResponseModel
import com.mentor.application.repository.models.SimpleSuccessResponse
import com.mentor.application.repository.networkrequests.Apis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Response
import javax.inject.Inject

/**
 * Internal class for providing data from data sources.
 */
class BookingRepository @Inject constructor(private val webService: Apis) {

    suspend fun getBookings(
        type:String, page: Int,
        limit: Int?,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<CustomerBookingResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getBookings(type, page, limit)
        }
    }

    suspend fun getBookingInfo(
        bookingId:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<BookingInfoResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getBookingInfo(bookingId)
        }
    }

    suspend fun cancelBookingByVendor(
        bookingId:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.cancelBookingByVendor(bookingId)
        }
    }

    suspend fun getReviews(
        professionalId:String,
        page:Int,
        limit:Int,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<ReviewsResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getReviews(professionalId,page,limit)
        }
    }

    suspend fun addReview(
        bookingId:String,
        professionalId:String,
        message:String,
        star:Float,
        image:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.addReview(bookingId,professionalId,message,star,image)
        }
    }

    suspend fun cancelBooking(
        bookingId:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.cancelBooking(bookingId)
        }
    }



}