package com.mentor.application.repository.models

data class PojoMessage(
    val `data`: MessageData=MessageData(),
    val message: String="",
    val statusCode: Int=0
)

data class MessageData(
    val chatListing: List<Message>?= mutableListOf(),
)

data class Message(
    val __v: Int=0,
    val _id: String="",
    val bookingId: String="",
    val message: String="",
    val type: String="",
    val createdAt: String="",
    val isDeleted: Boolean=false,
    var isSending: Boolean=false,
    val `receiver`: String="",
    val receiverType: String="",
    val sender: String="",
    val senderType: String="",
    val updatedAt: String=""
)