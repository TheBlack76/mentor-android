package com.mentor.application.repository.models

data class ReviewsResponseModel(
    val `data`: ReviewsData=ReviewsData(),
    val message: String="",
    val statusCode: Int=0
)

data class ReviewsData(
    val reviewCount: Int=0,
    val reviews: List<ReviewData>?= mutableListOf()
)
