package com.mentor.application.views.customer.fragment

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mentor.application.R
import com.mentor.application.databinding.FragmentBaseRecyclerViewBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.NotificationListing
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_ACCEPT_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_BOOKING_COMPLETE
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_CANCEL_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_NEW_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_REJECT_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_RESCHEDULE_BOOKING
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_REVIEW_ADDED
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_SESSION_STARTED
import com.mentor.application.services.MyFirebaseMessagingService.Companion.PUSH_TYPE_VENDOR_CANCEL_BOOKING
import com.mentor.application.utils.ApplicationGlobal.Companion.CUSTOMER
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.ServicesViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.adapters.NotificationAdapter
import com.mentor.application.views.customer.fragment.HomeFragment.Companion.INTENT_NOTIFICATION
import com.mentor.application.views.customer.interfaces.NotificationInterface
import com.mentor.application.views.vendor.fragments.BookingRequestDetailFragment
import com.mentor.application.views.vendor.fragments.NewRequestFragment
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.NEW_BOOKINGS
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.PAST_BOOKINGS
import com.swingby.app.views.fragments.base.BaseRecyclerViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class NotificationFragment :
    BaseRecyclerViewFragment<FragmentBaseRecyclerViewBinding>(FragmentBaseRecyclerViewBinding::inflate),
    NotificationInterface {

    private val mViewModel: ServicesViewModel by viewModels()

    @Inject
    lateinit var mNotificationAdapter: NotificationAdapter

    private var mPage = 0

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun setData(savedInstanceState: Bundle?) {
        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_notification)

        // Api call
        mViewModel.getNotification(mPage)
    }

    override val recyclerViewAdapter: RecyclerView.Adapter<*>?
        get() = mNotificationAdapter

    override val layoutManager: RecyclerView.LayoutManager?
        get() = LinearLayoutManager(requireContext())

    override val isShowRecyclerViewDivider: Boolean
        get() = false

    override val recyclerView: RecyclerView
        get() = binding.recyclerView

    override val tvNoData: TextView?
        get() = binding.tvNoData

    override val swipeRefreshLayout: SwipeRefreshLayout?
        get() = binding.swipeRefreshLayout

    override fun onPullDownToRefresh() {
        mPage = 0
        mViewModel.getNotification(mPage)
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetNotifications().observe(this, Observer {
            it?.let { it1 -> mNotificationAdapter.updateData(it1, mPage) }

            requireContext().sendBroadcast(Intent(INTENT_NOTIFICATION))

        })
    }


    override fun onItemClick(notificationData: NotificationListing) {
        when (notificationData.pushType) {
            PUSH_TYPE_NEW_BOOKING -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        NEW_BOOKINGS,
                        notificationData.bookingId
                    )
                )
            }

            PUSH_TYPE_ACCEPT_BOOKING, PUSH_TYPE_VENDOR_CANCEL_BOOKING, PUSH_TYPE_REJECT_BOOKING -> {
                doTransaction(
                    BookingDetailFragment.newInstance(
                        notificationData.bookingId,
                        BookingDetailFragment.UPCOMING_BOOKINGS
                    )
                )
            }

            PUSH_TYPE_BOOKING_COMPLETE -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        PAST_BOOKINGS,
                        notificationData.bookingId
                    )
                )
            }

            PUSH_TYPE_REVIEW_ADDED -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        PAST_BOOKINGS,
                        notificationData.bookingId
                    )
                )
            }

            PUSH_TYPE_SESSION_STARTED -> {
                if (mUserPrefsManager.loginUser?.userType == CUSTOMER) {
                    doTransaction(
                        BookingDetailFragment.newInstance(
                            notificationData.bookingId,
                            BookingDetailFragment.UPCOMING_BOOKINGS
                        )
                    )
                } else {
                    doTransaction(
                        BookingRequestDetailFragment.newInstance(
                            NewRequestFragment.UPCOMING_BOOKINGS,
                            notificationData.bookingId
                        )
                    )
                }

            }

            PUSH_TYPE_CANCEL_BOOKING -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        PAST_BOOKINGS,
                        notificationData.bookingId
                    )
                )
            }

            PUSH_TYPE_RESCHEDULE_BOOKING -> {
                doTransaction(
                    BookingRequestDetailFragment.newInstance(
                        NEW_BOOKINGS,
                        notificationData.bookingId
                    )
                )
            }
        }
    }

    override fun onLoadMore() {
        mPage++
        mViewModel.getNotification(mPage)
    }

    private fun doTransaction(fragment: Fragment) {
        (activityContext as BaseAppCompactActivity<*>).doFragmentTransaction(
            fragment = fragment,
            containerViewId = R.id.flFragContainerMain,
            enterAnimation = R.animator.slide_right_in_fade_in,
            exitAnimation = R.animator.scale_fade_out,
            popExitAnimation = R.animator.slide_right_out_fade_out
        )
    }


}
