package com.app.glambar.repository.models.post

data class PostFacebookLogin(
    val name_en: String,
    val facebook_id: String,
    val email: String,
    val device_type: String,
    val device_token: String,
    var user_type: String=""
)