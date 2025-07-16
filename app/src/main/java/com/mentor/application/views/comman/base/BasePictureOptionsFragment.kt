package com.swingby.app.views.fragments.base

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.mentor.application.BuildConfig
import com.mentor.application.R
import com.mentor.application.databinding.ShowPictureOptionsBottomSheetBinding
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.utils.ImageDownSample
import com.mentor.application.utils.MarshMallowPermissions
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File


/**
 * Created by Mukesh on 17/5/18.
 */
abstract class BasePictureOptionsFragment<VB : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> VB
) : BaseFragment<VB>(bindingInflater), ImageDownSample.SampledImageAsyncResp {

    private var picturePath: String? = null
    private var imagesDirectory: String? = null
    private var isCameraOptionSelected: Boolean = false
    private var isGalleryImage: Boolean = false
    private val mMarshMallowPermissions by lazy { MarshMallowPermissions(this) }
    private var imagesList = arrayListOf<String>()

    override fun init(savedInstanceState: Bundle?) {
        setData(savedInstanceState)
    }

    fun showPictureOptionsBottomSheet(
        imagesDirectory: String,
        showRecentUploadsOption: Boolean = false
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.TransparentDialog)
        val view = ShowPictureOptionsBottomSheetBinding.inflate(layoutInflater)

        view.tvCamera.setOnClickListener {
            checkForPermissions(true, imagesDirectory)
            bottomSheetDialog.dismiss()
        }
        view.tvGallery.setOnClickListener {
            checkForPermissions(false, imagesDirectory)
            bottomSheetDialog.dismiss()
        }

        view.tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }

        bottomSheetDialog.setContentView(view.root)
        bottomSheetDialog.show()
    }

    fun checkForPermissions(isCameraOptionSelected: Boolean, imagesDirectory: String) {
        this.isCameraOptionSelected = isCameraOptionSelected
        this.imagesDirectory = imagesDirectory

        // Check if the app has permission to read media access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {


            if (isCameraOptionSelected) {
                startCameraIntent()
            } else {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    mMarshMallowPermissions.giveReadMediaImagePermission()
                } else {
                    openGallery()

                }
            }


        } else {
            if (mMarshMallowPermissions.isPermissionGrantedForWriteExtStorage) {
                if (isCameraOptionSelected) {
                    startCameraIntent()
                } else {
                    openGallery()
                }
            } else {
                mMarshMallowPermissions.requestPermissionForWriteExtStorage()
            }
        }


    }

    private fun openGallery() {
        isGalleryImage = true
        imageActivityResultLauncher.launch(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
        )
    }


    private fun startCameraIntent() {
        if (mMarshMallowPermissions.isCameraPermission) {
            isGalleryImage = false
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                val file = imagesDirectory?.let { GeneralFunctions.setUpImageFile(it) }
                picturePath = file?.absolutePath

                val outputUri = file?.let {
                    FileProvider
                        .getUriForFile(
                            requireActivity(),
                            BuildConfig.APPLICATION_ID + ".provider",
                            it
                        )
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            } catch (e: Exception) {
                e.printStackTrace()
                picturePath = null
            }
            imageActivityResultLauncher.launch(takePictureIntent)

        } else {
            mMarshMallowPermissions.giveCameraPermission()
        }

    }

    private var imageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (isGalleryImage) {
                val selectedImage = result.data?.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = selectedImage?.let {
                    requireActivity().contentResolver.query(
                        it,
                        filePathColumn, null, null, null
                    )
                }
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                picturePath = columnIndex?.let { cursor.getString(it) }
                cursor?.close()
            }

            // DownSample image
            ImageDownSample(
                lifecycleScope, this, picturePath!!, imagesDirectory!!, true,
                resources.getDimension(R.dimen.image_downsample_size).toInt()
            ).run()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MarshMallowPermissions.RQ_WRITE_EXTERNAL_STORAGE ->
                if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    if (isCameraOptionSelected) {
                        startCameraIntent()
                    } else {
                        openGallery()
                    }
                } else {
                    showMessage(
                        R.string.enable_storage_permission, null,
                        true
                    )
                }

            MarshMallowPermissions.RQ_CAMERA_CAPTURE_PERMISSION ->
                if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    if (isCameraOptionSelected) {
                        startCameraIntent()
                    } else {
                        openGallery()
                    }
                } else {
                    showMessage(
                        R.string.enable_storage_permission, null,
                        true
                    )
                }

            MarshMallowPermissions.READ_MEDIA_IMAGES ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isCameraOptionSelected) {
                        startCameraIntent()
                    } else {
                        openGallery()
                    }
                } else {
                    showMessage(
                        R.string.enable_storage_permission, null,
                        true
                    )
                }
        }
    }


    override fun onSampledImageAsyncPostExecute(file: File) {
        onGettingImageFile(file)
    }

    abstract fun setData(savedInstanceState: Bundle?)
    abstract fun onGettingImageFile(file: File)
}