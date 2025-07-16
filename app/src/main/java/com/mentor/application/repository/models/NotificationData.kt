package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class NotificationData(
    val bookingId:String="",
    val userName:String="",
):Parcelable
