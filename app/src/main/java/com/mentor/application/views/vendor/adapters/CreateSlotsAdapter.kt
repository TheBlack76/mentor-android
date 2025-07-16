package com.mentor.application.views.vendor.adapters

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.R
import com.mentor.application.databinding.LayoutAvailabilitySlotsCreateBinding
import com.mentor.application.repository.models.AvailabilityTimeSlots
import com.mentor.application.repository.models.TimeSlot
import com.mentor.application.utils.Constants.DATE_DISPLAY_TIME
import com.mentor.application.utils.Constants.DATE_SERVER_TIME
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.vendor.fragments.EditAvailabilityFragment.Companion.SLOTS_TYPE_AUTO
import com.mentor.application.views.vendor.fragments.EditAvailabilityFragment.Companion.SLOTS_TYPE_CUSTOM
import com.mentor.application.views.vendor.fragments.EditAvailabilityFragment.Companion.mSlotType
import com.mentor.application.views.vendor.interfaces.EditAvailabilityInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@FragmentScoped
class CreateSlotsAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: EditAvailabilityInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mList = mutableListOf<AvailabilityTimeSlots>()
    private val durationMainList = mutableListOf("Select Duration", "30", "60", "90")
    private var durationOptions = mutableListOf("Select Duration", "30", "60", "90")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutAvailabilitySlotsCreateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun getSlots(): List<AvailabilityTimeSlots> {
        return mList
    }

    fun updateData(list: List<AvailabilityTimeSlots>) {
        mList.clear()
        mList.addAll(list)
        updateDurationList()
        notifyDataSetChanged()
    }

    fun addDurationSlot() {
        mList.add(AvailabilityTimeSlots("", "", "", mutableListOf()))
        notifyDataSetChanged()
    }

    fun deleteSlot(position: Int) {
        if (position == 0) {
            mList[0] = AvailabilityTimeSlots("", "", "", mutableListOf())
        } else {
            mList.removeAt(position)
        }
        notifyDataSetChanged()
    }

    fun updateDurationList() {
        durationOptions.clear()
        durationOptions.addAll(durationMainList)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }


    private inner class ListViewHolder(val binding: LayoutAvailabilitySlotsCreateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            // If the slots are updated disable editable fields and enable the buttons
            if (mList[absoluteAdapterPosition].slots?.isEmpty() == true) {
                binding.btnCancel.visibility =
                    if (absoluteAdapterPosition == 0) View.GONE else View.VISIBLE

                binding.btnGenerate.visibility = View.VISIBLE
                binding.tvStartTime.isEnabled = true
                binding.tvEndTime.isEnabled = true
                binding.spnrDuration.isEnabled = true
                binding.spnrDuration.alpha = 1f
                binding.tvDurationValue.alpha = 0f
            } else {
                binding.btnCancel.visibility = View.GONE
                binding.btnGenerate.visibility = View.GONE
                binding.tvStartTime.isEnabled = false
                binding.tvEndTime.isEnabled = false
                binding.spnrDuration.isEnabled = false
                binding.spnrDuration.alpha = 0f
                binding.tvDurationValue.alpha = 1f

            }

            // Preselect the items
            binding.tvStartTime.text = GeneralFunctions.changeDateFormat(
                mList[absoluteAdapterPosition].startTime,
                DATE_SERVER_TIME, DATE_DISPLAY_TIME
            )
            binding.tvEndTime.text = GeneralFunctions.changeDateFormat(
                mList[absoluteAdapterPosition].endTime,
                DATE_SERVER_TIME, DATE_DISPLAY_TIME
            )
            binding.tvDurationValue.text = mList[absoluteAdapterPosition].duration

            // Update adapter
            val adapter = SlotsGeneratedAdapter(
                mContext,
                mList[absoluteAdapterPosition].slots as ArrayList,
                absoluteAdapterPosition,
                mListener
            )
            binding.recyclerView.adapter = adapter

            // Click listener
            binding.tvStartTime.setOnClickListener {
                showTimePickerDialog(absoluteAdapterPosition, 0) { selectedTime ->
                    mList[absoluteAdapterPosition].startTime = selectedTime
                    binding.tvStartTime.text = GeneralFunctions.changeDateFormat(
                        selectedTime,
                        DATE_SERVER_TIME, DATE_DISPLAY_TIME
                    ) // Set the selected time in TextView

                    if (mSlotType == SLOTS_TYPE_CUSTOM) {
                        if (mList[absoluteAdapterPosition].duration.isNotBlank()) {
                            val endTime = getEndTime(
                                mList[absoluteAdapterPosition].startTime,
                                mList[absoluteAdapterPosition].duration.toInt()
                            )

                            mList[absoluteAdapterPosition].endTime = endTime
                            binding.tvEndTime.text = GeneralFunctions.changeDateFormat(
                                endTime,
                                DATE_SERVER_TIME, DATE_DISPLAY_TIME
                            )
                        }

                    } else {
                        mList[absoluteAdapterPosition].endTime = ""
                        binding.tvEndTime.text = ""
                    }

                }
            }

            // For custom slots disable the end time field
            if (mSlotType == SLOTS_TYPE_CUSTOM) {
                binding.tvEndTime.isEnabled = false
                binding.tvEndTime.background = ContextCompat.getDrawable(
                    mContext!!,
                    R.drawable.drawable_color_medium_rounded_corners_solid
                )

                binding.tvEndTime.backgroundTintList = ContextCompat.getColorStateList(
                    mContext,
                    R.color.colorDivider
                )

            } else {
                binding.tvEndTime.background = ContextCompat.getDrawable(
                    mContext!!,
                    R.drawable.drawable_input_edittext_normal
                )

                binding.tvEndTime.backgroundTintList = ContextCompat.getColorStateList(
                    mContext,
                    R.color.colorEditTextStroke
                )

                // Set click listener
                binding.tvEndTime.setOnClickListener {
                    showTimePickerDialog(absoluteAdapterPosition, 1) { selectedTime ->
                        mList[absoluteAdapterPosition].endTime = selectedTime
                        binding.tvEndTime.text = GeneralFunctions.changeDateFormat(
                            selectedTime,
                            DATE_SERVER_TIME, DATE_DISPLAY_TIME
                        ) // Set the selected time in TextView
                    }
                }
            }

            // Initialize spinner
            initSpinner(binding.spnrDuration, binding.tvEndTime, absoluteAdapterPosition)

            // Set click listener to generate the slots
            binding.btnGenerate.setOnClickListener {
                if (mList[absoluteAdapterPosition].startTime.isNotBlank()
                    && mList[absoluteAdapterPosition].endTime.isNotBlank()
                    && mList[absoluteAdapterPosition].duration.isNotBlank()
                ) {

                    val mSlotsList = createTimeSlots(
                        mList[absoluteAdapterPosition].startTime,
                        mList[absoluteAdapterPosition].endTime,
                        mList[absoluteAdapterPosition].duration.toInt()
                    )

                    if (mSlotsList.isEmpty()) {
                        mListener.onShowError("Duration is too long for the given time range.")
                    } else {
                        mList[absoluteAdapterPosition].slots = mSlotsList

                        val mAdapter =
                            SlotsGeneratedAdapter(
                                mContext,
                                mSlotsList as ArrayList,
                                absoluteAdapterPosition,
                                mListener
                            )
                        binding.recyclerView.adapter = mAdapter
                        notifyItemChanged(absoluteAdapterPosition)
                        mListener.onGenerateSlot()
                    }

                } else {
                    if (mList[absoluteAdapterPosition].startTime.isBlank()) {
                        mListener.onShowError(mContext.getString(R.string.st_please_set_start_time))
                    } else if (mList[absoluteAdapterPosition].endTime.isBlank()) {
                        mListener.onShowError(mContext.getString(R.string.st_please_set_end_time))
                    } else if (mList[absoluteAdapterPosition].duration.isBlank()) {
                        mListener.onShowError(mContext.getString(R.string.st_please_set_duration))
                    }
                }
            }

            // Set click listener to cancel the slot
            binding.btnCancel.setOnClickListener {
                mList.removeAt(absoluteAdapterPosition)

                // Update the list again once the new item is added or remove
                durationOptions.clear()
                durationOptions.addAll(durationMainList)
                notifyDataSetChanged()
                mListener.onCancelSlot(absoluteAdapterPosition)
            }
        }
    }

    fun initSpinner(spinner: Spinner, endTimeView: TextView, absoluteAdapterPosition: Int) {
        // Remove the duration from the options which are already added
        if (mSlotType == SLOTS_TYPE_AUTO) {
            durationOptions.remove(mList[absoluteAdapterPosition].duration)
        }
        // Create an ArrayAdapter using a simple spinner layout and the list of options
        val durationAdapter =
            ArrayAdapter(mContext!!, android.R.layout.simple_spinner_item, durationOptions)

        // Specify the layout to use when the list of choices appears
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the adapter to the Spinner
        spinner.adapter = durationAdapter

        // Set an item selection listener for the Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                // Get the selected item
                val selectedOption = parent.getItemAtPosition(position).toString()
                if (position != 0) {
                    mList[absoluteAdapterPosition].duration = selectedOption
                    if (mSlotType == SLOTS_TYPE_CUSTOM) {
                        if (mList[absoluteAdapterPosition].startTime.isNotBlank()) {
                            val endTime = getEndTime(
                                mList[absoluteAdapterPosition].startTime,
                                mList[absoluteAdapterPosition].duration.toInt()
                            )

                            mList[absoluteAdapterPosition].endTime = endTime
                            endTimeView.text = GeneralFunctions.changeDateFormat(
                                endTime,
                                DATE_SERVER_TIME, DATE_DISPLAY_TIME
                            )
                        }

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case where no option is selected
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePickerDialog(position: Int, type: Int, onTimeSelected: (String) -> Unit) {
        // Get the current time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create the TimePickerDialog
        val timePickerDialog = TimePickerDialog(mContext, { _, selectedHour, selectedMinute ->
            // Format the selected time
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

            if (type == 0) {
                onTimeSelected(selectedTime)
            } else {
                if (mList[position].startTime.isNotBlank() && !isEndTimeGreaterThanStartTime(
                        mList[position].startTime, selectedTime
                    )
                ) {
                    mListener.onShowError(mContext!!.getString(R.string.st_invalid_time))
                } else {
                    // Return the selected time through the callback
                    onTimeSelected(selectedTime)
                }
            }

        }, hour, minute, true) // Set 'true' for 24-hour format, 'false' for 12-hour

        // Show the TimePickerDialog
        timePickerDialog.show()
    }

    // Function to generate time slots from start to end time
    fun createTimeSlots(
        startTime: String,
        endTime: String,
        durationInMinutes: Int
    ): List<TimeSlot> {
        val timeFormat = SimpleDateFormat(DATE_SERVER_TIME, Locale.getDefault()) // 24-hour format
        val start = timeFormat.parse(startTime) ?: return emptyList()
        val end = timeFormat.parse(endTime) ?: return emptyList()

        val calendar = Calendar.getInstance()
        calendar.time = start

        val slots = mutableListOf<TimeSlot>()

        // Loop through time from start to end, adding slots of given duration
        while (calendar.time.before(end)) {
            val slotStart = timeFormat.format(calendar.time) // Format the slot start time
            calendar.add(Calendar.MINUTE, durationInMinutes)  // Add duration to time

            // Check if the slot end time exceeds the end time
            if (calendar.time.after(end)) {
                break // Exit loop if the next slot end time would exceed end time
            }

            val slotEnd = timeFormat.format(calendar.time) // Format the slot end time
            slots.add(TimeSlot(startTime = slotStart, endTime = slotEnd)) // Add the slot
        }

        return slots
    }


    fun getEndTime(startTime: String, durationInMinutes: Int): String {
        val timeFormat = SimpleDateFormat(DATE_SERVER_TIME, Locale.getDefault())
        val startDate = timeFormat.parse(startTime)

        // Initialize a Calendar with the start time
        val calendar = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.MINUTE, durationInMinutes) // Add duration to the start time
        }

        return timeFormat.format(calendar.time) // Return formatted end time
    }

    // Function to compare times in HH:mm format
    private fun isEndTimeGreaterThanStartTime(start: String, end: String): Boolean {
        // Split the times into hours and minutes
        val startTimeParts = start.split(":").map { it.toInt() }
        val endTimeParts = end.split(":").map { it.toInt() }

        // Create comparable time values (in minutes)
        val startMinutes =
            startTimeParts[0] * 60 + startTimeParts[1] // Convert start time to total minutes
        val endMinutes = endTimeParts[0] * 60 + endTimeParts[1] // Convert end time to total minutes

        return endMinutes > startMinutes // Return true if end time is greater
    }


}