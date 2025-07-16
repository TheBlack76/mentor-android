package com.swingby.app.views.fragments.base

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import android.view.LayoutInflater
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewbinding.ViewBinding
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


abstract class BaseLocationFragment<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : BaseFragment<VB>(bindingInflater) {

    companion object {
        private const val RQ_LOCATION_SETTINGS = 328
        private const val UPDATE_INTERVAL = 60 * 1000L  /* 60 secs */
        private const val FASTEST_INTERVAL = 30 * 1000L /* 30 secs */
        private const val UPDATE_DELAY = 100L
    }


    private val mLocationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            .setMaxUpdateDelayMillis(UPDATE_DELAY)
            .build()
    }

    private lateinit var mLocationCallback: LocationCallback
    private val mFusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(activityContext)
    }


    protected fun checkForLocationPermission() {
        // Ex. Launching ACCESS_FINE_LOCATION permission.
        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        // Get last known location
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            // Got last known location. In some rare situations this can be null.
            if (null != location) {
                onLocationUpdated(location)
            } else {
                // Initialize Location Callback
                mLocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        locationResult.let { result ->
                            result.lastLocation?.let { onLocationUpdated(it) }

                            // Remove Location updates
                            mFusedLocationProviderClient
                                .removeLocationUpdates(mLocationCallback)
                        }
                    }
                }

                // Request for location updates
                mFusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper()
                )
            }
        }

    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onAllLocationPermissionsGranted(true)
            getLocation()
        } else {
            onAllLocationPermissionsGranted(false)
            showMessage(
                com.mentor.application.R.string.enable_location_permission, null,
                true
            )
        }
    }

    private fun checkForUserLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)
            .setNeedBle(true)
            .setAlwaysShow(true)

        val result =
            LocationServices.getSettingsClient(activityContext as BaseAppCompactActivity<*>)
                .checkLocationSettings(builder.build())

        result.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            getLocation()
        }
        result.addOnFailureListener {
            if (it is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(it.resolution.intentSender).build()
                    phonePickIntentResultLauncher.launch(intentSenderRequest)
                    /* it.startResolutionForResult(activityContext as BaseAppCompactActivity,
                             RQ_LOCATION_SETTINGS)*/
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private val phonePickIntentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result != null) {
                if (result.resultCode == Activity.RESULT_OK) {
                    getLocation()
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    onAllLocationPermissionsGranted(false)
                }

            }
        }


    abstract fun onAllLocationPermissionsGranted(isLocationPermissionGranted: Boolean)
    abstract fun onLocationUpdated(location: Location)
}