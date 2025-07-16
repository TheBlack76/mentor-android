package com.mentor.application.repository.networkrequests


import com.mentor.application.repository.models.*

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface CustomerAPI {

    @POST("auth/signup")
    suspend fun signUp(
        @Body mSignupRequestModel: SignupRequestModel
    ): Response<PojoUserLogin>

    @POST("auth/login")
    suspend fun login(
        @Body mLoginRequestModel: LoginRequestModel
    ): Response<PojoUserLogin>

    @FormUrlEncoded
    @POST("auth/socialLogin")
    suspend fun socialLogin(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("image") image: String,
        @Field("socialId") socialId: String,
        @Field("userType") userType: String,
        @Field("deviceToken") deviceToken: String,
        @Field("deviceId") deviceId: String,
        @Field("deviceType") deviceType: String = "android",
    ): Response<PojoUserLogin>

    @FormUrlEncoded
    @POST("auth/mobileNumber")
    suspend fun registerNumber(
        @Field("countryCode") countryCode: String,
        @Field("mobileNumber") mobileNumber: String,
    ): Response<PojoUserLogin>

    @POST("auth/verifyOtp")
    suspend fun verifyOtp(
        @Body mOtpRequestModel: OtpRequestModel
    ): Response<SimpleSuccessResponse>

    @POST("auth/resendOtp")
    suspend fun resendOtp(
    ): Response<SimpleSuccessResponse>

    @PUT("auth/logout")
    suspend fun logout(
    ): Response<SimpleSuccessResponse>

    @DELETE("auth/deleteAccount")
    suspend fun deleteAccount(
    ): Response<SimpleSuccessResponse>

    @GET("/user/profession/notification/unseenCount")
    suspend fun getMessageCount(
    ): Response<NotificationCountModel>

    @GET("auth/userInfo")
    suspend fun getProfile(
    ): Response<ProfileResponseModel>

    @FormUrlEncoded
    @PUT("auth/editProfile")
    suspend fun editProfile(
        @Field("fullName") fullName: String?,
        @Field("email") email: String?,
        @Field("image") image: String?,
        @Field("latitude") latitude: Double?,
        @Field("longitude") longitude: Double?,
        @Field("location") location: String?,
        @Field("countryCode") countryCode: String?,
        @Field("mobileNumber") mobileNumber: String?,
    ): Response<PojoUserLogin>


    @GET("user/profession/getProfessionals")
    suspend fun getProfessional(
        @Query("professionId") professionId: String,
        @Query("subProfessionId") subProfessionId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int?,
        @Query("minPrice") minPrice: Float?,
        @Query("maxPrice") maxPrice: Float?,
        @Query("distance") distance: Float?,
        @Query("rating") rating: Float?,
    ): Response<ProfessionalsResponseModel>

    @GET("user/profession/getProfessions")
    suspend fun getServices(
    ): Response<ProfessionsResponseModel>

    @GET("user/profession/slots")
    suspend fun getSlots(
        @Query("professionalId") professionalId: String?,
        @Query("bookingId") bookingId: String?,
        @Query("date") date: String?,
    ): Response<BookingSlotsResponseModel>

    @GET("user/profession/professionQuestions")
    suspend fun getQuestions(
        @Query("professionId") professionId: String?,
    ): Response<GetQuestionResponseModel>

    @POST("user/profession/createBooking")
    suspend fun createBooking(
        @Body mCreateBookingRequestModel: CreateBookingRequestModel
    ): Response<CreateBookingResponseModel>

    @FormUrlEncoded
    @POST("user/profession/session-checkout")
    suspend fun confirmBooking(
        @Field("bookingId") bookingId: String?,
    ): Response<SimpleSuccessResponse>

    @FormUrlEncoded
    @PUT("user/profession/reScheduleBooking")
    suspend fun reScheduleBooking(
        @Field("bookingId") bookingId: String?,
        @Field("startTime") startTime: String?,
        @Field("endTime") endTime: String?,
        @Field("date") date: String?,
    ): Response<SimpleSuccessResponse>

    @GET("user/profession/instantMaxPrice")
    suspend fun getBookingPrices(
        @Query("professionId") bookingId: String?,
        @Query("subProfessionId") startTime: String?,
    ): Response<InstantBookingPriceResponseModel>

    @FormUrlEncoded
    @POST("user/profession/createInstantBooking/v2")
    suspend fun createInstantBooking(
        @Field("professionId") professionId: String?,
        @Field("subProfessionId") subProfessionId: String?,
        @Field("professionalId") professionalId: String?,
        @Field("offeredPrice") offerPrice: String?,
        @Field("slot") slot: String?,
        @Field("description") description: String?,
    ): Response<CreateBookingResponseModel>

    @GET("user/profession/customerBookings")
    suspend fun getBookings(
        @Query("bookingType") subProfessionId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int?,
    ): Response<CustomerBookingResponseModel>

    @GET("user/profession/bookingInfo")
    suspend fun getBookingInfo(
        @Query("bookingId") bookingId: String
    ): Response<BookingInfoResponseModel>

    @FormUrlEncoded
    @POST("user/profession/cancelBookingByVendor")
    suspend fun cancelBookingByVendor(
        @Field("bookingId") bookingId: String
    ): Response<SimpleSuccessResponse>

    @GET("user/profession/review")
    suspend fun getReviews(
        @Query("professionalId") professionalId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Response<ReviewsResponseModel>

    @FormUrlEncoded
    @POST("user/profession/review")
    suspend fun addReview(
        @Field("bookingId") bookingId: String,
        @Field("professionalId") professionalId: String,
        @Field("message") message: String,
        @Field("star") star: Float,
        @Field("image") image: String,
    ): Response<SimpleSuccessResponse>

    @FormUrlEncoded
    @POST("user/profession/cancelBooking")
    suspend fun cancelBooking(
        @Field("bookingId") bookingId: String
    ): Response<SimpleSuccessResponse>

    @GET("user/profession/chats")
    suspend fun getChat(
        @Query("bookingId") bookingId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<PojoMessage>

    @FormUrlEncoded
    @PUT("user/profession/bookingCompleted")
    suspend fun completeBooking(
        @Field("bookingId") bookingId: String,
        @Field("resolvedStatus") resolvedStatus: Int,
        @Field("other") other: String?
    ): Response<SimpleSuccessResponse>

    @FormUrlEncoded
    @POST("user/profession/joinSession")
    suspend fun joinSession(
        @Field("bookingId") bookingId: String,
    ): Response<JoinSessionResponseModel>

    @GET("user/profession/notificationListing")
    suspend fun getNotification(
        @Query("page") page:Int,
        @Query("limit") limit:Int,
    ): Response<NotificationResponseModel>


}