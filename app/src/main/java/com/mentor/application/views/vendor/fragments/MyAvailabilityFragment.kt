package com.mentor.application.views.vendor.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentMyAvailabilityBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.AvailabilityData
import com.mentor.application.repository.models.TimeSlot
import com.mentor.application.utils.Constants.REQUEST_DATE_FORMAT_SERVER
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.vendor.PersonalisationViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.fragment.ProfileFragment.Companion.INTENT_PROFILE
import com.mentor.application.views.vendor.adapters.AvailableSlotsAdapter
import com.mentor.application.views.vendor.fragments.EditAvailabilityFragment.Companion.BUNDLE_VIEW_TYPE_CREATE
import com.mentor.application.views.vendor.fragments.EditAvailabilityFragment.Companion.BUNDLE_VIEW_TYPE_EDIT

import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class MyAvailabilityFragment :
    BaseFragment<FragmentMyAvailabilityBinding>(FragmentMyAvailabilityBinding::inflate),
    OnClickListener {

    companion object {
        const val INTENT_AVAILABILITY = "availabilityIntent"
    }

    private val mViewModel: PersonalisationViewModel by viewModels()

    @Inject
    lateinit var mAvailableSlotsAdapter: AvailableSlotsAdapter

    private var mSelectedDate = ""

    private var mAvailabilityData = AvailabilityData()


    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_my_availability)

        // Set recyclerview
        binding.recyclerView.adapter = mAvailableSlotsAdapter


        // Set click listener
        binding.tvEditSlots.setOnClickListener(this)
        binding.ivAdd.setOnClickListener(this)

        // Calender view
        binding.calenderView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Note: month is zero-based (0 for January, 1 for February, etc.)
            mSelectedDate = "$year-${month + 1}-$dayOfMonth"

            // Api call
            mViewModel.getAvailability(mSelectedDate)

        }

        // Get the default date as timestamp in milliseconds
        val defaultDateInMillis = binding.calenderView.date

        // Convert timestamp to a readable date format
        val dateFormat = SimpleDateFormat(REQUEST_DATE_FORMAT_SERVER, Locale.getDefault())
        mSelectedDate = dateFormat.format(Date(defaultDateInMillis))

        // Api call
        mViewModel.getAvailability(mSelectedDate)


    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.isShowSwipeRefreshLayout().observe(this, Observer {
            if (it) {
                binding.recyclerView.visibility = View.GONE
                binding.ivNoData.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })

        mViewModel.onGetAvailability().observe(this, Observer {
            if (it.availabilitySlots?.isEmpty() == true) {
                binding.tvSelectDate.visibility = View.GONE
                binding.tvEditSlots.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.ivNoData.visibility = View.VISIBLE
            } else {
                binding.tvSelectDate.visibility = View.VISIBLE
                binding.tvEditSlots.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                binding.ivNoData.visibility = View.GONE

                // Update adapter
                mAvailabilityData = it
                val list = ArrayList<TimeSlot>()
                it.availabilitySlots?.forEach { item ->
                    item.slots?.let { it1 -> list.addAll(it1) }

                }
                mAvailableSlotsAdapter.updateData(list)
            }

            if (mUserPrefsManager.loginUser?.isAvailability == false) {
                // Send broadcast
                requireContext().sendBroadcast(Intent(INTENT_PROFILE))
            }
        })
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvEditSlots -> {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = EditAvailabilityFragment.newInstance(
                        mSelectedDate,
                        BUNDLE_VIEW_TYPE_EDIT,
                        mAvailabilityData
                    ),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
            }

            R.id.ivAdd -> {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = EditAvailabilityFragment.newInstance(
                        mSelectedDate,
                        BUNDLE_VIEW_TYPE_CREATE,
                        AvailabilityData()
                    ),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
            }

        }
    }

    override fun onResume() {
        super.onResume()
        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(INTENT_AVAILABILITY), Context.RECEIVER_EXPORTED
            )
    }

    override fun onPause() {
        super.onPause()
        // Initialize receiver
        requireContext()
            .unregisterReceiver(
                mGetUpdateDataBroadcastReceiver
            )
    }

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    // Api call
                    mViewModel.getAvailability(mSelectedDate)
                } catch (e: Exception) {
                    println(e)

                }

            }
        }
    }

}
