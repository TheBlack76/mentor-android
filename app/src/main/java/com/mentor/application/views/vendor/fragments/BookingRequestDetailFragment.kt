package com.mentor.application.views.vendor.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mentor.application.R
import com.mentor.application.databinding.BottomsheetAcceptBookingBinding
import com.mentor.application.databinding.FragmentBookingRequestBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.BookingInfo
import com.mentor.application.repository.models.enumValues.BookingStatus
import com.mentor.application.repository.models.enumValues.BookingType
import com.mentor.application.utils.Constants
import com.mentor.application.utils.Constants.DATE_FORMAT_DISPLAY
import com.mentor.application.utils.Constants.DATE_SERVER_TIME
import com.mentor.application.utils.Constants.DATE_TIME_FORMAT_DISPLAY
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.BaseViewModel.ErrorHandler
import com.mentor.application.viewmodels.customer.BookingsViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.MessageActivity
import com.mentor.application.views.comman.activities.VideoSessionActivity
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BOOKING_TYPE_INSTANT
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BOOKING_TYPE_NON_INSTANT
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BUNDLE_BOOKING
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_BOOKING
import com.mentor.application.views.vendor.adapters.BookingRequestIssuesAdapter
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.ACCEPT
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.NEW_BOOKINGS
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.NEW_REQUEST_INTENT
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.REJECT
import com.swingby.app.views.fragments.base.BaseFragment
import com.trendy.app.utils.DialogUtils.CancelBookingDialogInterface
import com.trendy.app.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject


