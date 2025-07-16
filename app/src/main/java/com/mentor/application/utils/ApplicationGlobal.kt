package com.mentor.application.utils

import android.app.Application
import android.os.Build
import android.provider.Settings
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.regions.Regions
import com.mentor.application.repository.preferences.UserPrefsManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.FirebaseApp
import com.mentor.application.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import java.util.*


@HiltAndroidApp
class ApplicationGlobal : Application() {

    companion object {
        var accessToken: String = ""
        var deviceLocale: String = ""
        var timeZone = ""
        var deviceUniqueId: String = ""
        const val CUSTOMER = "customer"
        const val PROFESSIONAL = "professional"
        var mUserType=CUSTOMER
        var inChatConversationId : String? = null
        var isAndroidFourteen=false

    }

    override fun onCreate() {
        super.onCreate()

        // Initialize fresco
        Fresco.initialize(this)

        FirebaseApp.initializeApp(this)

        // Get device locale
        deviceLocale = Locale.getDefault().language

        TransferNetworkLossHandler.getInstance(this)

        // Get session id
        accessToken = UserPrefsManager(this).accessToken

        // Get userType
        mUserType = UserPrefsManager(this).loginUser?.userType?:CUSTOMER

        // Get device id
        deviceUniqueId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // get timezone
        timeZone = TimeZone.getDefault().id

        S3ImageLoader.initialize(
            poolId = BuildConfig.COGNITO_POOL_ID,
            region = Regions.fromName("us-west-1")
        )

        isAndroidFourteen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    }
}
