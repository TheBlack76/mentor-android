package com.mentor.application.views.comman.dialgofragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/**
 * Created by Mukesh on 14/04/2016.
 */
class DatePickerDialogFragment : DialogFragment() {

    private lateinit var onDateSetListener: DatePickerDialog.OnDateSetListener

    companion object {

        const val BUNDLE_EXTRAS_YEAR = "year"
        const val BUNDLE_EXTRAS_MONTH = "month"
        const val BUNDLE_EXTRAS_DAY = "day"
        const val BUNDLE_EXTRAS_MIN_DATE = "minDate"
        const val BUNDLE_EXTRAS_MAX_DATE = "maxDate"

        fun newInstance(year: Int, month: Int, day: Int, minDate: Long = 0, maxDate: Long = 0):
                DatePickerDialogFragment {
            val datePickerDialogFragment = DatePickerDialogFragment()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_EXTRAS_YEAR, year)
            bundle.putInt(BUNDLE_EXTRAS_MONTH, month)
            bundle.putInt(BUNDLE_EXTRAS_DAY, day)
            bundle.putLong(BUNDLE_EXTRAS_MIN_DATE, minDate)
            bundle.putLong(BUNDLE_EXTRAS_MAX_DATE, maxDate)
            datePickerDialogFragment.arguments = bundle
            return datePickerDialogFragment
        }
    }

    fun setCallBack(onDateSetListener: DatePickerDialog.OnDateSetListener) {
        this.onDateSetListener = onDateSetListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val datePickerDialog: DatePickerDialog
        if (null != arguments) {

            // Create date picker dialog
            datePickerDialog = DatePickerDialog(requireActivity(),
                    onDateSetListener, requireArguments().getInt(BUNDLE_EXTRAS_YEAR),
                    requireArguments().getInt(BUNDLE_EXTRAS_MONTH), requireArguments()
                    .getInt(BUNDLE_EXTRAS_DAY))

            // Get min and max date from arguments
            if (null != arguments) {
                // Get and set min date
                var date = requireArguments().getLong(BUNDLE_EXTRAS_MIN_DATE, 0)
                if (0L != date) {
                    datePickerDialog.datePicker.minDate = date
                }

                // Get and set max date
                date = requireArguments().getLong(BUNDLE_EXTRAS_MAX_DATE, 0)
                if (0L != date) {
                    datePickerDialog.datePicker.maxDate = date
                }
            }
        } else {
            datePickerDialog = DatePickerDialog(requireActivity(),
                    onDateSetListener, 1, 1, 1971)
        }
        return datePickerDialog
    }

}
