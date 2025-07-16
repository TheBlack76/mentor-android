package com.mentor.application.repository.models

import android.os.Parcelable

@kotlinx.parcelize.Parcelize
data class ImagePreview(
    val image: String = "",
    val caption: String = "") : Parcelable