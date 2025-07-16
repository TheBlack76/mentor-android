package com.mentor.application.utils

import android.os.Environment

/**
 * Created by Mukesh on 20/7/18.
 */
object Constants {

    private const val APP_NAME = "Mentor"

    // Date Time Format Constants
    const val DATE_FORMAT_SERVER = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val REQUEST_DATE_FORMAT_SERVER = "yyyy-MM-dd"
    const val DATE_FORMAT_SERVER_GMT = "EEE MMM dd HH:mm:ss zzz yyyy"
    const val DATE_FORMAT_DISPLAY = "d MMM, yyyy"
    const val DATE_TIME_FORMAT_DISPLAY = "d MMM, yyyy, hh:mma"
    const val DATE_SERVER_TIME = "HH:mm"
    const val DATE_DISPLAY_TIME = "hh:mma"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id="
    const val PLAY_STORE_MARKET_URL = "market://details?id="

    // Media Constants
    private val LOCAL_STORAGE_BASE_PATH_FOR_MEDIA = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/" + APP_NAME
    val LOCAL_STORAGE_BASE_PATH_FOR_USER_PHOTOS = "$LOCAL_STORAGE_BASE_PATH_FOR_MEDIA/Users/Photos/"
    val LOCAL_STORAGE_BASE_PATH_FOR_QUESTION_PHOTOS =
        "$LOCAL_STORAGE_BASE_PATH_FOR_MEDIA/Questions/Media"
    val LOCAL_STORAGE_BASE_PATH_FOR_CHAT_MEDIA = "$LOCAL_STORAGE_BASE_PATH_FOR_MEDIA/Chats/"
    const val IMAGES_FOLDER = "Images/"
    const val VIDEOS_FOLDER = "Videos/"
    const val VIDEOS_THUMBS_FOLDER = "Thumbs/"

}