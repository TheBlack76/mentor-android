package com.mentor.application.views.comman.activities

import android.content.Intent
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.NavHostFragment
import com.mentor.application.R
import com.mentor.application.databinding.ActivityMainBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.comman.fragments.MessageFragment
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_BOOKING
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_RESPONSE_TYPE
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_TYPE_REFRESH
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageActivity :
    BaseAppCompactActivity<ActivityMainBinding>(ActivityMainBinding::inflate){

    companion object {
        const val BUNDLE_BOOKING = "booking"
    }

    private var mBooking = Booking()

    override val isMakeStatusBarTransparent: Boolean
        get() = false

    override fun init() {
        // Get intent
        mBooking = intent.getParcelableExtra(BUNDLE_BOOKING) ?: Booking()

        GeneralFunctions.windowDecoder(binding.flFragContainerMain)

        doFragmentTransaction(
            fragment = MessageFragment.newInstance(mBooking._id, mBooking.professionalName),
            containerViewId = R.id.flFragContainerMain,
            isAddToBackStack = false,
        )

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeActivity()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Send broad cast
        sendBroadcast(
            Intent(INTENT_BOOKING).putExtra(
                INTENT_RESPONSE_TYPE, INTENT_TYPE_REFRESH
            )
        )
    }

    override val navHostFragment: NavHostFragment?
        get() = null


}
