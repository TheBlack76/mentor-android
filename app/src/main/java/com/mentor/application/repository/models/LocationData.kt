package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class LocationData(
    var name:String="",
    var lat:Double=0.0,
    var lng : Double=0.0
):Parcelable