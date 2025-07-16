package com.mentor.application.repository.models

data class AccountDetailResponseModel(
    val `data`: AccountData,
    val message: String,
    val statusCode: Int
)

data class AccountData(
    val accountHolderName: String,
    val accountNumber: String,
    val routingNumber: String
)