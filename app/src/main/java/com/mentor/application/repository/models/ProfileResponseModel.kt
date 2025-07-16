package com.mentor.application.repository.models

data class ProfileResponseModel(
    val `data`: User=User(),
    val message: String="",
    val statusCode: Int=0
)

