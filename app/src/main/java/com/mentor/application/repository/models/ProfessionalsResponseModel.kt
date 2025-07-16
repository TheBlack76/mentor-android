package com.mentor.application.repository.models

data class ProfessionalsResponseModel(
    val `data`: Data=Data(),
    val message: String="",
    val statusCode: Int=0
)

data class Data(
    val professionalCount: Int=0,
    val professionalList: List<User>?= mutableListOf()
)
