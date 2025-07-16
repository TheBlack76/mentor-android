package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ProfessionalSlotsResponseModel(
    val `data`: AvailabilityData=AvailabilityData(),
    val message: String="",
    val statusCode: Int=0
)

@Parcelize
data class AvailabilityData(
    val __v: Int=0,
    val _id: String="",
    val availabilitySlots: List<AvailabilityTimeSlots>?= mutableListOf(),
    val createdAt: String="",
    val endDate: String="",
    val isDeleted: Boolean=false,
    val professionalId: String="",
    val slotType: String="",
    val startDate: String="",
    val updatedAt: String=""
):Parcelable
