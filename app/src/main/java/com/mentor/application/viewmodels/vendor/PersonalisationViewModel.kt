package com.mentor.application.viewmodels.vendor

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.mentor.application.repository.coroutine.vendor.PersonalisationRepository

import com.mentor.application.repository.networkrequests.NetworkRequestCallbacks
import com.mentor.application.repository.networkrequests.RetrofitRequest
import com.mentor.application.repository.models.*
import com.mentor.application.utils.AmazonS3
import com.mentor.application.utils.AmazonS3.Companion.S3_PROFESSIONAL_CERTIFICATE
import com.mentor.application.utils.AmazonS3.Companion.S3_PROFESSIONAL_PAST_WORK
import com.mentor.application.utils.AmazonS3.Companion.SERVER_PROFESSIONAL_CERTIFICATE
import com.mentor.application.utils.AmazonS3.Companion.SERVER_PROFESSIONAL_PAST_WORK
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PersonalisationViewModel @Inject constructor(
    private val repository: PersonalisationRepository,
    application: Application
) : BaseViewModel(application) {


    private val mProfessionalData = MutableLiveData<ProfessionsData>()
    private val isDetailedSubmitted = MutableLiveData<Boolean>()
    private val mAvailabilityData = MutableLiveData<AvailabilityData>()
    private val mAccountDetail = MutableLiveData<AccountData>()


    fun getProfessions(
    ) {
        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.getProfessions(
            )
        }, object : NetworkRequestCallbacks<ProfessionsResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as ProfessionsResponseModel
                        mProfessionalData.value = pojoResponse.data
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


    fun addPersonalisation(
        mSelectedProfession: MutableList<Profession>,
        experience: String,
        bio: String,
        certificates: MutableList<String>,
        pastWork: MutableList<String>,
        lat: Double,
        lng: Double,
        location: String,
        halfHourlyPrice: String,
        hourlyPrice: String,
        oneHalfHourlyPrice: String,
    ) {

        when {
            mSelectedProfession.isEmpty() -> errorHandler.value = ErrorHandler.EMPTY_SUB_PROFESSION
            !GeneralFunctions.isValidExperience(experience) -> errorHandler.value = ErrorHandler.EMPTY_EXPERIENCE
            halfHourlyPrice.isBlank() || hourlyPrice.isBlank() || oneHalfHourlyPrice.isBlank() -> errorHandler.value =
                ErrorHandler.EMPTY_RATE
            halfHourlyPrice.toInt()==0 || hourlyPrice.toInt()==0 || oneHalfHourlyPrice.toInt()==0 -> errorHandler.value =
                ErrorHandler.VALID_RATE

            location.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_LOCATION
            bio.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_BIO
            certificates.isEmpty() -> errorHandler.value = ErrorHandler.EMPTY_CERTIFICATES


            else -> {
                val mList = ArrayList<SelectedProfession>()
                mSelectedProfession.map { it ->
                    val profession = SelectedProfession()
                    profession.profession = it._id // Assuming `_id` is the profession ID
                    profession.professionName = null

                    // Collect only the IDs of subProfessions where checked is true
                    val checkedSubProfessionIds = it.subProfessions
                        ?.filter { subProfession -> subProfession.isChecked } // Filter checked subProfessions
                        ?.map { subProfession -> subProfession._id } // Map to IDs

                    profession.subProfessions?.addAll(checkedSubProfessionIds!!) // Add all checked subProfession IDs

                    mList.add(profession)
                }

                // Upload files to S3
                val mNewCertificateList = ArrayList<String>()
                certificates.forEach {
                    if (GeneralFunctions.isRemoteImage(it)) {
                        AmazonS3(getApplication()).uploadFileToS3(
                            File(it),
                            S3_PROFESSIONAL_CERTIFICATE

                        ) { isSuccess ->
                            if (isSuccess) {
                                // File uploaded successfully
                            } else {

                            }
                        }
                        mNewCertificateList.add(SERVER_PROFESSIONAL_CERTIFICATE + File(it).name)
                    } else {
                        mNewCertificateList.add(it)
                    }
                }

                // Upload files to S3
                val mNewPastWorkList = ArrayList<String>()
                pastWork.forEach {
                    if (GeneralFunctions.isRemoteImage(it)) {
                        AmazonS3(getApplication()).uploadFileToS3(
                            File(it),
                            S3_PROFESSIONAL_PAST_WORK

                        ) { isSuccess ->
                            if (isSuccess) {
                                // File uploaded successfully
                            } else {

                            }
                        }
                        mNewPastWorkList.add(SERVER_PROFESSIONAL_PAST_WORK + File(it).name)
                    } else {
                        mNewPastWorkList.add(it)
                    }
                }

                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.addPersonalisation(
                        EnterProfessionalDetailRequestModel(
                            bio,
                            mNewCertificateList,
                            experience,
                            mNewPastWorkList,
                            mList,
                            lat, lng, location,
                            hourlyRate = hourlyPrice,
                            halfHourlyRate = halfHourlyPrice,
                            oneAndHalfHourlyRate = oneHalfHourlyPrice


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
                                isDetailedSubmitted.value = true
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

    fun createAvailability(
        startDate: String,
        endDate: String,
        slots: List<AvailabilityTimeSlots>,
        excludeDates: List<String>,
        excludeWeeks: List<String>,
        slotType: String
    ) {
        when {
            startDate.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_START_DATE
            endDate.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_REPEAT_TILL_DATE
            slots.last().slots?.isEmpty() == true -> errorHandler.value = ErrorHandler.EMPTY_SLOTS

            else -> {

                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.createAvailability(
                        CreateAvailabilityRequestModel(
                            availabilitySlots = slots,
                            endDate = endDate,
                            excludedDates = excludeDates,
                            excludedDays = excludeWeeks,
                            slotType = slotType,
                            startDate = startDate

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
                                isDetailedSubmitted.value = true
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

    fun updateAvailabilitySlot(
        startDate: String,
        slots: List<AvailabilityTimeSlots>,
        slotType: String
    ) {
        when {
            startDate.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_START_DATE
            slots.last().slots?.isEmpty() == true -> errorHandler.value = ErrorHandler.EMPTY_SLOTS

            else -> {

                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.updateAvailabilitySlot(
                        UpdateAvailabilitySlotModel(
                            date = startDate,
                            availabilitySlots = slots,
                            slotType = slotType
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
                                isDetailedSubmitted.value = true
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

    fun getAvailability(date: String) {
        isShowSwipeRefreshLayout.value = true
        apiUtil.fetchResource({
            repository.getAvailability(
                date
            )
        }, object : NetworkRequestCallbacks<ProfessionalSlotsResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowSwipeRefreshLayout.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as ProfessionalSlotsResponseModel
                        mAvailabilityData.value = pojoResponse.data
                    }

                    else -> {
                        mAvailabilityData.value = AvailabilityData()
//                        retrofitErrorMessage
//                            .postValue(
//                                RetrofitErrorMessage(
//                                    errorMessage =
//                                    RetrofitRequest.getErrorMessage(response.errorBody()!!)
//                                )
//                            )
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

    fun addAccountDetail(
        token: String
    ) {

        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.addAccountDetail(
                token
            )
        }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as SimpleSuccessResponse
                        isDetailedSubmitted.value = true
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

    fun getAccountDetail(
    ) {
        isShowLoader.value = true
        apiUtil.fetchResource({
            repository.getAccountDetail(
            )
        }, object : NetworkRequestCallbacks<AccountDetailResponseModel> {
            override fun onSuccess(response: Response<*>) {
                isShowLoader.value = false
                val pojoNetworkResponse =
                    RetrofitRequest.checkForResponseCode(response.code())
                when {
                    pojoNetworkResponse.isSuccess && null != response.body() -> {
                        val pojoResponse = response.body() as AccountDetailResponseModel
                        mAccountDetail.value = pojoResponse.data
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

    fun contactUs(
        name: String,
        email: String,
        comment: String
    ) {
        when {
            name.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_NAME
            email.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_EMAIL
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> errorHandler.value =
                ErrorHandler.INVALID_EMAIL

            comment.isBlank() -> errorHandler.value = ErrorHandler.EMPTY_MESSAGE

            else -> {

                isShowLoader.value = true
                apiUtil.fetchResource({
                    repository.contactUs(
                        name, email, comment
                    )
                }, object : NetworkRequestCallbacks<SimpleSuccessResponse> {
                    override fun onSuccess(response: Response<*>) {
                        isShowLoader.value = false
                        val pojoNetworkResponse =
                            RetrofitRequest.checkForResponseCode(response.code())
                        when {
                            pojoNetworkResponse.isSuccess && null != response.body() -> {
                                val pojoResponse = response.body() as SimpleSuccessResponse
                                isDetailedSubmitted.value = true
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


    fun onGetProfessionalData() = mProfessionalData
    fun onDetailSubmitted() = isDetailedSubmitted
    fun onGetAvailability() = mAvailabilityData
    fun onGetAccountDetail() = mAccountDetail


}