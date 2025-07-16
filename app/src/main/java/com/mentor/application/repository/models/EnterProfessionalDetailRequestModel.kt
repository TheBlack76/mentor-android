package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class EnterProfessionalDetailRequestModel(
    val bio: String = "",
    val certificate: List<String> = mutableListOf(),
    val experience: String = "",
    val pastWork: List<String> = mutableListOf(),
    val professions: List<SelectedProfession>? = mutableListOf(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val location: String = "",
    val hourlyRate: String = "",
    val halfHourlyRate: String = "",
    val oneAndHalfHourlyRate: String = "",
)

@Parcelize
data class SelectedProfession(
    var profession: String = "",
    var professionName: String? = "",
    var subProfessions: ArrayList<String>? = ArrayList()
) : Parcelable