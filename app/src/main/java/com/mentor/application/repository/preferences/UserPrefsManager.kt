package com.mentor.application.repository.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mentor.application.repository.models.User
import com.mentor.application.repository.models.UserData
import com.mentor.application.utils.ApplicationGlobal
import com.google.gson.Gson
import com.mentor.application.utils.ApplicationGlobal.Companion.CUSTOMER
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Mukesh on 20/7/18.
 */

@Singleton
class UserPrefsManager @Inject constructor(@ApplicationContext context: Context) {

    private val mSharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    private val mEditor: SharedPreferences.Editor = mSharedPreferences.edit()

    private val masterKeyAlias = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    companion object {
        // SharedPreference Keys
        private const val PREFS_FILENAME = "secret_shared_prefs"
        private const val PREFS_USER = "user"
        private const val PREFS_IS_LOGINED = "isLogined"
        private const val PREFS_ACCESS_TOKEN = "accessToken"
    }


    fun clearUserPrefs() {
        ApplicationGlobal.accessToken = ""
        ApplicationGlobal.mUserType = CUSTOMER
        mEditor.clear()
        mEditor.apply()
    }

    val isLogin: Boolean
        get() = mSharedPreferences.getBoolean(PREFS_IS_LOGINED, false)

    fun saveUserSession(userData: UserData) {
        mEditor.putString(PREFS_ACCESS_TOKEN, userData.tokenData.token)
        ApplicationGlobal.accessToken = userData.tokenData.token
        ApplicationGlobal.mUserType = userData.userData.userType

        userData.userData.let { user ->
            mEditor.putString(PREFS_USER, Gson().toJson(user))
        }
        mEditor.apply()
    }

    fun setLogin(isRememberMe: Boolean = true) {
        mEditor.putBoolean(PREFS_IS_LOGINED, isRememberMe)
        mEditor.apply()
    }

    val loginUser: User?
        get() = Gson().fromJson(
            mSharedPreferences.getString(PREFS_USER, ""),
            User::class.java
        )

    fun updateUserData(user: User?) {
        if (null != user) {
            mEditor.putString(PREFS_USER, Gson().toJson(user))
            mEditor.apply()
        }
    }

    val accessToken: String
        get() = mSharedPreferences.getString(PREFS_ACCESS_TOKEN, "") ?: ""

}