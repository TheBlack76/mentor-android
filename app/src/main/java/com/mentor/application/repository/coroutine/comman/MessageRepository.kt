package com.mentor.application.repository.coroutine.comman

import com.mentor.application.repository.models.JoinSessionResponseModel
import com.mentor.application.repository.models.PojoMessage
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
class MessageRepository @Inject constructor(private val webService: Apis) {

    suspend fun completeBooking(
       bookingId:String,
       resolveStatus:Int,
       other:String?,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.completeBooking(bookingId,resolveStatus,other)
        }
    }

    suspend fun joinSession(
        bookingId:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<JoinSessionResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.joinSession(bookingId)
        }
    }

    suspend fun getChat(
        bookingId:String,
        page:Int,
        limit:Int,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<PojoMessage>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getChat(bookingId,page,limit)
        }
    }

}