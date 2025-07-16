package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CustomerBookingResponseModel(
    val `data`: BookingData=BookingData(),
    val message: String="",
    val statusCode: Int=0
)


data class BookingData(
    val bookings: List<Booking>?= mutableListOf(),
    val countBookings: Int=0
)

@Parcelize
data class Booking(
    val __v: Int=0,
    var _id: String="",
    val bookingId: String="",
    val createdAt: String="",
    val customerId: String="",
    val date: String="",
    val device: String="",
    val durationType: String="",
    val endTime: String="",
    var bookingType: String="",
    val generalQuestion: String="",
    val isAccepted: Boolean=false,
    val isCancelled: Boolean=false,
    val isCompleted: Boolean=false,
    val isDeleted: Boolean=false,
    val isPayment: Boolean=false,
    var professionId: ProfessionId=ProfessionId(),
    var professionalId: String="",
    var professionalImage: String="",
    var professionalName: String="",
    val softwareType: String="",
    val startTime: String="",
    var subProfessionId: SubProfessionId= SubProfessionId(),
    var totalAmount: String="",
    val troubleshooting: String="",
    val updatedAt: String="",
    val status: String=""
):Parcelable

