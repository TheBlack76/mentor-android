package com.trendy.app.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mentor.application.R
import com.mentor.application.databinding.DialogCancelBookingBinding
import com.mentor.application.databinding.DialogCancelBookingRequestBinding
import com.mentor.application.databinding.DialogInfomrtaionBinding

object DialogUtils {
    fun cancelBookingDialog(
        context: Context,
        name: String,
        isReschedule: Boolean,
        layoutInflater: LayoutInflater,
        callBack: CancelBookingDialogInterface
    ) {
        val dialog = MaterialAlertDialogBuilder(context).create()
        val dialogView = DialogCancelBookingBinding.inflate(layoutInflater)
        dialog.setView(dialogView.root)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setWindowAnimations(R.style.DialogFragmentAnimations)

        // Set content
        dialogView.tvName.text = name

        if (isReschedule) {
            dialogView.tvReschedule.visibility = View.VISIBLE
        } else {
            dialogView.tvReschedule.visibility = View.GONE
        }

        dialogView.btnSubmit.setOnClickListener {
            callBack.onCancel()
            dialog.dismiss()
        }
        dialogView.ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialogView.tvReschedule.setOnClickListener {
            callBack.onReschedule()
            dialog.dismiss()
        }
        dialog.show()
    }


    fun informationDialog(
        context: Context,
        name: String,
        isReschedule: Boolean,
        layoutInflater: LayoutInflater,
        callBack: InformationDialogInterface
    ) {
        val dialog = MaterialAlertDialogBuilder(context).create()
        val dialogView = DialogInfomrtaionBinding.inflate(layoutInflater)
        dialog.setView(dialogView.root)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setWindowAnimations(R.style.DialogFragmentAnimations)

        // Set content
        dialogView.btnSubmit.setOnClickListener {
            callBack.onOkay()
            dialog.dismiss()
        }
        dialogView.ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun cancelBookingRequestDialog(
        context: Context,
        layoutInflater: LayoutInflater,
        callBack: CancelBookingRequestDialogInterface
    ) {
        val dialog = MaterialAlertDialogBuilder(context).create()
        val dialogView = DialogCancelBookingRequestBinding.inflate(layoutInflater)
        dialog.setView(dialogView.root)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setWindowAnimations(R.style.DialogFragmentAnimations)

        dialogView.btnSubmit.setOnClickListener {
            callBack.onCancel()
            dialog.dismiss()
        }
        dialogView.ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialogView.tvReschedule.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    interface CancelBookingDialogInterface {
        fun onCancel()
        fun onReschedule()
    }

    interface CancelBookingRequestDialogInterface {
        fun onCancel()
    }

    interface InformationDialogInterface {
        fun onOkay()
    }


}