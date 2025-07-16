package com.mentor.application.repository.networkrequests

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Created by Mukesh on 20/7/18.
 */
object RetrofitUtils {

    private const val MULTIPART_FORM_DATA = "multipart/form-data"

//    fun stringToRequestBody(string: String): RequestBody {
//        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), string)
//    }
//
//    fun imageToRequestBody(file: File): RequestBody {
//        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), file)
//    }
//
fun createPartFromFile(partName: String, file: File): MultipartBody.Part {
    // create RequestBody instance from file
    val requestFile = file.asRequestBody(MULTIPART_FORM_DATA.toMediaTypeOrNull())

    // MultipartBody.Part is used to send also the actual file name
    return MultipartBody.Part.createFormData(partName, file.name, requestFile)
}

}