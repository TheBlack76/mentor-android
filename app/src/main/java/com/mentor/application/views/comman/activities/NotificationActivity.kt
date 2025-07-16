package com.mentor.application.views.comman.activities

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.mentor.application.R
import com.mentor.application.databinding.ActivityMainBinding
import com.mentor.application.repository.models.NotificationData
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_ACCEPT_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_BOOKING_COMPLETE
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_CANCEL_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_NEW_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_RESCHEDULE_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_REVIEW_ADDED
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_SESSION_STARTED
import com.mentor.application.utils.ApplicationGlobal
import com.mentor.application.utils.ApplicationGlobal.Companion.CUSTOMER
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.customer.fragment.BookingDetailFragment
import com.mentor.application.views.vendor.fragments.BookingRequestDetailFragment
import com.mentor.application.views.vendor.fragments.NewRequestFragment
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.NEW_BOOKINGS
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.PAST_BOOKINGS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationActivity :
    BaseAppCompactActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    companion object {
        const val BUNDLE_NOTIFICATION_DATA = "notificationData"
        const val BUNDLE_PUSH_TYPE = "pushType"
    }

    private var mData = NotificationData()

    override val isMakeStatusBarTransparent: Boolean
        get() = false

    override fun init() {
        // Get intent
        mData = intent.getParcelableExtra(BUNDLE_NOTIFICATION_DATA) ?: NotificationData()

        GeneralFunctions.windowDecoder(binding.flFragContainerMain)


        when (intent?.getStringExtra(BUNDLE_PUSH_TYPE)) {
            PUSH_TYPE_NEW_BOOKING, PUSH_TYPE_RESCHEDULE_BOOKING -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        NEW_BOOKINGS,
                        mData.bookingId
                    )
                )
            }

            PUSH_TYPE_ACCEPT_BOOKING -> {
                doTransaction(
                    BookingDetailFragment.newInstance(
                        mData.bookingId,
                        BookingDetailFragment.UPCOMING_BOOKINGS,

                    )
                )
            }

            PUSH_TYPE_BOOKING_COMPLETE -> {
                if (ApplicationGlobal.mUserType == CUSTOMER) {
                    doTransaction(
                        BookingDetailFragment.newInstance(
                            mData.bookingId,
                            PAST_BOOKINGS,

                        )
                    )
                } else {
                    doTransaction(
                        BookingRequestDetailFragment.newInstance(
                            PAST_BOOKINGS,
                            mData.bookingId
                        )
                    )
                }

            }

            PUSH_TYPE_REVIEW_ADDED -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        PAST_BOOKINGS,
                        mData.bookingId
                    )
                )
            }

            PUSH_TYPE_SESSION_STARTED -> {
                if (mUserPrefsManager.loginUser?.userType == CUSTOMER) {
                    doTransaction(
                        BookingDetailFragment.newInstance(
                            mData.bookingId,
                            BookingDetailFragment.UPCOMING_BOOKINGS
                        )
                    )
                } else {
                    doTransaction(
                        BookingRequestDetailFragment.newInstance(
                            NewRequestFragment.UPCOMING_BOOKINGS,
                            mData.bookingId
                        )
                    )
                }

            }

            PUSH_TYPE_CANCEL_BOOKING -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        PAST_BOOKINGS,
                        mData.bookingId
                    )
                )
            }

            PUSH_TYPE_RESCHEDULE_BOOKING -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        NEW_BOOKINGS,
                        mData.bookingId
                    )
                )
            }

        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeActivity()
            }
        })
    }

    override val navHostFragment: NavHostFragment?
        get() = null

    private fun doTransaction(fragment: Fragment) {
        doFragmentTransaction(
            fragment = fragment,
            containerViewId = R.id.flFragContainerMain,
            isAddToBackStack = false
        )
    }


}
