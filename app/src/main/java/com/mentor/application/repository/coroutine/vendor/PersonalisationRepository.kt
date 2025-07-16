package com.mentor.application.repository.coroutine.vendor

import com.mentor.application.repository.models.AccountDetailResponseModel
import com.mentor.application.repository.models.CreateAvailabilityRequestModel
import com.mentor.application.repository.models.EnterProfessionalDetailRequestModel
import com.mentor.application.repository.networkrequests.ResultWrapper
import com.mentor.application.repository.networkrequests.SafeCallGenerator
import com.mentor.application.repository.models.ProfessionalSlotsResponseModel
import com.mentor.application.repository.models.ProfessionsResponseModel
import com.mentor.application.repository.models.SimpleSuccessResponse
import com.mentor.application.repository.models.UpdateAvailabilitySlotModel
import com.mentor.application.repository.networkrequests.Apis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Response
import javax.inject.Inject

/**
 * Internal class for providing data from data sources.
 */
class PersonalisationRepository @Inject constructor(private val webService: Apis) {

    suspend fun getProfessions(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<ProfessionsResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getProfessions()
        }
    }


    suspend fun addPersonalisation(
        mEnterProfessionalDetailRequestModel: EnterProfessionalDetailRequestModel,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.addPersonalisation(mEnterProfessionalDetailRequestModel)
        }
    }

    suspend fun createAvailability(
        mCreateAvailabilityRequestModel: CreateAvailabilityRequestModel,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.createAvailability(mCreateAvailabilityRequestModel)
        }
    }

    suspend fun updateAvailabilitySlot(
        mUpdateAvailabilitySlotModel: UpdateAvailabilitySlotModel,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.updateAvailabilitySlot(mUpdateAvailabilitySlotModel)
        }
    }

    suspend fun addAccountDetail(
        token:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.addAccountDetail(token)
        }
    }

    suspend fun getAccountDetail(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<AccountDetailResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getAccountDetail()
        }
    }

    suspend fun getAvailability(
        date:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<ProfessionalSlotsResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getAvailability(date)
        }
    }

    suspend fun contactUs(
        name:String,email:String,comment:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.contactUs(name, email, comment)
        }
    }

}