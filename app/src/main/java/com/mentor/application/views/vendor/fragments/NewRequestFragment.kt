package com.mentor.application.views.vendor.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mentor.application.R
import com.mentor.application.databinding.BottomsheetAcceptBookingBinding
import com.mentor.application.databinding.FragmentNewRequestBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.BookingRequest
import com.mentor.application.repository.models.enumValues.BookingType
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.vendor.BookingsViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.VideoSessionActivity
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BOOKING_SESSION
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BOOKING_TYPE_INSTANT
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BUNDLE_BOOKING
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.fragment.BookNowFragment
import com.mentor.application.views.vendor.adapters.NewRequestAdapter
import com.mentor.application.views.vendor.interfaces.NewRequestInterface
import com.swingby.app.views.fragments.base.BaseRecyclerViewFragment
import com.trendy.app.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class NewRequestFragment :
    BaseRecyclerViewFragment<FragmentNewRequestBinding>(FragmentNewRequestBinding::inflate),
    NewRequestInterface {

    companion object {
        const val NEW_REQUEST_INTENT = "newRequestIntent"
        const val NEW_BOOKINGS = "new"
        const val UPCOMING_BOOKINGS = "upcoming"
        const val PAST_BOOKINGS = "past"
        const val BOOKING_TYPE = "bookingType"
        const val REJECT = "reject"
        const val ACCEPT = "accept"

        fun newInstance(bookingTYpe: String): NewRequestFragment {
            val args = Bundle()
            val fragment = NewRequestFragment()
            args.putString(BOOKING_TYPE, bookingTYpe)
            fragment.arguments = args
            return fragment
        }
    }

    private var mBookingType = NEW_BOOKINGS
    private var mPage = 0
    private var isShowLoader = true
    private val mViewModel: BookingsViewModel by viewModels()
    private var isInstant = false
    private var bottomSheetDialog: BottomSheetDialog? = null


    @Inject
    lateinit var mNewRequestAdapter: NewRequestAdapter

    override val toolbar: ToolbarBinding?
        get() = null

    override fun setData(savedInstanceState: Bundle?) {

        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(NEW_REQUEST_INTENT), Context.RECEIVER_EXPORTED
            )

        // Get arguments
        mBookingType = arguments?.getString(BOOKING_TYPE) ?: NEW_BOOKINGS
    }

    override fun onResume() {
        super.onResume()
        // Api call
        mViewModel.getBookings(mBookingType, mPage, isShowLoader)
        isShowLoader = false
    }

    override val recyclerViewAdapter: RecyclerView.Adapter<*>?
        get() = mNewRequestAdapter

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
            it?.let { it1 -> mNewRequestAdapter.updateData(it1, mPage, mBookingType) }
        })

        mViewModel.onGetBookingResponse().observe(this, Observer {
            mPage = 0
            mViewModel.getBookings(NEW_BOOKINGS, mPage)
        })

        mViewModel.onBookingResponseError().observe(this, Observer {
            mPage = 0
            mViewModel.getBookings(NEW_BOOKINGS, mPage)
        })

        mViewModel.onGetAcceptBookingData().observe(this, Observer { (it, type) ->
            if (isInstant && type == ACCEPT) {
                // Api call
                bottomSheetDialog?.dismiss()
                DialogUtils.informationDialog(
                    requireContext(),
                    "",
                    true,
                    layoutInflater = layoutInflater,
                    callBack = object : DialogUtils.InformationDialogInterface {
                        override fun onOkay() {
                        }

                    })
            }

            mPage = 0
            mViewModel.getBookings(NEW_BOOKINGS, mPage)

        })

    }

    override fun onItemClick(bookingId: String, requestSentTime: String, requestDurationTime: Int) {
        (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
            fragment = BookingRequestDetailFragment.newInstance(
                mBookingType, bookingId,
                requestSentTime, requestDurationTime.toString()
            ),
            containerViewId = R.id.flFragContainerMain,
            enterAnimation = R.animator.slide_right_in_fade_in,
            exitAnimation = R.animator.scale_fade_out,
            popExitAnimation = R.animator.slide_right_out_fade_out
        )
    }

    override fun onAccept(booking: BookingRequest) {
        val amount =
            if (booking.bookingType == BookingType.INSTANT.value) {
                booking.offeredPrice
            } else {
                null
            }
        isInstant = booking.bookingType == BookingType.INSTANT.value
        mViewModel.bookingResponse(booking._id, ACCEPT, amount)
    }

    override fun onReject(booking: BookingRequest) {
        mViewModel.bookingResponse(booking._id, REJECT, null)
    }

    override fun onCustomOffered(bookingId: BookingRequest) {
        bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.TransparentDialog)
        val view = BottomsheetAcceptBookingBinding.inflate(layoutInflater)

        view.bottomSheetLayout.viewTreeObserver?.addOnGlobalLayoutListener {
            val rect = Rect()
            view.bottomSheetLayout.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.bottomSheetLayout.rootView.height
            val keyboardHeight = screenHeight - rect.bottom

            if (keyboardHeight > screenHeight * 0.15) {
                // Keyboard is open
                view.bottomSheetLayout.setPadding(0, 0, 0, keyboardHeight + 100)
            } else {
                view.bottomSheetLayout.setPadding(0, 0, 0, 40)
            }
        }


        view.etName.doAfterTextChanged { text ->
            val enteredAmount = text.toString().toDoubleOrNull()

            if (enteredAmount != null) {
                val platformFee = (enteredAmount * bookingId.percentage.toInt()) / 100
                val finalAmount = enteredAmount - platformFee

                "-$${"%.2f".format(platformFee)}".also { view.tvPlatformFees.text = it }
                "$${"%.2f".format(finalAmount)}".also { view.tvPrice.text = it }
            } else {
                "-$--".also { view.tvPlatformFees.text = it }
                "$--".also { view.tvPrice.text = it }
            }
        }


        view.ivCancel.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }

        view.btnSubmit.setOnClickListener {
            isInstant = bookingId.bookingType == BookingType.INSTANT.value
            mViewModel.bookingResponse(bookingId._id, ACCEPT, view.etName.text.toString())
        }

        bottomSheetDialog?.setContentView(view.root)
        bottomSheetDialog?.show()
    }

    override fun onRefresh() {
        mPage = 0
        mViewModel.getBookings(NEW_BOOKINGS, mPage, false)
        bottomSheetDialog?.dismiss()
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

}
