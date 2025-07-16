package com.mentor.application.repository.coroutine.customer

import com.mentor.application.repository.models.BookingInfoResponseModel
import com.mentor.application.repository.models.CreateBookingResponseModel
import com.mentor.application.repository.models.CustomerBookingResponseModel
import com.mentor.application.repository.models.InstantBookingPriceResponseModel
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
class InstantBookingRepository @Inject constructor(private val webService: Apis) {

    suspend fun getBookingPrices(
        categoryId:String,
        subCategoryId:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<InstantBookingPriceResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getBookingPrices(categoryId, subCategoryId)
        }
    }

    suspend fun createInstantBooking(
        professionId:String,
        subCategoryId:String,
        professionalId:String,
        amount:String,
        slot:String,
        describeIssue:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<CreateBookingResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.createInstantBooking(professionId,subCategoryId,professionalId.ifBlank { null },amount,slot,describeIssue)
        }
    }


}