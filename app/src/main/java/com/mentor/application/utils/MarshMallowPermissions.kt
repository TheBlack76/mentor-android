package com.mentor.application.utils

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mentor.application.R

/**
 * Created by Mukesh on 20/7/18.
 */
class MarshMallowPermissions(private val mFragment: Fragment) {

    companion object {
        const val RQ_LOCATION_PERMISSION = 23
        const val RQ_WRITE_EXTERNAL_STORAGE = 19
        const val RQ_REad_EXTERNAL_STORAGE = 1111
        const val RQ_RECORD_AUDIo = 124
        const val RQ_CAMERA_CAPTURE_PERMISSION = 28
        const val READ_MEDIA_IMAGES = 232
    }
    private val mCameraCapturePermissions: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var mActivity: Activity = mFragment.requireActivity()

    val isPermissionGrantedForLocation: Boolean
        get() = ContextCompat.checkSelfPermission(
            mActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun requestPermissionForLocation(cancelListener: DialogInterface.OnClickListener) {
        if (mFragment.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showAlertDialog(
                mActivity.getString(R.string.location_permission_needed),
                DialogInterface.OnClickListener { _, _ ->
                    mFragment.requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        RQ_LOCATION_PERMISSION
                    )
                }, cancelListener
            )
        } else {
            mFragment.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                RQ_LOCATION_PERMISSION
            )
        }
    }

    val isPermissionGrantedForWriteExtStorage: Boolean
        get() = ContextCompat.checkSelfPermission(
            mActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    fun requestPermissionForWriteExtStorage() {
        if (mFragment.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showAlertDialog(
                mActivity.getString(R.string.storage_permission_needed),
                DialogInterface.OnClickListener { _, _ ->
                    mFragment.requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        RQ_WRITE_EXTERNAL_STORAGE
                    )
                }, null
            )
        } else {
            mFragment.requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RQ_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun requestPermissionForReadExtStorage() {
        if (mFragment.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showAlertDialog(
                mActivity.getString(R.string.storage_permission_needed),
                DialogInterface.OnClickListener { _, _ ->
                    mFragment.requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        RQ_REad_EXTERNAL_STORAGE
                    )
                }, null
            )
        } else {
            mFragment.requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RQ_REad_EXTERNAL_STORAGE
            )
        }
    } fun requestPermissionForAudio() {
        if (mFragment.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            showAlertDialog(
                mActivity.getString(R.string.storage_permission_needed),
                DialogInterface.OnClickListener { _, _ ->
                    mFragment.requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        RQ_RECORD_AUDIo
                    )
                }, null
            )
        } else {
            mFragment.requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RQ_RECORD_AUDIo
            )
        }
    }
    val isCameraPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            mActivity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun giveCameraPermission() {

        mFragment.requestPermissions(
            mCameraCapturePermissions,
            RQ_CAMERA_CAPTURE_PERMISSION
        )}

    fun giveReadMediaImagePermission() {
        mFragment.requestPermissions(
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            READ_MEDIA_IMAGES)

    }

    fun reqPermissionsForCameraCapture(selfie: Boolean) {
        if (mFragment.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {

            giveCameraPermission()

        } else {
              mFragment.requestPermissions(
                      mCameraCapturePermissions,
                      RQ_CAMERA_CAPTURE_PERMISSION
              )

            /*    mFragment.requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        RQ_WRITE_EXTERNAL_STORAGE
                )*/

        }
    }
        private fun showAlertDialog(
        message: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(mActivity)
            .setMessage(message)
            .setPositiveButton(mActivity.getString(R.string.ok), okListener)
            .setNegativeButton(mActivity.getString(R.string.cancel), cancelListener)
            .create()
            .show()
    }

}