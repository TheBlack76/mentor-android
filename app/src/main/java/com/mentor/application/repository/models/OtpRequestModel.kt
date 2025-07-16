package com.mentor.application.repository.models

data class OtpRequestModel(
    val code: String,
    val mobileNumber:String?,
    val countryCode:String?,
)