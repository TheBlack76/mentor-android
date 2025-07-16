package com.mentor.application.views.comman.activities

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.mentor.application.R
import com.mentor.application.databinding.ActivityMainBinding
import com.mentor.application.services.MyFirebaseMessagingService.Companion.isInstantNotificationGet
import com.mentor.application.utils.ApplicationGlobal
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.customer.fragment.DashboardFragment
import com.mentor.application.views.customer.fragment.DashboardFragment.Companion.BUNDLE_TAB_HOME
import com.mentor.application.views.customer.fragment.DashboardFragment.Companion.BUNDLE_TAB_NUMBER
import com.mentor.application.views.vendor.fragments.VendorDashboardFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : BaseAppCompactActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    companion object {
        const val NEW_INSTANT_REQUEST_INTENT = "instantRequestIntent"
    }

    override val isMakeStatusBarTransparent: Boolean
        get() = false

    override fun init() {

        GeneralFunctions.windowDecoder(binding.flFragContainerMain)


        if (ApplicationGlobal.mUserType == ApplicationGlobal.CUSTOMER) {
            // Set Splash Screen
            doFragmentTransaction(
                fragment = DashboardFragment.newInstance(
                    intent?.getIntExtra(
                        BUNDLE_TAB_NUMBER,
                        BUNDLE_TAB_HOME
                    ) ?: BUNDLE_TAB_HOME
                ),
                containerViewId = R.id.flFragContainerMain, isAddToBackStack = false
            )
        } else {
            // Set Splash Screen
            doFragmentTransaction(
                fragment = VendorDashboardFragment(),
                containerViewId = R.id.flFragContainerMain, isAddToBackStack = false
            )
        }

        // Request for notification permissions
        if (ContextCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(POST_NOTIFICATIONS),
                22
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Initialize receiver
        registerReceiver(
            mGetUpdateDataBroadcastReceiver,
            IntentFilter(NEW_INSTANT_REQUEST_INTENT), Context.RECEIVER_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGetUpdateDataBroadcastReceiver)
    }

    override val navHostFragment: NavHostFragment?
        get() = null

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    if (isInstantNotificationGet) {
                        val intent = Intent(this@HomeActivity, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                        isInstantNotificationGet = false
                    }
                } catch (e: Exception) {
                    println(e)

                }

            }
        }
    }

}
