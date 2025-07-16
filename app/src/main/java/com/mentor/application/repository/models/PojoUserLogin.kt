package com.mentor.application.repository.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Created by Mukesh on 19/7/18.
 */
data class PojoUserLogin(
    val `data`: com.mentor.application.repository.models.UserData = UserData(),
    val message: String = "",
    val statusCode: Int = 0
)

data class UserData(
    val userData: User = User(),
    val tokenData: TokenData = TokenData()
)

@Parcelize
data class User(
    val _id: String = "",
    val countryCode: String = "",
    val createdAt: String = "",
    val email: String = "",
    val fullName: String = "",
    val image: String = "",
    val isBlocked: Boolean = false,
    val isDeleted: Boolean = false,
    val isPayment: Boolean = false,
    val isVerified: Boolean = false,
    val isSocialLogin: Boolean = false,
    val isRegister: Boolean = false,
    val isAvailability: Boolean = false,
    val isBankAccount: Boolean = false,
    val mobileNumber: String = "",
    val userType: String = "",
    val updatedAt: String = "",
    val bio: String = "",
    val experience: String = "",
    val hourlyRate: String = "",
    val location: String = "",
    val halfHourlyRate: String = "",
    val averageStars: Double=0.0,
    val upcomingBookingCount: Int=0,
    val completedBookingCount: Int=0,
    val reviews: List<ReviewData> = mutableListOf(),
    val oneAndHalfHourlyRate: String = "",
    val certificate: List<String>? = mutableListOf(),
    val pastWork: List<String>? = mutableListOf(),
    val loc: Coordinates = Coordinates(),
    val professions: List<Profession>? = mutableListOf()
) : Parcelable

@Parcelize
data class Coordinates(
    val coordinates: List<Double>? = mutableListOf(),
    val type: String = ""
) : Parcelable

data class TokenData(
    val expires: String = "",
    val token: String = ""
)

@Parcelize
data class ReviewData(
    val __v: Int=0,
    val _id: String="",
    val createdAt: String="",
    val customerId: CustomerId=CustomerId(),
    val image: String="",
    val isDeleted: Boolean=false,
    val message: String="",
    val professionalId: String="",
    val star: Double=0.0,
    val updatedAt: String=""
):Parcelable

