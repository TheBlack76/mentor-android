package com.mentor.application.utils

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mentor.application.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

/**
 * Created by Mukesh on 20/7/18.
 */
class MyCustomLoader @Inject constructor(@ActivityContext val mContext: Context?) {

    private var mDialog: Dialog? = null

    fun showSnackBar(view: View?, contentMsg: String, isError: Boolean = false) {
        if (null != mContext && null != view) {
            // Define the background color based on isError condition
            val backgroundColor = if (isError) {
                ContextCompat.getColor(mContext, R.color.colorToastError)
            } else {
                ContextCompat.getColor(mContext, R.color.colorToastSuccess)
            }




            // Create and configure the Snackbar
            val snackbar = Snackbar.make(view, contentMsg, BaseTransientBottomBar.LENGTH_LONG)
                .setActionTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.colorBlack
                    )
                ) // Set action text color
                .setBackgroundTint(backgroundColor) // Set background color
                .setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.colorWhite
                    )
                ) // Set text color

            // Get the Snackbar's View and modify its layout parameters
            val snackbarView = snackbar.view

            // Change the text size
            val textView =
                snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.textSize = 15f // Set your desired text size in SP

            val params = snackbarView.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP // Position the Snackbar at the top
            params.setMargins(
                20,
                20,
                20,
                20
            ) // Optional: Add some margin from the top to avoid status bar overlap
            snackbarView.layoutParams = params

            // Show the Snackbar
            snackbar.show()
        }
    }

    fun showToast(contentMsg: String, isError: Boolean = false) {
        if (null != mContext) {
            Toast.makeText(mContext, contentMsg, Toast.LENGTH_SHORT).show()
        }
    }

    fun showProgressDialog() {
        mDialog = mContext?.let {
            Dialog(
                it,
                com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar_Fullscreen
            )
        }
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.window?.setBackgroundDrawable(mContext?.let {
            ContextCompat.getColor(
                it, R.color.colorWhiteTransparent_30
            )
        }?.let { ColorDrawable(it) })
        mDialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        /* // Inflate custom view
         val view = (mContext?.getSystemService(
             Context
                 .LAYOUT_INFLATER_SERVICE
         ) as LayoutInflater)
             .inflate(R.layout.dialog_progress_loader, null)*/

        // Inflate custom view
        val view = (mContext?.getSystemService(
            Context
                .LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater)
            .inflate(R.layout.dialog_progress_loader, null)

        mDialog?.setContentView(view)
        mDialog?.setCancelable(false)
        mDialog?.show()
    }

    fun dismissProgressDialog() {
        mDialog?.dismiss()
    }
}