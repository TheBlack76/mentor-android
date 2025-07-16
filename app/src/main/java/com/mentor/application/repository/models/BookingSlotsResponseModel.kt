package com.mentor.application.repository.models

data class BookingSlotsResponseModel(
    val `data`: SlotsData=SlotsData(),
    val message: String="",
    val statusCode: Int=0
)

data class SlotsData(
    val date: String="",
    val slots: Slots=Slots()
)

data class Slots(
    val ninetyMin: List<TimeSlot>?= mutableListOf(),
    val sixtyMin: List<TimeSlot>?= mutableListOf(),
    val thirtyMin: List<TimeSlot>?= mutableListOf()
)