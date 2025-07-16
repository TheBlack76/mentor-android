package com.mentor.application.views.customer.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mentor.application.R
import com.mentor.application.databinding.BottomsheetConfirmBookingBinding
import com.mentor.application.databinding.FragmentBookNowBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.TimeSlot
import com.mentor.application.utils.Constants
import com.mentor.application.utils.Constants.DATE_DISPLAY_TIME
import com.mentor.application.utils.Constants.DATE_FORMAT_DISPLAY
import com.mentor.application.utils.Constants.DATE_SERVER_TIME
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.ServicesViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.adapters.TimeSlotsAdapter
import com.mentor.application.views.customer.adapters.TimeSlotsInterFace
import com.mentor.application.views.customer.fragment.DashboardFragment.Companion.BUNDLE_TAB_BOOKINGS
import com.mentor.application.views.customer.fragment.DashboardFragment.Companion.BUNDLE_TAB_NUMBER
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Named
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class BookNowFragment :
    BaseFragment<FragmentBookNowBinding>(FragmentBookNowBinding::inflate),
    OnClickListener, TimeSlotsInterFace {

    companion object {
        const val BUNDLE_BOOKING_ID = "bookingId"
        const val BUNDLE_PROFESSIONAL_ID = "professionalId"
        const val BUNDLE_PROFESSION_ID = "professionId"
        const val BUNDLE_SUB_PROFESSION_ID = "subProfessionId"
        const val BUNDLE_PROFESSIONAL_NAME = "professionalName"

        fun newInstance(
            booking: Booking,
            professionalId: String,
            professionId: String = "",
            subProfessionId: String = "",
            professionalName: String = ""
        ): BookNowFragment {
            val args = Bundle()
            val fragment = BookNowFragment()
            args.putParcelable(BUNDLE_BOOKING_ID, booking)
            args.putString(BUNDLE_PROFESSIONAL_ID, professionalId)
            args.putString(BUNDLE_PROFESSION_ID, professionId)
            args.putString(BUNDLE_SUB_PROFESSION_ID, subProfessionId)
            args.putString(BUNDLE_PROFESSIONAL_NAME, professionalName)
            fragment.arguments = args
            return fragment
        }

        var professionId = ""
        var subProfessionId = ""
        var professionalId = ""
        var professionalName = ""
    }

    private val mViewModel: ServicesViewModel by viewModels()

    private val mTimeSlotsAdapter30Min = TimeSlotsAdapter(this)

    private val mTimeSlotsAdapter60Min = TimeSlotsAdapter(this)

    private val mTimeSlotsAdapter90Min = TimeSlotsAdapter(this)

    private var mDate = ""
    private var mTimeSlot = TimeSlot()
    private var mBooking = Booking()

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // get argument
        mBooking = arguments?.getParcelable(BUNDLE_BOOKING_ID) ?: Booking()
        professionalId = arguments?.getString(BUNDLE_PROFESSIONAL_ID) ?: ""
        professionId = arguments?.getString(BUNDLE_PROFESSION_ID) ?: ""
        subProfessionId = arguments?.getString(BUNDLE_SUB_PROFESSION_ID) ?: ""
        professionalName = arguments?.getString(BUNDLE_PROFESSIONAL_NAME) ?: ""

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_book_now)

        // Set adapter
        binding.rv30Min.adapter = mTimeSlotsAdapter30Min
        binding.rv60Min.adapter = mTimeSlotsAdapter60Min
        binding.rv90Min.adapter = mTimeSlotsAdapter90Min

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)
        binding.tvDate.setOnClickListener(this)

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.isShowSwipeRefreshLayout().observe(this, Observer {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })

        mViewModel.onGetSlots().observe(this, Observer {
            if (it?.slots?.thirtyMin?.isEmpty() == true && it.slots.sixtyMin?.isEmpty() == true && it.slots.ninetyMin?.isEmpty() == true) {
                binding.rv30Min.visibility = View.GONE
                binding.tv30MinSlot.visibility = View.GONE
                binding.rv60Min.visibility = View.GONE
                binding.tv60MinSlot.visibility = View.GONE
                binding.rv90Min.visibility = View.GONE
                binding.tv90MinSlot.visibility = View.GONE
                binding.ivNoData.visibility = View.GONE
                binding.ivNoData.visibility = View.VISIBLE
            } else {
                binding.rv30Min.visibility = View.VISIBLE
                binding.tv30MinSlot.visibility =
                    if (it?.slots?.thirtyMin?.isNotEmpty() == true) View.VISIBLE else View.GONE
                binding.rv60Min.visibility = View.VISIBLE
                binding.tv60MinSlot.visibility =
                    if (it?.slots?.sixtyMin?.isNotEmpty() == true) View.VISIBLE else View.GONE
                binding.rv90Min.visibility = View.VISIBLE
                binding.tv90MinSlot.visibility =
                    if (it?.slots?.ninetyMin?.isNotEmpty() == true) View.VISIBLE else View.GONE
                binding.ivNoData.visibility = View.GONE
                binding.ivNoData.visibility = View.GONE

                // Update adapter
                mTimeSlotsAdapter30Min.updateData(it.slots.thirtyMin!!)
                mTimeSlotsAdapter60Min.updateData(it.slots.sixtyMin!!)
                mTimeSlotsAdapter90Min.updateData(it.slots.ninetyMin!!)
            }
        })

        mViewModel.onBookingConfirm().observe(this, Observer {
            showMessage(null, getString(R.string.st_your_booking_has_been_updated), true)
            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(BUNDLE_TAB_NUMBER, BUNDLE_TAB_BOOKINGS)
            startActivity(intent)
        })
    }

    private fun showDatePickerDialog() {
        // Get the current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create the DatePickerDialog
        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Update the TextView with the selected date
                mDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                binding.tvDate.text = mDate

                // Api Call
                mViewModel.getSlots(professionalId, mBooking._id, mDate)

            }, year, month, day)


        // Show the DatePickerDialog
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun showFilterOptionsBottomSheet(bookingData: Booking) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.TransparentDialog)
        val view = BottomsheetConfirmBookingBinding.inflate(layoutInflater)

        view.tvDate.text = GeneralFunctions
            .changeDateFormat(mDate, Constants.REQUEST_DATE_FORMAT_SERVER, DATE_FORMAT_DISPLAY)
        view.tvTime.text = GeneralFunctions.changeDateFormat(
            mTimeSlot.startTime, DATE_SERVER_TIME, DATE_DISPLAY_TIME
        )
        view.tvCategory.text = bookingData.professionId.profession
        view.tvSubCategory.text = bookingData.subProfessionId.subProfession
        "$${bookingData.totalAmount}".also { view.tvTotalPrice.text = it }

        view.ivCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        view.btnSubmit.setOnClickListener {
            // Api call
            mViewModel.reScheduleBooking(
                mBooking._id,
                mTimeSlot.startTime,
                mTimeSlot.endTime,
                mDate
            )
        }

        bottomSheetDialog.setContentView(view.root)
        bottomSheetDialog.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                when {
                    mTimeSlot.startTime
                        .isBlank() -> showMessage(
                        null,
                        getString(R.string.st_please_select_booking_date_and_time_slot),
                        isShowSnackbarMessage = true,
                        isError = true
                    )

                    else -> {
                        if (mBooking._id.isBlank()) {
                            (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                                fragment = HelpUsServiceFragment.newInstance(
                                    mDate, mTimeSlot
                                ),
                                containerViewId = R.id.flFragContainerMain,
                                enterAnimation = R.animator.slide_right_in_fade_in,
                                exitAnimation = R.animator.scale_fade_out,
                                popExitAnimation = R.animator.slide_right_out_fade_out
                            )
                        } else {
                            showFilterOptionsBottomSheet(mBooking)

                        }

                    }
                }

            }

            R.id.tvDate -> {
                showDatePickerDialog()
            }
        }
    }

    override fun onItemClick(time: TimeSlot) {
        mTimeSlot = time
        mTimeSlotsAdapter30Min.onTimeSelected(time)
        mTimeSlotsAdapter60Min.onTimeSelected(time)
        mTimeSlotsAdapter90Min.onTimeSelected(time)

    }

}
