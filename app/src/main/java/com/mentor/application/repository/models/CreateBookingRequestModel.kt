package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CreateBookingRequestModel(
    val date: String="",
    val endTime: String="",
    val professionalId: String="",
    val professionId: String="",
    val subProfessionId: String="",
    val startTime: String="",
    val generalQuestions: List<Questions>?= mutableListOf()
)

@Parcelize
data class Questions(
    var question: String="",
    var answer: String=""

):Parcelable