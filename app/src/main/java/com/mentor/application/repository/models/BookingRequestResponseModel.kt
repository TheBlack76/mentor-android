package com.mentor.application.repository.models

data class BookingRequestResponseModel(
    val `data`: BookingRequestData=BookingRequestData(),
    val message: String="",
    val statusCode: Int=0
)

data class BookingRequestData(
    val bookings: List<BookingRequest>?= mutableListOf(),
    val countBookings: Int=0
)

data class BookingRequest(
    val __v: Int=0,
    val _id: String="",
    val bookingId: String="",
    val createdAt: String="",
    val requestSentTime: String="",
    val requestDurationTime: Int=0,
    val customerId: User=User(),
    val date: String="",
    val device: String="",
    val durationType: String="",
    val endTime: String="",
    val generalQuestion: String="",
    val isAccepted: Boolean=false,
    val isCancelled: Boolean=false,
    val isCompleted: Boolean=false,
    val isDeleted: Boolean=false,
    val isPayment: Boolean=false,
    val bookingType: String="",
    val professionId: ProfessionId=ProfessionId(),
    val professionalId: String="",
    val softwareType: String="",
    val status: String="",
    val startTime: String="",
    val subProfessionId: SubProfessionId=SubProfessionId(),
    val totalAmount: String="",
    val bookingAmount: String="",
    val offeredPrice: String="",
    val percentage: String="",
    val troubleshooting: String="",
    val updatedAt: String="",
)
