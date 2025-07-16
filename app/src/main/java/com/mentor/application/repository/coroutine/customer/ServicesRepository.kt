package com.mentor.application.repository.coroutine.customer

import com.mentor.application.repository.models.BookingSlotsResponseModel
import com.mentor.application.repository.models.CreateBookingRequestModel
import com.mentor.application.repository.models.CreateBookingResponseModel
import com.mentor.application.repository.models.GetQuestionResponseModel
import com.mentor.application.repository.models.NotificationResponseModel
import com.mentor.application.repository.networkrequests.ResultWrapper
import com.mentor.application.repository.networkrequests.SafeCallGenerator
import com.mentor.application.repository.models.ProfessionalsResponseModel
import com.mentor.application.repository.models.ProfessionsResponseModel
import com.mentor.application.repository.models.SimpleSuccessResponse
import com.mentor.application.repository.networkrequests.Apis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Response
import javax.inject.Inject

/**
 * Internal class for providing data from data sources.
 */
class ServicesRepository @Inject constructor(private val webService: Apis) {

    suspend fun getServices(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<ProfessionsResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getServices()
        }
    }

    suspend fun getSlots(
        professionalId:String,
        bookingId: String?,
        date:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<BookingSlotsResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getSlots(professionalId,bookingId,date)
        }
    }

    suspend fun getQuestions(
        professionId:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<GetQuestionResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getQuestions(professionId)
        }
    }

    suspend fun createBooking(
        mCreateBookingRequestModel: CreateBookingRequestModel,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<CreateBookingResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.createBooking(mCreateBookingRequestModel)
        }
    }

    suspend fun confirmBooking(
        bookingId: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.confirmBooking(bookingId)
        }
    }

    suspend fun reScheduleBooking(
        bookingId: String,
        startTime:String,
        endTime:String,
        date:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.reScheduleBooking(bookingId,startTime,endTime,date)
        }
    }

    suspend fun getProfessional(
        professionId: String, subProfessionId: String, page: Int,
        limit: Int?,minPrice:Float?,maxPrice:Float?,distance:Float?,rating:Float?,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<ProfessionalsResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getProfessional(professionId, subProfessionId, page, limit,
                minPrice,maxPrice,distance,rating)
        }
    }

    suspend fun getNotification(
        page:Int,limit: Int,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<NotificationResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getNotification(page,limit)
        }
    }


}