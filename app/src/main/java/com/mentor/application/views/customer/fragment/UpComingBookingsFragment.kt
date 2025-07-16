package com.mentor.application.views.customer.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mentor.application.R
import com.mentor.application.databinding.FragmentChildMyBookingsBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.BookingsViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.adapters.UpcomingBookingAdapter
import com.mentor.application.views.customer.interfaces.BookingsInterface
import com.swingby.app.views.fragments.base.BaseRecyclerViewFragment
import com.trendy.app.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class UpComingBookingsFragment :
    BaseRecyclerViewFragment<FragmentChildMyBookingsBinding>(FragmentChildMyBookingsBinding::inflate),
    BookingsInterface ,OnClickListener{

    companion object {
        const val UPCOMING_BOOKING_INTENT = "upcomingBookingIntent"
        const val BUNDLE_BOOKING_TYPE = "bookingType"
        const val UPCOMING_BOOKINGS = "upcoming"
        const val PAST_BOOKING = "past"

        fun newInstance(bookingType: String): UpComingBookingsFragment {
            val args = Bundle()
            val fragment = UpComingBookingsFragment()
            args.putString(BUNDLE_BOOKING_TYPE, bookingType)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: BookingsViewModel by viewModels()
    private var mPage = 0
    private var isShowLoader = true
    private var mBookingType = UPCOMING_BOOKINGS

    @Inject
    lateinit var mUpcomingBookingAdapter: UpcomingBookingAdapter

    override val toolbar: ToolbarBinding?
        get() = null

    override fun setData(savedInstanceState: Bundle?) {
        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(UPCOMING_BOOKING_INTENT), Context.RECEIVER_EXPORTED
            )

        // Get arguments
        mBookingType = arguments?.getString(BUNDLE_BOOKING_TYPE) ?: UPCOMING_BOOKINGS

        // Set click listener
        binding.btnLogin.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        // Check user login
        if (mUserPrefsManager.isLogin) {
            binding.btnLogin.visibility=View.GONE
            binding.ivGuestBg.visibility=View.GONE
            mViewModel.getBookings(mBookingType, mPage, isShowLoader)
            isShowLoader = false
        } else {
            binding.btnLogin.visibility=View.VISIBLE
            binding.ivGuestBg.visibility=View.VISIBLE
            binding.refreshLayout.isEnabled = false
        }
    }

    override val recyclerViewAdapter: RecyclerView.Adapter<*>?
        get() = mUpcomingBookingAdapter

    override val layoutManager: RecyclerView.LayoutManager?
        get() = LinearLayoutManager(requireContext())

    override val isShowRecyclerViewDivider: Boolean
        get() = false

    override val recyclerView: RecyclerView
        get() = binding.recyclerView

    override val tvNoData: TextView?
        get() = binding.tvNoData

    override val swipeRefreshLayout: SwipeRefreshLayout?
        get() = binding.refreshLayout

    override fun onPullDownToRefresh() {
        mPage = 0
        mViewModel.getBookings(mBookingType, mPage)

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetBookings().observe(this, Observer {
            it?.let { it1 -> mUpcomingBookingAdapter.updateData(it1, mPage) }
        })

        mViewModel.onBookingCancel().observe(this, Observer {
            mPage = 0
            mViewModel.getBookings(mBookingType, mPage)
        })
    }

    override fun onItemClick(bookingId: String) {
        (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
            fragment = BookingDetailFragment.newInstance(bookingId, mBookingType),
            containerViewId = R.id.flFragContainerMain,
            enterAnimation = R.animator.slide_right_in_fade_in,
            exitAnimation = R.animator.scale_fade_out,
            popExitAnimation = R.animator.slide_right_out_fade_out
        )
    }

    override fun onCancelClick(booking: Booking, professionalId: String) {
        // Api call
        DialogUtils.cancelBookingDialog(
            requireContext(),
            "",
            true,
            layoutInflater = layoutInflater,
            callBack = object : DialogUtils.CancelBookingDialogInterface {
                override fun onCancel() {
                    // Api call
                    mViewModel.cancelBooking(booking._id)
                }

                override fun onReschedule() {
                    (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                        fragment = BookNowFragment.newInstance(booking, professionalId),
                        containerViewId = R.id.flFragContainerMain,
                        enterAnimation = R.animator.slide_right_in_fade_in,
                        exitAnimation = R.animator.scale_fade_out,
                        popExitAnimation = R.animator.slide_right_out_fade_out
                    )

                }

            })
    }

    override fun onLoadMore() {
        mPage++
        mViewModel.getBookings(mBookingType, mPage, false)
    }

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    // Api call
                    mPage = 0
                    mViewModel.getBookings(mBookingType, mPage)
                } catch (e: Exception) {
                    println(e)

                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnLogin->{
                navigateToMainActivity()
            }
        }
    }

}