@AndroidEntryPoint
class BookingRequestDetailFragment :
    BaseFragment<FragmentBookingRequestBinding>(FragmentBookingRequestBinding::inflate),
    OnClickListener {

    companion object {
        const val BUNDLE_BOOKING_ID = "bookingId"
        const val BUNDLE_BOOKING_TYPE = "bookingTYpe"
        const val BUNDLE_BOOKING_REQ_TIME = "requestTime"
        const val BUNDLE_BOOKING_REQ_DURATION = "requestDuration"

        fun newInstance(
            viewType: String, bookingId: String, reqTime: String = "", requestDuration: String = ""
        ): BookingRequestDetailFragment {
            val args = Bundle()
            val fragment = BookingRequestDetailFragment()
            args.putString(BUNDLE_BOOKING_ID, bookingId)
            args.putString(BUNDLE_BOOKING_TYPE, viewType)
            args.putString(BUNDLE_BOOKING_REQ_TIME, reqTime)
            args.putString(BUNDLE_BOOKING_REQ_DURATION, requestDuration)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var mBookingRequestIssuesAdapter: BookingRequestIssuesAdapter

    private val mViewModel: BookingsViewModel by viewModels()

    private val mViewModelVendor: com.mentor.application.viewmodels.vendor.BookingsViewModel by viewModels()


    private var timersMap: CountDownTimer? = null
    private var bottomSheetDialog: BottomSheetDialog? = null


    private val sdfTime: SimpleDateFormat by lazy {
        SimpleDateFormat(
            DATE_FORMAT_DISPLAY, Locale.US
        )
    }
    private var mViewType = NEW_BOOKINGS
    private var mBookingId: String = ""
    private var mBooking = BookingInfo()
    private var mReqDuration = ""
    private var mReqTime = ""
    private var isSessionStart = false
    private var countDownTimer: CountDownTimer? = null
    private var isTimerFinish = false


    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_booking_detail)

        // Get arguments
        mViewType = arguments?.getString(BUNDLE_BOOKING_TYPE) ?: NEW_BOOKINGS
        mBookingId = arguments?.getString(BUNDLE_BOOKING_ID) ?: ""
        mReqTime = arguments?.getString(BUNDLE_BOOKING_REQ_TIME) ?: ""
        mReqDuration = arguments?.getString(BUNDLE_BOOKING_REQ_DURATION) ?: ""

        // Set adapter
        binding.rvIssues.adapter = mBookingRequestIssuesAdapter

        // Set click listener
        binding.ivChat.setOnClickListener(this)
        binding.tvJoin.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.ivRefresh.setOnClickListener(this)
        binding.btnReject.setOnClickListener(this)
        binding.acceptContainer.setOnClickListener(this)
        binding.btnCustom.setOnClickListener(this)

        // Api call
        mViewModel.getBookingInfo(mBookingId)

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetBookingInfo().observe(this, Observer {
            setDetail(it)
        })

        mViewModel.onBookingCancel().observe(this, Observer {
            // Api call
            mViewModel.getBookingInfo(mBookingId)
        })

        mViewModelVendor.isShowLoader.observe(this, Observer {
            if (it){
                showProgressLoader()
            }else{
                hideProgressLoader()
            }
        })

        mViewModelVendor.onGetAcceptBookingData().observe(this, Observer { (it, type) ->
            activityContext.sendBroadcast(Intent(NEW_REQUEST_INTENT))
            if (mBooking.bookingType == BookingType.INSTANT.value || type == REJECT) {
                (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()

                if (type == ACCEPT) {
                    bottomSheetDialog?.dismiss()
                    // Api call
                    DialogUtils.informationDialog(requireContext(),
                        "",
                        true,
                        layoutInflater = layoutInflater,
                        callBack = object : DialogUtils.InformationDialogInterface {
                            override fun onOkay() {
                            }

                        })
                }

            } else if (type == ACCEPT) {
                // Api call
                binding.btnReject.visibility = View.GONE
                binding.acceptContainer.visibility = View.GONE
                mViewModel.getBookingInfo(mBookingId)
            }

        })

    }

    private fun setDetail(data: BookingInfo) {
        binding.viewBg.visibility = View.GONE
        mBooking = data
        binding.tvBookingId.text = getString(R.string.booking_id, data.bookingId)

        if (data.bookingType == BookingType.INSTANT.value && mViewType == NEW_BOOKINGS) {
            startTimer()
        } else if (mViewType == NEW_BOOKINGS) {
            binding.btnCustom.visibility = View.GONE
            binding.acceptContainer.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(), R.color.colorPrimary
            )
            binding.buttonText.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.colorWhite
                )
            )
        } else {
            binding.btnCustom.visibility = View.GONE
            binding.btnReject.visibility = View.GONE
            binding.acceptContainer.visibility = View.GONE

        }

        if (data.bookingType == BookingType.INSTANT.value) {
            binding.rvIssues.visibility = View.INVISIBLE
            binding.tvInstantIssueNotes.visibility = View.VISIBLE
        } else {
            binding.rvIssues.visibility = View.VISIBLE
            binding.tvInstantIssueNotes.visibility = View.INVISIBLE
        }

        binding.sdvCustomerImage.setImageURI(
            GeneralFunctions.getUserImage(data.customerId.image)
        )

        binding.tvName.text = data.customerId.fullName
        binding.tvEmail.text = data.customerId.email
        isSessionStart = data.isSession

        binding.sdvServiceImage.setImageURI(data.subProfessionId.image)
        binding.tvServiceName.text = data.subProfessionId.subProfession

        if (data.bookingType == BookingType.INSTANT.value) {
            (GeneralFunctions.changeUtcToLocal(
                data.createdAt, Constants.DATE_FORMAT_SERVER, DATE_TIME_FORMAT_DISPLAY
            )).also { binding.sdvServiceDate.text = it }
        } else {
            (GeneralFunctions.changeDateFormat(
                data.date, Constants.DATE_FORMAT_SERVER, DATE_FORMAT_DISPLAY
            ) + "(" + GeneralFunctions.changeDateFormat(
                data.startTime, DATE_SERVER_TIME, Constants.DATE_DISPLAY_TIME + ")"
            )).also { binding.sdvServiceDate.text = it }
        }

        binding.tvPrice.text = "$${data.bookingAmount}"

        "-$${data.platformFee}".also { binding.tvPlatformFees.text = it }

        if (data.status == BookingStatus.REQUESTED.value && data.bookingType == BookingType.INSTANT.value) {
            binding.tvItemFee.text = "$${data.offeredPrice}"

        } else {
            binding.tvItemFee.text = "$${data.totalAmount}"
        }

        binding.tvInstantIssueNotes.text = data.description

        // Update adapter
        data.generalQuestions?.let { mBookingRequestIssuesAdapter.updateData(it) }

        // Update ratting
        if (data.reviews._id.isBlank()) {
            binding.tvMyReviews.visibility = View.GONE
            binding.ratingbar.visibility = View.GONE
            binding.tvReview.visibility = View.GONE
            binding.tvReviewDate.visibility = View.GONE
        } else {
            binding.tvMyReviews.visibility = View.VISIBLE
            binding.ratingbar.visibility = View.VISIBLE
            binding.tvReview.visibility = View.VISIBLE
            binding.tvReviewDate.visibility = View.VISIBLE
            binding.tvReview.text = data.reviews.message
            binding.ratingbar.rating = data.reviews.star.toFloat()
            binding.tvReviewDate.text = GeneralFunctions.changeUtcToLocal(
                data.reviews.createdAt, Constants.DATE_FORMAT_SERVER, DATE_FORMAT_DISPLAY
            )
        }

        // Check the booking status
        when (data.status) {
            BookingStatus.COMPLETED.value -> {
                binding.tvJoin.visibility = View.GONE
                binding.ivChat.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorGreen
                    )
                )

                binding.tvDate.text = getString(
                    R.string.st_completed_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt,
                        Constants.DATE_FORMAT_SERVER,
                        Constants.DATE_TIME_FORMAT_DISPLAY
                    )
                )
            }

            BookingStatus.ACCEPTED.value -> {
                binding.tvJoin.visibility = View.VISIBLE
                binding.ivChat.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.VISIBLE

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorGreen
                    )
                )

                binding.tvDate.text = getString(
                    R.string.st_accepted_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt,
                        Constants.DATE_FORMAT_SERVER,
                        Constants.DATE_TIME_FORMAT_DISPLAY
                    )
                )

                // Start booking date and time count
                val calender = Calendar.getInstance()
                if (GeneralFunctions.changeDateFormat(
                        data.date, Constants.DATE_FORMAT_SERVER, DATE_FORMAT_DISPLAY
                    ) == sdfTime.format(calender.time)
                ) {

                    startTimer(
                        GeneralFunctions.getCurrentDate("HH:mm:ss"),
                        data.startTime + ":00",
                        "HH:mm:ss"
                    )

                } else {
                    (getString(R.string.st_booking_start_on) + GeneralFunctions.changeDateFormat(
                        data.date, Constants.DATE_FORMAT_SERVER, DATE_FORMAT_DISPLAY
                    ) + ", " + GeneralFunctions.changeDateFormat(
                        data.startTime, DATE_SERVER_TIME, Constants.DATE_DISPLAY_TIME
                    )).also { binding.tvJoin.text = it }
                }
            }

            BookingStatus.ONGOING.value -> {
                binding.ivChat.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.GONE

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorGreen
                    )
                )

                binding.tvDate.text = getString(R.string.st_ongoing)

                // Start booking date and time count
                if (data.isSession) {
                    binding.tvJoin.text = getString(R.string.st_join_booking_session)
                    binding.tvJoin.visibility = View.VISIBLE
                } else {
                    binding.tvJoin.visibility = View.GONE
                }
            }

            BookingStatus.REQUESTED.value -> {
                binding.tvJoin.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorEditTextStroke
                    )
                )
                binding.tvDate.text = getString(
                    R.string.st_placed_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt,
                        Constants.DATE_FORMAT_SERVER,
                        Constants.DATE_TIME_FORMAT_DISPLAY
                    )
                )
            }

            BookingStatus.CANCELLED.value -> {
                binding.tvJoin.visibility = View.GONE
                binding.ivChat.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorRed
                    )
                )

                binding.tvDate.text = getString(
                    R.string.st_cancelled_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt, Constants.DATE_FORMAT_SERVER, DATE_TIME_FORMAT_DISPLAY
                    )
                )

            }

            BookingStatus.REJECTED.value -> {
                binding.tvJoin.visibility = View.GONE
                binding.ivChat.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.colorRed
                    )
                )

                binding.tvDate.text = getString(
                    R.string.st_rejected_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt,
                        Constants.DATE_FORMAT_SERVER,
                        Constants.DATE_TIME_FORMAT_DISPLAY
                    )
                )

            }

        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun startTimer(currentDate: String, endDate: String, dateTimeFormat: String) {
        val sdf = SimpleDateFormat(dateTimeFormat)
        try {
            val currentTime = sdf.parse(currentDate) ?: Date()
            val bulkTime = sdf.parse(endDate) ?: Date()

            countDownTimer = object : CountDownTimer(bulkTime.time - currentTime.time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    var different = millisUntilFinished
                    val secondsInMilli: Long = 1000
                    val minutesInMilli = secondsInMilli * 60
                    val hoursInMilli = minutesInMilli * 60
                    val daysInMilli = hoursInMilli * 24
                    different %= daysInMilli

                    val elapsedHours = different / hoursInMilli
                    different %= hoursInMilli

                    val elapsedMinutes = different / minutesInMilli
                    different %= minutesInMilli

                    val elapsedSeconds = different / secondsInMilli
                    if (elapsedHours.toInt() != 0) {
                        val mStartTime =
                            "${elapsedHours}h : ${elapsedMinutes}m : ${elapsedSeconds}s"
                        binding.tvJoin.text = getString(R.string.st_booking_start_in, mStartTime)

                    } else if (elapsedMinutes.toInt() != 0) {
                        val mStartTime = "${elapsedMinutes}m : ${elapsedSeconds}s"
                        binding.tvJoin.text = getString(R.string.st_booking_start_in, mStartTime)
                    } else {
                        val mStartTime = "${elapsedSeconds}s"
                        binding.tvJoin.text = getString(R.string.st_booking_start_in, mStartTime)
                    }
                }

                override fun onFinish() {
                    // Api call
                    if (!isTimerFinish) {
                        mViewModel.getBookingInfo(mBookingId, false)
                        isTimerFinish = true
                    }
                }
            }.start()

        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }

    // Function to start the refresh animation
    private fun startRefreshAnimation() {
        val animator = ObjectAnimator.ofFloat(binding.ivRefresh, "rotation", 0f, 360f)
        animator.duration = 500 // 1 second per rotation
        animator.repeatCount = 1
        animator.interpolator = LinearInterpolator()
        animator.start()

        // Api call
        mViewModel.getBookingInfo(mBookingId, false)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivChat -> {
                val booking = Booking(
                    _id = mBooking._id,
                    professionalName = mBooking.customerId.fullName,
                )
                startActivity(
                    Intent(requireContext(), MessageActivity::class.java).putExtra(
                        MessageActivity.BUNDLE_BOOKING, booking

                    )
                )
            }

            R.id.tvJoin -> {
                if (isSessionStart) {
                    val booking = Booking(
                        _id = mBooking._id,
                        professionalName = mBooking.customerId.fullName,
                    )

                    startActivity(
                        Intent(requireContext(), VideoSessionActivity::class.java).putExtra(
                            BUNDLE_BOOKING, booking

                        ).putExtra(
                            VideoSessionActivity.BOOKING_TYPE, BOOKING_TYPE_NON_INSTANT
                        )
                    )
                }
            }

            R.id.btnCancel -> {
                // Api call
                DialogUtils.cancelBookingDialog(requireContext(),
                    binding.tvName.text.toString().trim(),
                    false,
                    layoutInflater = layoutInflater,
                    callBack = object : CancelBookingDialogInterface {
                        override fun onCancel() {
                            // Api call
                            mViewModel.cancelBookingByVendor(mBookingId, true)
                        }

                        override fun onReschedule() {

                        }
                    })
            }

            R.id.acceptContainer -> {
                val amount = if (mBooking.bookingType == BookingType.INSTANT.value) {
                    mBooking.offeredPrice
                } else {
                    null
                }
                mViewModelVendor.bookingResponse(mBookingId, ACCEPT, amount)
            }

            R.id.btnReject -> {
                mViewModelVendor.bookingResponse(mBookingId, REJECT, null)
            }

            R.id.btnCustom -> {
                customOffer()
            }

            R.id.ivRefresh -> {
                startRefreshAnimation()
            }

        }
    }

    private fun customOffer() {
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
                val platformFee = (enteredAmount * mBooking.percentage.toInt()) / 100
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
            val amount = view.etName.text.toString()
            when {
                amount.isBlank() -> showMessage(
                    message = getString(R.string.st_empty_fare),
                    isShowSnackbarMessage = true, isError = true
                )

                amount.toDouble() <= 0 -> showMessage(
                    message = getString(R.string.st_invalid_fare),
                    isShowSnackbarMessage = true, isError = true
                )

                else -> {
                    mViewModelVendor.bookingResponse(
                        mBookingId,
                        ACCEPT,
                        view.etName.text.toString()
                    )
                }
            }


        }

        bottomSheetDialog?.setContentView(view.root)
        bottomSheetDialog?.show()
    }

    override fun onResume() {
        super.onResume()
        // Initialize receiver
        requireContext().registerReceiver(
            mGetUpdateDataBroadcastReceiver, IntentFilter(INTENT_BOOKING), Context.RECEIVER_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()

        countDownTimer?.cancel()
        timersMap?.cancel()
        // Initialize receiver
        requireContext().registerReceiver(
            mGetUpdateDataBroadcastReceiver, IntentFilter(INTENT_BOOKING), Context.RECEIVER_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomSheetDialog?.dismiss()
    }

    private fun startTimer() {
        val createdAtMillis = GeneralFunctions.getLocalMillisFromISOString(mReqTime)
        val endTimeMillis = createdAtMillis + (mReqDuration.toInt() * 1000)
        val remainingTime = (endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0)

        // **Cancel previous timer if exists**
        binding.progressBar.max = mReqDuration.toInt()
        if (remainingTime > 0) {
            timersMap = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000  // Convert to seconds
                    Log.e("durationLeft", "onTick: " + secondsRemaining)
                    binding.buttonText.text = "Accept in (${secondsRemaining}s)"
                    binding.progressBar.progress =
                        secondsRemaining.toInt() // Update progress in seconds
                }

                override fun onFinish() {
                    try {
                        binding.progressBar.progress = 0
                        (activityContext as BaseAppCompactActivity<*>).onBackPressed()
                    } catch (e: Exception) {
                        println(e)
                    }

                }
            }
            timersMap?.start()
        } else {
            binding.progressBar.progress = 0
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    // Api call
                    mViewModel.getBookingInfo(mBookingId, false)
                } catch (e: Exception) {
                    println(e)

                }

            }
        }
    }


}
