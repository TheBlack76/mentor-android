package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class JoinSessionResponseModel(
    val `data`: SessionData=SessionData(),
    val message: String="",
    val statusCode: Int
)

@Parcelize
data class SessionData(
    val __v: Int=0,
    val _id: String="",
    val bookingId: String="",
    val createdAt: String="",
    val isDeleted: Boolean=false,
    val sessionId: String="",
    val token: String="",
    val appId: String="",
    val updatedAt: String=""
):Parcelable