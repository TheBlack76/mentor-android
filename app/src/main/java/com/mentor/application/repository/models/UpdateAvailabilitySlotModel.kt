package com.mentor.application.repository.models

data class UpdateAvailabilitySlotModel(
    val availabilitySlots: List<AvailabilityTimeSlots>,
    val date: String,
    val slotType: String
)
