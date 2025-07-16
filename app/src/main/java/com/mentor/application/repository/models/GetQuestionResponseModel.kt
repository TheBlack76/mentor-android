package com.mentor.application.repository.models

class GetQuestionResponseModel(
    val message: String,
    val statusCode: Int,
    val data: List<String>
)