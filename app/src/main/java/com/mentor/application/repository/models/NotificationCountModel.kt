package com.mentor.application.repository.models

data class NotificationCountModel(
    val `data`: CountData,
    val message: String,
    val statusCode: Int
)

data class CountData(
    val count: Int
)

