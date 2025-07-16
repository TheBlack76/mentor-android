package com.mentor.application.views.customer.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mentor.application.R
import com.mentor.application.databinding.BottomSheetIssueResolvedBinding
import com.mentor.application.databinding.FragmentBookingDetailBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.BookingInfo
import com.mentor.application.repository.models.enumValues.BookingStatus
import com.mentor.application.repository.models.enumValues.BookingType
import com.mentor.application.utils.Constants
import com.mentor.application.utils.Constants.DATE_FORMAT_DISPLAY
import com.mentor.application.utils.Constants.DATE_FORMAT_SERVER
import com.mentor.application.utils.Constants.DATE_SERVER_TIME
import com.mentor.application.utils.Constants.DATE_TIME_FORMAT_DISPLAY
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.MessageViewModel
import com.mentor.application.viewmodels.customer.BookingsViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.MessageActivity
import com.mentor.application.views.comman.activities.VideoSessionActivity
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BOOKING_TYPE_INSTANT
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BOOKING_TYPE_NON_INSTANT
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BUNDLE_BOOKING
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.adapters.UpcomingBookingAdapter
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.BOOKING_TYPE
import com.swingby.app.views.fragments.base.BaseFragment
import com.trendy.app.utils.DialogUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class BookingDetailFragment :
    BaseFragment<FragmentBookingDetailBinding>(FragmentBookingDetailBinding::inflate),
    OnClickListener {

    companion object {
        const val INTENT_BOOKING = "bookingIntent"
        const val INTENT_RESPONSE_TYPE = "responseType"
        const val INTENT_TYPE_REFRESH = 1

        const val BUNDLE_BOOKING_ID = "bookingId"
        const val BUNDLE_VIEW_TYPE = "viewType"
        const val UPCOMING_BOOKINGS = "upcoming"
        const val PAST_BOOKING = "past"

        const val BOOKING_STATUS_COMPLETE = 1
        const val BOOKING_STATUS_PROFESSIONAL_NOT_JOIN = 2
        const val BOOKING_STATUS_CUSTOMER_NOT_JOIN = 3
        const val BOOKING_STATUS_OTHER = 4

        fun newInstance(bookingId: String, viewType: String): BookingDetailFragment {
            val args = Bundle()
            val fragment = BookingDetailFragment()
            args.putString(BUNDLE_VIEW_TYPE, viewType)
            args.putString(BUNDLE_BOOKING_ID, bookingId)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: BookingsViewModel by viewModels()
    private val mMessageViewModel: MessageViewModel by viewModels()

    private var mViewType = UPCOMING_BOOKINGS
    private var mBookingId = ""
    private var mProfessionalId = ""
    private var mBooking = Booking()
    private var countDownTimer: CountDownTimer? = null
    private var isSessionStart = false
    private var isTimerFinish = false
    private var sessionStatus = BOOKING_STATUS_COMPLETE

    private val sdfTime: SimpleDateFormat by lazy {
        SimpleDateFormat(
            DATE_FORMAT_DISPLAY,
            Locale.US
        )
    }

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_booking_detail)

        // Get arguments
        arguments?.let {
            mBookingId = it.getString(BUNDLE_BOOKING_ID) ?: ""
            mViewType = it.getString(BUNDLE_VIEW_TYPE) ?: UPCOMING_BOOKINGS
        }

        binding.btnSubmit.setOnClickListener(this)
        binding.btnRate.setOnClickListener(this)
        binding.ivChat.setOnClickListener(this)
        binding.tvReschedule.setOnClickListener(this)
        binding.tvJoin.setOnClickListener(this)
        binding.tvEndSession.setOnClickListener(this)
        binding.ivRefresh.setOnClickListener(this)

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

        mMessageViewModel.onBookingComplete().observe(this) {
            // Api call
            mViewModel.getBookingInfo(mBookingId, false)
            if (isAdded) {
                AddReviewFragment.newInstance(mBooking)
                    .show(childFragmentManager, "")
            }
        }

        mMessageViewModel.isProgressShow.observe(this) {
            if (it) {
                mMyCustomLoader.showProgressDialog()
            } else {
                mMyCustomLoader.dismissProgressDialog()

            }
        }
    }

    private fun setDetail(data: BookingInfo) {
        binding.viewBg.visibility = View.GONE
        mBooking._id = data._id
        mBooking.professionalId = data.professionalId._id
        mBooking.professionalName = data.professionalId.fullName
        mBooking.professionalImage = data.professionalId.image
        mBooking.professionId = data.professionId
        mBooking.subProfessionId = data.subProfessionId
        mBooking.totalAmount = data.totalAmount
        mBooking.bookingType = data.bookingType
        mProfessionalId = data.professionalId._id
        isSessionStart = data.isSession

        binding.tvBookingId.text = getString(R.string.booking_id, data.bookingId)
        binding.sdvProfessionalImage.setImageURI(
            GeneralFunctions.getUserImage(data.professionalId.image)
        )

        if (data.professionalId.fullName.isBlank()) {
            binding.viewUserBg.visibility = View.GONE
            binding.sdvProfessionalImage.visibility = View.GONE
            binding.tvName.visibility = View.GONE
            binding.tvProfession.visibility = View.GONE
            binding.tvRating.visibility = View.GONE
            binding.ivChat.visibility = View.GONE

        } else {
            binding.tvName.text = data.professionalId.fullName
            "${data.professionalId.averageStars} | ${data.professionalId.experience} ${
                getString(
                    R.string.st_experience
                )
            }".also { binding.tvRating.text = it }

            binding.tvProfession.text = data.subProfessionId.subProfession
        }

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

        "$${data.totalAmount}".also { binding.tvPrice.text = it }

        // Update ratting
        if (data.reviews._id.isBlank()) {
            if (data.status == BookingStatus.COMPLETED.value) binding.btnRate.visibility =
                View.VISIBLE else binding.btnRate.visibility = View.GONE

            binding.tvMyReviews.visibility = View.GONE
            binding.ratingbar.visibility = View.GONE
            binding.tvReview.visibility = View.GONE
        } else {
            binding.btnRate.visibility = View.GONE
            binding.tvMyReviews.visibility = View.VISIBLE
            binding.ratingbar.visibility = View.VISIBLE
            binding.tvReview.visibility = View.VISIBLE
            binding.tvReview.text = data.reviews.message
            binding.ratingbar.rating = data.reviews.star.toFloat()
        }

        // Check booking status
        when (data.status) {
            BookingStatus.CANCELLED.value -> {
                binding.tvJoin.visibility = View.GONE
                binding.tvEndSession.visibility = View.GONE
                binding.ivChat.visibility = View.GONE
                binding.tvReschedule.visibility = View.GONE
                binding.tvRefund.visibility = View.VISIBLE

                if (data.bookingType == BookingType.NON_INSTANT.value) {
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.btnSubmit.text = getString(R.string.st_book_again)
                    binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.colorPrimary
                    )

                }

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorRed
                    )
                )
                binding.tvDate.text = getString(
                    R.string.st_cancelled_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt,
                        DATE_FORMAT_SERVER,
                        DATE_TIME_FORMAT_DISPLAY
                    )
                )

            }

            BookingStatus.REJECTED.value -> {
                binding.tvJoin.visibility = View.GONE
                binding.tvEndSession.visibility = View.GONE
                binding.ivChat.visibility = View.GONE
                binding.tvReschedule.visibility = View.GONE
                binding.tvRefund.visibility = View.VISIBLE

                if (data.bookingType == BookingType.NON_INSTANT.value) {
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.btnSubmit.text = getString(R.string.st_book_again)
                    binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.colorPrimary
                    )

                }
                binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.colorPrimary
                )

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorRed
                    )
                )

                binding.tvDate.text = getString(
                    R.string.st_rejected_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt,
                        DATE_FORMAT_SERVER,
                        DATE_TIME_FORMAT_DISPLAY
                    )
                )
            }

            BookingStatus.COMPLETED.value -> {
                binding.tvJoin.visibility = View.GONE
                binding.tvEndSession.visibility = View.GONE
                binding.ivChat.visibility = View.GONE
                binding.tvReschedule.visibility = View.GONE
                if (data.bookingType == BookingType.NON_INSTANT.value) {
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.btnSubmit.text = getString(R.string.st_book_again)
                    binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.colorPrimary
                    )

                }

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorGreen
                    )
                )
                binding.tvDate.text = getString(
                    R.string.st_completed_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt,
                        DATE_FORMAT_SERVER,
                        DATE_TIME_FORMAT_DISPLAY
                    )
                )
            }


            BookingStatus.ACCEPTED.value -> {
                binding.tvJoin.visibility = View.VISIBLE
                binding.ivChat.visibility = View.VISIBLE
                binding.btnSubmit.visibility = View.VISIBLE
                binding.tvReschedule.visibility = View.VISIBLE

                binding.btnSubmit.text = getString(R.string.st_cancel_booking)
                binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.colorRed
                )

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorGreen
                    )
                )
                binding.tvDate.text = getString(
                    R.string.st_accepted_on, GeneralFunctions.changeUtcToLocal(
                        data.updatedAt,
                        DATE_FORMAT_SERVER,
                        DATE_TIME_FORMAT_DISPLAY
                    )
                )

                // Start booking date and time count
                val calender = Calendar.getInstance()
                if (GeneralFunctions.changeDateFormat(
                        data.date,
                        DATE_FORMAT_SERVER,
                        DATE_FORMAT_DISPLAY
                    ) == sdfTime.format(calender.time)
                ) {

                    startTimer(
                        GeneralFunctions.getCurrentDate("HH:mm:ss"),
                        data.startTime + ":00",
                        "HH:mm:ss"
                    )

                } else {
                    (getString(R.string.st_booking_start_on) +
                            GeneralFunctions.changeDateFormat(
                                data.date,
                                Constants.DATE_FORMAT_SERVER,
                                Constants.DATE_FORMAT_DISPLAY
                            ) + ", " + GeneralFunctions.changeDateFormat(
                        data.startTime,
                        DATE_SERVER_TIME,
                        Constants.DATE_DISPLAY_TIME
                    )).also { binding.tvJoin.text = it }
                }
            }

            BookingStatus.ONGOING.value -> {
                binding.ivChat.visibility = View.VISIBLE
                binding.btnSubmit.visibility = View.GONE
                binding.tvReschedule.visibility = View.GONE
                binding.tvEndSession.visibility = View.VISIBLE

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorGreen
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
                if (data.professionalId.fullName.isBlank()) {
                    binding.ivChat.visibility = View.GONE
                } else {
                    binding.ivChat.visibility = View.VISIBLE
                }
                binding.tvEndSession.visibility = View.GONE
                binding.btnSubmit.visibility = View.VISIBLE
                binding.btnSubmit.text = getString(R.string.st_cancel_booking)
                binding.tvReschedule.visibility = View.VISIBLE
                binding.btnSubmit.backgroundTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.colorRed
                )

                binding.tvDate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorEditTextStroke
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
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun startTimer(currentDate: String, endDate: String, dateTimeFormat: String) {
        Log.e("referfer", "startTimer: "+currentDate+"::"+endDate+"::"+dateTimeFormat )
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                if (binding.btnSubmit.text.toString().trim() == getString(R.string.st_book_again)) {
                    doTransaction(
                        BookNowFragment.newInstance(
                            Booking(), mProfessionalId,
                            mBooking.professionId._id,
                            mBooking.subProfessionId._id,
                            mBooking.professionalName
                        )
                    )
                } else {
                    // Api call
                    DialogUtils.cancelBookingDialog(
                        requireContext(),
                        binding.tvName.text.toString().trim(),
                        isReschedule = true,
                        layoutInflater = layoutInflater,
                        callBack = object : DialogUtils.CancelBookingDialogInterface {
                            override fun onCancel() {
                                // Api call
                                mViewModel.cancelBooking(mBookingId)
                            }
                            override fun onReschedule() {
                                doTransaction(
                                    BookNowFragment.newInstance(
                                        mBooking,
                                        mProfessionalId
                                    )
                                )
                            }

                        })
                }
            }

            R.id.btnRate -> {
                AddReviewFragment.newInstance(mBooking).show(childFragmentManager, "")
            }

            R.id.ivChat -> {
                startActivity(
                    Intent(requireContext(), MessageActivity::class.java).putExtra(
                        MessageActivity.BUNDLE_BOOKING, mBooking
                    )
                )
            }

            R.id.tvReschedule -> {
                doTransaction(BookNowFragment.newInstance(mBooking, mProfessionalId))
            }

            R.id.tvEndSession -> {
                showFilterOptionsBottomSheet()
            }

            R.id.ivRefresh -> {
                startRefreshAnimation()
            }

            R.id.tvJoin -> {
                if (isSessionStart) {
                    startActivity(
                        Intent(requireContext(), VideoSessionActivity::class.java).putExtra(
                            BUNDLE_BOOKING, mBooking

                        ).putExtra(
                            BOOKING_TYPE, BOOKING_TYPE_NON_INSTANT
                        )
                    )
                }
            }
        }
    }

    fun doTransaction(fragment: Fragment) {
        (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
            fragment = fragment,
            containerViewId = R.id.flFragContainerMain,
            enterAnimation = R.animator.slide_right_in_fade_in,
            exitAnimation = R.animator.scale_fade_out,
            popExitAnimation = R.animator.slide_right_out_fade_out
        )
    }

    private fun showFilterOptionsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.TransparentDialog)
        val view = BottomSheetIssueResolvedBinding.inflate(layoutInflater)

        view.ivCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        view.cbResolved.setOnClickListener {
            view.cbResolved.isChecked = true
            view.cbServiceProviderNotJoin.isChecked = false
            view.cbCustomerNotJoin.isChecked = false
            view.cbOther.isChecked = false
            view.etOther.visibility = View.GONE
            sessionStatus = BOOKING_STATUS_COMPLETE
        }

        view.cbServiceProviderNotJoin.setOnClickListener {
            view.cbResolved.isChecked = false
            view.cbServiceProviderNotJoin.isChecked = true
            view.cbCustomerNotJoin.isChecked = false
            view.cbOther.isChecked = false
            view.etOther.visibility = View.GONE
            sessionStatus = BOOKING_STATUS_PROFESSIONAL_NOT_JOIN
        }

        view.cbCustomerNotJoin.setOnClickListener {
            view.cbResolved.isChecked = false
            view.cbServiceProviderNotJoin.isChecked = false
            view.cbCustomerNotJoin.isChecked = true
            view.cbOther.isChecked = false
            view.etOther.visibility = View.GONE
            sessionStatus = BOOKING_STATUS_CUSTOMER_NOT_JOIN
        }

        view.cbOther.setOnClickListener {
            view.cbResolved.isChecked = false
            view.cbServiceProviderNotJoin.isChecked = false
            view.cbCustomerNotJoin.isChecked = false
            view.cbOther.isChecked = true
            view.etOther.visibility = View.VISIBLE
            sessionStatus = BOOKING_STATUS_OTHER
        }

        view.btnSubmit.setOnClickListener {
            // Api call
            mMessageViewModel.completeBooking(
                mBooking._id,
                sessionStatus,
                view.etOther.text.toString().trim()
            )
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view.root)
        bottomSheetDialog.show()
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

    override fun onResume() {
        super.onResume()
        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(INTENT_BOOKING), Context.RECEIVER_EXPORTED
            )
    }

    override fun onPause() {
        super.onPause()
        countDownTimer?.cancel()
        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(INTENT_BOOKING), Context.RECEIVER_EXPORTED
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    if (p1?.getIntExtra(
                            INTENT_RESPONSE_TYPE,
                            INTENT_TYPE_REFRESH
                        ) == INTENT_TYPE_REFRESH
                    ) {
                        // Api call
                        mViewModel.getBookingInfo(mBookingId, false)
                    }
                } catch (e: Exception) {
                    println(e)

                }

            }
        }
    }

}
