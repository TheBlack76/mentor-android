package com.mentor.application.repository.networkrequests

import com.mentor.application.repository.models.AcceptCallResponseModel
import com.mentor.application.repository.models.AccountDetailResponseModel
import com.mentor.application.repository.models.BookingRequestResponseModel
import com.mentor.application.repository.models.CreateAvailabilityRequestModel
import com.mentor.application.repository.models.EnterProfessionalDetailRequestModel
import com.mentor.application.repository.models.ProfessionalSlotsResponseModel
import com.mentor.application.repository.models.ProfessionsResponseModel
import com.mentor.application.repository.models.SimpleSuccessResponse
import com.mentor.application.repository.models.UpdateAvailabilitySlotModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ProfessionalAPI : CustomerAPI{

    @GET("user/profession/getProfessions")
    suspend fun getProfessions(
    ): Response<ProfessionsResponseModel>


    @PUT("professional/auth/editPersonalisation")
    suspend fun addPersonalisation(
        @Body mProfessionalDetailRequestModel: EnterProfessionalDetailRequestModel
    ): Response<SimpleSuccessResponse>


    @GET("user/profession/professionalBookings")
    suspend fun getProfessionalBookings(
        @Query("bookingType") subProfessionId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int?,
    ): Response<BookingRequestResponseModel>

    @FormUrlEncoded
    @PUT("user/profession/acceptOrReject/v2")
    suspend fun bookingResponse(
        @Field ("bookingId") bookingId:String,
        @Field ("type") type:String,
        @Field ("amount") amount:String?=null,
    ): Response<AcceptCallResponseModel>

    @POST("user/profession/slots")
    suspend fun createAvailability(
       @Body mCreateAvailabilityRequestModel: CreateAvailabilityRequestModel
    ): Response<SimpleSuccessResponse>

    @PUT("user/profession/slots")
    suspend fun updateAvailabilitySlot(
        @Body mUpdateAvailabilitySlotModel: UpdateAvailabilitySlotModel
    ): Response<SimpleSuccessResponse>

    @FormUrlEncoded
    @POST("professional/auth/bankAccount")
    suspend fun addAccountDetail(
        @Field ("token") accountId:String
    ): Response<SimpleSuccessResponse>

    @GET("professional/auth/bankAccount")
    suspend fun getAccountDetail(
    ): Response<AccountDetailResponseModel>

    @GET("user/profession/professionalSlots")
    suspend fun getAvailability(
        @Query ("date") date:String
    ): Response<ProfessionalSlotsResponseModel>

    @FormUrlEncoded
    @POST("user/common/contactUs")
    suspend fun contactUs(
        @Field("name") name:String,
        @Field("email") email:String,
        @Field("message") comment:String
    ): Response<SimpleSuccessResponse>


}
