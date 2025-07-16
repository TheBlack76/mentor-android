package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class BookingInfoResponseModel(
    val `data`: BookingInfo=BookingInfo(),
    val message: String="",
    val statusCode: Int=0
)

@Parcelize
data class BookingInfo(
    val __v: Int=0,
    val _id: String="",
    val bookingId: String="",
    val createdAt: String="",
    val customerId: CustomerId=CustomerId(),
    val date: String="",
    val durationType: String="",
    val bookingType: String="",
    val endTime: String="",
    val isAccepted: Boolean=false,
    val isCancelled: Boolean=false,
    val isCompleted: Boolean=false,
    val isDeleted: Boolean=false,
    val isPayment: Boolean=false,
    val resolvedStatus: Boolean=false,
    val isSession: Boolean=false,
    val generalQuestions: List<Questions>?= mutableListOf(),
    val professionId: ProfessionId=ProfessionId(),
    val professionalId: User=User(),
    val startTime: String="",
    val percentage: String="",
    val subProfessionId: SubProfessionId=SubProfessionId(),
    val totalAmount: String="",
    val bookingAmount: String="",
    val offeredPrice: String="",
    val platformFee: String="",
    val description: String="",
    val updatedAt: String="",
    val status: String="",
    val reviews: Review=Review()
):Parcelable

@Parcelize
data class CustomerId(
    val _id: String="",
    val email: String="",
    val fullName: String="",
    val image: String=""
):Parcelable

@Parcelize
data class Review(
    val _id: String="",
    val message: String="",
    val star: Double=0.0,
    val createdAt: String=""
):Parcelable


//data class ProfessionId(
//    val __v: Int=0,
//    val _id: String="",
//    val createdAt: String="",
//    val isDeleted: Boolean=false,
//    val profession: String="",
//    val updatedAt: String=""
//)
//
//data class SubProfessionId(
//    val __v: Int=0,
//    val _id: String="",
//    val createdAt: String="",
//    val image: String="",
//    val isDeleted: Boolean=false,
//    val professionId: String="",
//    val subProfession: String="",
//    val updatedAt: String=""
//)