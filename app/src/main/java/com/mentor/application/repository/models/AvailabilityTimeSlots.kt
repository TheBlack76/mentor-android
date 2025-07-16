package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class AvailabilityTimeSlots(
    var startTime: String = "",
    var endTime: String = "",
    var duration: String = "",
    var slots: List<TimeSlot>? = mutableListOf()
) : Parcelable

@Parcelize
class TimeSlot(
    var startTime: String = "",
    var endTime: String = "",
) : Parcelable