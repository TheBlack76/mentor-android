package com.mentor.application.views.customer.fragment

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.mentor.application.R
import com.mentor.application.databinding.FragmentSelectLocationBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.LocationData
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.customer.fragment.ProfileFragment.Companion.INTENT_PROFILE
import com.mentor.application.views.vendor.fragments.EnterDetailFragment.Companion.INTENT_ENTER_DETAIL
import com.mentor.application.views.vendor.fragments.EnterDetailFragment.Companion.INTENT_LOCATION_DATA
import com.swingby.app.views.fragments.base.BaseLocationFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SelectLocationFragment :
    BaseLocationFragment<FragmentSelectLocationBinding>(FragmentSelectLocationBinding::inflate),
    OnMapReadyCallback,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener, LocationListener,
    OnClickListener {

    companion object {
        private const val GOOGLE_PLACES_API_KEY = "AIzaSyBm2svJgJZO5XQfmepCHtqIThf6ByEf-n0"
        const val BUNDLE_USER_TYPE = "userType"
        const val BUNDLE_CUSTOMER = 0
        const val BUNDLE_PROFESSIONAL = 1

        fun newInstance(userType: Int): SelectLocationFragment {
            val args = Bundle()
            val fragment = SelectLocationFragment()
            fragment.arguments = args
            args.putInt(BUNDLE_USER_TYPE, userType)
            return fragment
        }

    }

    private val mViewModel: OnBoardingViewModel by viewModels()
    var mMap: GoogleMap? = null
    var mLatitide = 0.0
    var mLongitude = 0.0
    var mLocationName = ""
    var mUserType = BUNDLE_CUSTOMER
    var autoCompleteAddress = ""

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {

        // Get argument
        mUserType = arguments?.getInt(BUNDLE_USER_TYPE) ?: BUNDLE_CUSTOMER

        // Set map view
        Handler(Looper.myLooper()!!).postDelayed({
            binding.fgGoogleMap.onCreate(savedInstanceState)
            binding.fgGoogleMap.getMapAsync(this)
            binding.fgGoogleMap.onStart()

            // Allow permissions
            checkForLocationPermission()
        }, 500)

        // Set click listener
        binding.tvLocation.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onProfileUpdate().observe(this, Observer {
            // Send broadcast
            requireContext().sendBroadcast(Intent(INTENT_PROFILE))
            (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
        })
    }

    override fun onAllLocationPermissionsGranted(isLocationPermissionGranted: Boolean) {

    }

    override fun onLocationUpdated(location: Location) {
        // Get Location
        mLatitide = location.latitude
        mLongitude = location.longitude

        setLocation(mLatitide, mLongitude)


        // Animate to location
        if (mMap != null) {
            mapInit(mMap!!)
        }

    }

    override fun onCameraMove() {
        // Clear map
        mMap!!.clear()
        // display imageView
        binding.imgLocationPinUp.visibility = View.VISIBLE

    }

    override fun onCameraIdle() {
        // Get the location on camera camera move
        binding.imgLocationPinUp.visibility = View.VISIBLE
        mLatitide = mMap!!.cameraPosition.target.latitude
        mLongitude = mMap!!.cameraPosition.target.longitude

        setLocation(mLatitide, mLongitude)
        autoCompleteAddress=""

    }

    private fun mapInit(googleMap: GoogleMap) {
        googleMap.apply {
            val value = LatLng(mLatitide, mLongitude)
            addMarker(MarkerOptions().apply {
                position(value)
                draggable(false)
            })
            // setup zoom level
            animateCamera(CameraUpdateFactory.newLatLngZoom(value, 15f))
            // maps events we need to respond to
            setOnCameraMoveListener(this@SelectLocationFragment)
            setOnCameraIdleListener(this@SelectLocationFragment)
        }
    }


    private fun setLocation(lat: Double, lng: Double) {
        // get Address
        // get Address
        if (autoCompleteAddress.isNotBlank()) {
            mLocationName = autoCompleteAddress
        } else {
            mLocationName = GeneralFunctions.getAddress(lat, lng, requireContext())

        }
        binding.tvLocation.text = mLocationName

    }


    override fun onLocationChanged(p0: Location) {

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    private fun openPlacePicker() {
        if (!Places.isInitialized()) {
            Places.initialize(activityContext, GOOGLE_PLACES_API_KEY)
        }

        val fields = listOf(
            Place.Field.ID, Place.Field.NAME,
            Place.Field.ADDRESS, Place.Field.LAT_LNG
        )

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        ).build(activityContext)

        imageActivityResultLauncher.launch(
            intent
        )
    }

    private var imageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val place = Autocomplete.getPlaceFromIntent(intent)

                place.latLng?.let { latLng ->
                    mLatitide = latLng.latitude
                    mLongitude = latLng.longitude
                    mapInit(mMap!!)
                }
                autoCompleteAddress = place.address.toString()
                setLocation(mLatitide, mLongitude)
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvLocation -> {
                openPlacePicker()
            }

            R.id.btnSubmit -> {
                if (mUserType == BUNDLE_CUSTOMER) {
                    mViewModel.editProfile(
                        latitude = mLatitide,
                        longitude = mLongitude,
                        location = mLocationName
                    )
                } else {

                    // Send broadcast
                    requireContext().sendBroadcast(
                        Intent(INTENT_ENTER_DETAIL).putExtra(
                            INTENT_LOCATION_DATA, LocationData(
                                mLocationName, mLatitide, mLongitude
                            )
                        )
                    )
                    (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
                }
            }

            R.id.ivBack -> {
                (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
            }

        }
    }

    override fun onMapReady(mGoogleMap: GoogleMap) {
        mMap = mGoogleMap
    }

}
