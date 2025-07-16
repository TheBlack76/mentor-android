package com.mentor.application.repository.coroutine.vendor

import com.mentor.application.repository.models.AcceptCallResponseModel
import com.mentor.application.repository.models.BookingRequestResponseModel
import com.mentor.application.repository.networkrequests.ResultWrapper
import com.mentor.application.repository.networkrequests.SafeCallGenerator
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

    suspend fun getProfessionalBookings(
        type:String, page: Int,
        limit: Int?,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<BookingRequestResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getProfessionalBookings(type, page, limit)
        }
    }

    suspend fun bookingResponse(
        bookingId:String,
        type:String,
        amount:String?,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<AcceptCallResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.bookingResponse(bookingId,type,amount)
        }
    }


}