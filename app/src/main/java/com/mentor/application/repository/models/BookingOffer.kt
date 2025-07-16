package com.mentor.application.repository.models

data class BookingOffer(
    val fullName: String,
    val image: String,
    val offeredPrice: String,
    val duration: String,
    val rating: String,
    val professionalId: String
)