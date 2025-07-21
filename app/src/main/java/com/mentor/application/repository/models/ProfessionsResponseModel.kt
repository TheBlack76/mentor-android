package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ProfessionsResponseModel(
    val `data`: ProfessionsData = ProfessionsData(),
    val message: String = "",
    val statusCode: Int = 0
)

@Parcelize
data class ProfessionsData(
    val professionCount: Int = 0,
    val professionList: List<Profession>? = mutableListOf()
) : Parcelable

@Parcelize
data class Profession(
    val __v: Int = 0,
    val _id: String = "",
    val createdAt: String = "",
    val isDeleted: Boolean = false,
    val profession: String = "",
    var isOpen: Boolean = false,
    val subProfessions: List<SubProfession>? = mutableListOf(),
    val updatedAt: String = ""
) : Parcelable

@Parcelize
data class SubProfession(
    val __v: Int = 0,
    val _id: String = "",
    val createdAt: String = "",
    val image: String = "",
    val isDeleted: Boolean = false,
    var isChecked: Boolean = false,
    val professionId: String = "",
    val subProfession: String = "",
    val updatedAt: String = ""
) : Parcelable