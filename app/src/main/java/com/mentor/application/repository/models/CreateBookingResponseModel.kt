package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CreateBookingResponseModel(
    val `data`: CreateBookingData=CreateBookingData(),
    val message: String="",
    val statusCode: Int=0
)

data class CreateBookingData(
    val __v: Int=0,
    val _id: String="",
    val createdAt: String="",
    val date: String="",
    val device: String="",
    val endTime: String="",
    val generalQuestion: String="",
    val isAccepted: Boolean=false,
    val isCancelled: Boolean=false,
    val isCompleted: Boolean=false,
    val isDeleted: Boolean=false,
    val isPayment: Boolean=false,
    val professionId: ProfessionId=ProfessionId(),
    val softwareType: String="",
    val startTime: String="",
    val subProfessionId: SubProfessionId=SubProfessionId(),
    val totalAmount: Int=0,
    val troubleshooting: String="",
    val updatedAt: String="",
    val payment: Payment=Payment()
)
@Parcelize
data class ProfessionId(
    val __v: Int=0,
    val _id: String="",
    val createdAt: String="",
    val isDeleted: Boolean=false,
    val profession: String="",
    val updatedAt: String=""
):Parcelable

@Parcelize
data class SubProfessionId(
    val __v: Int=0,
    val _id: String="",
    val createdAt: String="",
    val image: String="",
    val isDeleted: Boolean=false,
    val professionId: String="",
    val subProfession: String="",
    val updatedAt: String=""
):Parcelable


@Parcelize
data class Payment(
    val customerId: String="",
    val paymentIntent: String="",
    val ephemeralKey: String="",
):Parcelable