package com.mentor.application.viewmodels.comman

import android.app.Application
import android.content.ContentValues
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mentor.application.repository.coroutine.comman.OnBoardingRepository

import com.mentor.application.repository.networkrequests.NetworkRequestCallbacks
import com.mentor.application.repository.networkrequests.RetrofitRequest
import com.mentor.application.repository.models.*
import com.mentor.application.utils.AmazonS3
import com.mentor.application.utils.AmazonS3.Companion.S3_CUSTOMER_PHOTOS
import com.mentor.application.utils.AmazonS3.Companion.S3_PROFESSIONAL_PHOTOS
import com.mentor.application.utils.AmazonS3.Companion.SERVER_CUSTOMER_PHOTOS
import com.mentor.application.utils.AmazonS3.Companion.SERVER_PROFESSIONAL_PHOTOS
import com.mentor.application.utils.ApplicationGlobal
import com.mentor.application.utils.GeneralFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val repository: OnBoardingRepository,
    application: Application
) : BaseViewModel(application) {

    private val mRegisterSuccess = MutableLiveData<Boolean>()
    private val mSocialLoginSuccess = MutableLiveData<Boolean>()
    private val isLogout = MutableLiveData<Boolean>()
    private val isOtpResend = MutableLiveData<Boolean>()
    private val isProfileUpdate = MutableLiveData<Boolean>()
    private val mNotificationCount = MutableLiveData<Int>()
    private var mFcmToken = ""

    init {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            mFcmToken = task.result
            Log.e("FCMToken", "fetchFcmToken: " + mFcmToken)

        })
    }

    fun signup(
        name: String,
        email: String,
        countryCode: String,
        phoneNumber: String,
        image: String
    ) {
        when {
            name.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_NAME
            email.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_EMAIL
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorHandler.value =
                ErrorHandler.INVALID_EMAIL

            phoneNumber.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_NUMBER
//            image.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_IMAGE

            else -> {

                // Upload image to S3
                if (image.isNotBlank() && GeneralFunctions.isRemoteImage(File(image).absolutePath)) {
                    AmazonS3(getApplication()).uploadFileToS3(
                        File(image),
                        if (ApplicationGlobal.mUserType == ApplicationGlobal.CUSTOMER)
                            S3_CUSTOMER_PHOTOS
                        else S3_PROFESSIONAL_PHOTOS

                    ) { isSuccess ->
                        if (isSuccess) {
                            // File uploaded successfully
                        } else {

                        }
                    }
                }

                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.signUp(
                        SignupRequestModel(
                            countryCode = countryCode,
                            deviceId = ApplicationGlobal.deviceUniqueId,
                            deviceToken = mFcmToken,
                            email = email,
                            fullName = name,
                            image = if (ApplicationGlobal.mUserType == ApplicationGlobal.CUSTOMER)
                                SERVER_CUSTOMER_PHOTOS + File(image).name
                            else SERVER_PROFESSIONAL_PHOTOS + File(image).name,
                            mobileNumber = phoneNumber,
                            userType = ApplicationGlobal.mUserType
                        )
                    )
                }, object : NetworkRequestCallbacks<PojoUserLogin> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as PojoUserLogin
                                pojoResponse.data.let {
                                    mUserPrefsManager.saveUserSession(
                                        it
                                    )
                                }
                                mRegisterSuccess.value = true
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

    fun login(
        countryCode: String,
        phoneNumber: String
    ) {
        when {
            phoneNumber.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_NUMBER
            else -> {
                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.login(
                        LoginRequestModel(
                            countryCode = countryCode,
                            mobileNumber = phoneNumber,
                            deviceId = ApplicationGlobal.deviceUniqueId,
                            deviceToken = mFcmToken,
                            userType = ApplicationGlobal.mUserType

                        )
                    )
                }, object : NetworkRequestCallbacks<PojoUserLogin> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as PojoUserLogin
                                pojoResponse.data.let {
                                    // Save user session
                                    mUserPrefsManager.saveUserSession(
                                        it
                                    )
                                }

                                mRegisterSuccess.value = true
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

    fun registerNumber(
        countryCode: String,
        phoneNumber: String
    ) {
        when {
            phoneNumber.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_NUMBER
            else -> {
                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.registerNumber(
                        countryCode, phoneNumber
                    )
                }, object : NetworkRequestCallbacks<PojoUserLogin> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as PojoUserLogin
                                mUserPrefsManager.saveUserSession(pojoResponse.data)
                                mRegisterSuccess.value = true
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

    fun socialLogin(
        name: String,
        email: String,
        image: String,
        socialId: String,
    ) {

        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.socialLogin(
                name,
                email,
                image,
                socialId,
                ApplicationGlobal.mUserType,
                mFcmToken,
                ApplicationGlobal.deviceUniqueId
            )
        }, object : NetworkRequestCallbacks<PojoUserLogin> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as PojoUserLogin
                        pojoResponse.data.let {
                            // Save user session
                            mUserPrefsManager.saveUserSession(
                                it
                            )

                            if (pojoResponse.data.userData.isVerified) {
                                mUserPrefsManager.setLogin(true)
                            }

                        }

                        mSocialLoginSuccess.value = true
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


    fun verifyOtp(
        otp: String,
        phoneNumber: String?,
        countryCode: String?
    ) {
        when {
            otp.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_OTP
            otp.length<4 -> errorHandler.value = ErrorHandler.INCORRECT_OTP
            else -> {
                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.verifyOtp(
                        OtpRequestModel(
                            otp,
                            phoneNumber,
                            countryCode
                        )

                    )
                }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as SimpleSuccessResponse

                                mUserPrefsManager.setLogin(true)
                                mRegisterSuccess.value = true
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

    fun resendOtp(
    ) {

        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.resendOtp(
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        successMessage.value = pojoResponse.message
                        isOtpResend.value = true
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

    fun getProfile(
    ) {
        isShowSwipeRefreshLayout.value = true
        apiUtil.fetchResource({
            repository.getProfile(
            )
        }, object : NetworkRequestCallbacks<ProfileResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as ProfileResponseModel

                        // Update user data
                        mUserPrefsManager.updateUserData(pojoResponse.data)

                        isProfileUpdate.value = true

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

    fun logout(
    ) {

        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.logout(
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        mUserPrefsManager.clearUserPrefs()
                        isLogout.value = true
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

    fun deleteAccount(
    ) {
        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.deleteAccount(
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        mUserPrefsManager.clearUserPrefs()
                        isLogout.value = true
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

    fun getMessageCount(
    ) {
        apiUtil.fetchResource({
            repository.getMessageCount(
            )
        }, object : NetworkRequestCallbacks<NotificationCountModel> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as NotificationCountModel
                        mNotificationCount.value=pojoResponse.data.count
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


    fun editProfile(
        name: String? = null,
        email: String? = null,
        image: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        location: String? = null,
        countryCode: String? = null,
        number: String? = null
    ) {
        when {
            name?.isBlank() == true -> errorHandler.value = ErrorHandler.EMPTY_NAME
            email?.isBlank() == true -> errorHandler.value = ErrorHandler.EMPTY_EMAIL
            email?.let {
                !Patterns.EMAIL_ADDRESS.matcher(it).matches()
            } == true -> errorHandler.value =
                ErrorHandler.INVALID_EMAIL

            image?.isBlank() == true -> errorHandler.value = ErrorHandler.EMPTY_IMAGE

            else -> {

                // Upload image to S3
                val mImage: String?

                if (image?.let { GeneralFunctions.isRemoteImage(it) } == true) {
                    AmazonS3(getApplication()).uploadFileToS3(
                        File(image),
                        if (ApplicationGlobal.mUserType == ApplicationGlobal.CUSTOMER)
                            S3_CUSTOMER_PHOTOS
                        else S3_PROFESSIONAL_PHOTOS

                    ) { isSuccess ->
                        if (isSuccess) {
                            // File uploaded successfully
                        } else {

                        }
                    }

                    mImage = if (ApplicationGlobal.mUserType == ApplicationGlobal.CUSTOMER)
                        SERVER_CUSTOMER_PHOTOS + File(image).name
                    else SERVER_PROFESSIONAL_PHOTOS + File(image).name

                } else {
                    mImage = image
                }

                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.editProfile(
                        name, email, mImage, latitude, longitude, location, countryCode, number
                    )
                }, object : NetworkRequestCallbacks<PojoUserLogin> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as PojoUserLogin
                                isProfileUpdate.value = true
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

    fun onSignUpSuccess() = mRegisterSuccess
    fun mSocialLoginSuccess() = mSocialLoginSuccess
    fun onLogout() = isLogout
    fun onOtpResend() = isOtpResend
    fun onProfileUpdate() = isProfileUpdate
    fun onGetNotificationCount() = mNotificationCount

}