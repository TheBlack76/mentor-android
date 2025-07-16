package com.mentor.application.repository.models

data class InstantBookingPriceResponseModel(
    val `data`: InstantBookingPrice,
    val message: String,
    val statusCode: Int
)

data class InstantBookingPrice(
    val halfHourly: String,
    val hourly: String,
    val oneAndHalfHourly: String

)