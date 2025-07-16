package com.mentor.application.viewmodels.comman

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mentor.application.R
import com.mentor.application.repository.networkrequests.ApiUtil
import com.mentor.application.repository.models.RetrofitErrorMessage
import com.mentor.application.repository.preferences.UserPrefsManager
import javax.inject.Inject

/**
 * Created by Mukesh on 13/02/2018.
 */

abstract class BaseViewModel(
    application: Application
) : AndroidViewModel(application) {

    @Inject
    lateinit var mUserPrefsManager: UserPrefsManager

    @Inject
    lateinit var apiUtil: ApiUtil

    val isShowLoader = MutableLiveData<Boolean>()
    protected val isShowNoDataText = MutableLiveData<Boolean>()
    protected val isShowSwipeRefreshLayout = MutableLiveData<Boolean>()
    protected val isSessionExpired = MutableLiveData<Boolean>()
    protected val retrofitErrorDataMessage = MutableLiveData<RetrofitErrorMessage>()
    protected val retrofitErrorMessage = MutableLiveData<RetrofitErrorMessage>()
    protected val successMessage = MutableLiveData<String>()
    protected val errorHandler = MutableLiveData<ErrorHandler>()

    fun isShowLoader(): LiveData<Boolean> = isShowLoader

    fun isShowNoDataText(): LiveData<Boolean> = isShowNoDataText

    fun isSessionExpired(): LiveData<Boolean> = isSessionExpired

    fun isShowSwipeRefreshLayout(): LiveData<Boolean> = isShowSwipeRefreshLayout

    fun getRetrofitErrorDataMessage(): LiveData<RetrofitErrorMessage> = retrofitErrorDataMessage

    fun getRetrofitErrorMessage(): LiveData<RetrofitErrorMessage> = retrofitErrorMessage

    fun getErrorHandler(): LiveData<ErrorHandler> = errorHandler

    fun getSuccessMessage(): LiveData<String> = successMessage

    enum class ErrorHandler(@StringRes private val resourceId: Int) : ErrorEvent {
        EMPTY_NAME(R.string.empty_first_name),
        EMPTY_PROFESSION(R.string.empty_profession),
        EMPTY_SUB_PROFESSION(R.string.empty_sub_profession),
        EMPTY_EXPERIENCE(R.string.empty_experience),
        EMPTY_LOCATION(R.string.empty_location),
        EMPTY_RATE(R.string.empty_rate),
        VALID_RATE(R.string.valid_rate),
        EMPTY_QUESTIONARY(R.string.empty_questionary),
        EMPTY_DATE(R.string.empty_date),
        EMPTY_BIO(R.string.empty_bio),
        EMPTY_CERTIFICATES(R.string.empty_certificates),
        EMPTY_OTP(R.string.empty_otp),
        INCORRECT_OTP(R.string.st_incomplete_otp),
        INVALID_EMAIL(R.string.invalid_email),
        EMPTY_NUMBER(R.string.empty_number),
        EMPTY_IMAGE(R.string.empty_image),
        EMPTY_START_DATE(R.string.empty_start_date),
        EMPTY_REPEAT_TILL_DATE(R.string.empty_till_date),
        EMPTY_SLOTS(R.string.empty_slots),
        EMPTY_MESSAGE(R.string.st_empty_message),
        EMPTY_ISSUE(R.string.st_empty_issue_describe),
        EMPTY_AMOUNT(R.string.st_empty_fare),
        INVALID_AMOUNT(R.string.st_invalid_fare),
        EMPTY_EMAIL(R.string.empty_email);



        override fun getErrorResource() = resourceId
    }

    private fun onError(message: String) {
        successMessage.value = message

    }

    interface ErrorEvent {
        @StringRes
        fun getErrorResource(): Int
    }
}