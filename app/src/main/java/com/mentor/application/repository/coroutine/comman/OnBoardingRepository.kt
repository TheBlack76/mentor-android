package com.mentor.application.repository.coroutine.comman

import com.mentor.application.repository.models.LoginRequestModel
import com.mentor.application.repository.models.NotificationCountModel
import com.mentor.application.repository.models.OtpRequestModel
import com.mentor.application.repository.networkrequests.ResultWrapper
import com.mentor.application.repository.networkrequests.SafeCallGenerator
import com.mentor.application.repository.models.PojoUserLogin
import com.mentor.application.repository.models.ProfileResponseModel
import com.mentor.application.repository.models.SignupRequestModel
import com.mentor.application.repository.models.SimpleSuccessResponse
import com.mentor.application.repository.networkrequests.Apis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Response
import javax.inject.Inject

/**
 * Internal class for providing data from data sources.
 */
class OnBoardingRepository @Inject constructor(private val webService: Apis) {

    suspend fun signUp(
        mSignupRequestModel: SignupRequestModel,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<PojoUserLogin>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.signUp(mSignupRequestModel)
        }
    }

    suspend fun login(
        mLoginRequestModel:LoginRequestModel,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<PojoUserLogin>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.login(mLoginRequestModel)
        }
    }

    suspend fun socialLogin(
        name:String,email:String,image:String,socialId:String,userType:String,
        deviceToken:String,deviceId:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<PojoUserLogin>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.socialLogin(name,email,image,socialId,userType,deviceToken,deviceId)
        }
    }

    suspend fun registerNumber(
        countryCode:String,number:String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<PojoUserLogin>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.registerNumber(countryCode,number)
        }
    }

    suspend fun verifyOtp(
        mOtpRequestModel: OtpRequestModel,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.verifyOtp(mOtpRequestModel)
        }
    }

    suspend fun resendOtp(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.resendOtp()
        }
    }

    suspend fun logout(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.logout()
        }
    }

    suspend fun deleteAccount(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<SimpleSuccessResponse>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.deleteAccount()
        }
    }

    suspend fun getMessageCount(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<NotificationCountModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getMessageCount()
        }
    }

    suspend fun getProfile(
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<ProfileResponseModel>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.getProfile()
        }
    }
    suspend fun editProfile(
        name:String?,email:String?,image:String?,latitude: Double?,longitude: Double?, location: String?,
        countryCode:String?,number:String?,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ResultWrapper<Response<PojoUserLogin>> {
        return SafeCallGenerator.safeApiCall(dispatcher) {
            webService.editProfile(name,email,image,latitude,longitude,location,countryCode,number)
        }
    }

}