package com.mentor.application.repository.models

data class CreateAvailabilityRequestModel(
    val availabilitySlots: List<AvailabilityTimeSlots>,
    val endDate: String,
    val excludedDates: List<String>,
    val excludedDays: List<String>,
    val slotType: String,
    val startDate: String
)