package com.mentor.application.repository.models

data class SignupRequestModel(
    val countryCode: String="",
    val deviceId: String="",
    val deviceToken: String="",
    val email: String="",
    val fullName: String="",
    val image: String="",
    val mobileNumber: String="",
    val userType: String="",
    val deviceType: String="android"
    )