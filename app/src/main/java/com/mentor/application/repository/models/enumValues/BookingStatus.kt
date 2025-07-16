package com.mentor.application.repository.models.enumValues

enum class BookingStatus(var value:String) {
    CANCELLED("cancelled"),
    REJECTED("rejected"),
    COMPLETED("completed"),
    ACCEPTED("accepted"),
    REQUESTED("requested"),
    ONGOING("ongoing"),
}