package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class AcceptCallResponseModel(
    val `data`: AcceptCallData= AcceptCallData(),
    val message: String="",
    val statusCode: Int=0
)

@Parcelize
data class AcceptCallData(
    val session: SessionData=SessionData(),
    val updatedBooking: BookingInfo=BookingInfo()
):Parcelable
