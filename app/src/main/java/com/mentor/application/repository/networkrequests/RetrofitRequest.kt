package com.mentor.application.repository.networkrequests

import com.mentor.application.repository.models.PojoNetworkResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody


/**
 * Created by Mukesh on 20/7/18.
 */
object RetrofitRequest {

    fun checkForResponseCode(code: Int): PojoNetworkResponse {
        return when (code) {
            200 -> PojoNetworkResponse(isSuccess = true, isSessionExpired = false)
            401 -> PojoNetworkResponse(isSuccess = false, isSessionExpired = true)
            else -> PojoNetworkResponse(isSuccess = false, isSessionExpired = false)
        }
    }

    fun getErrorMessage(responseBody: ResponseBody): String {
        val errorMessage = ""
        try {
            val gson = Gson()
            val type = object : TypeToken<com.mentor.application.repository.models.Error>() {}.type
            val errorResponse: com.mentor.application.repository.models.Error? = gson.fromJson(responseBody.charStream(), type)
            return errorResponse?.message!!.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return errorMessage
    }

}