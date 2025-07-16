package com.mentor.application.repository.models

data class NotificationResponseModel(
    val `data`: Notification=Notification(),
    val message: String="",
    val statusCode: Int=0
)

data class Notification(
    val notificationListing: List<NotificationListing>?= mutableListOf()
)

data class NotificationListing(
    val __v: Int=0,
    val _id: String="",
    val bookingId: String="",
    val createdAt: String="",
    val isDeleted: Boolean=false,
    val professionalId: String="",
    val pushType: String="",
    val title: String="",
    val description: String="",
    val updatedAt: String=""
)