package com.mentor.application.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ImageDownSample(
    private val coroutineScope: CoroutineScope,
    private var mFragment: Fragment,
    private var picturePath: String,
    private var imageDirectory: String,
    private var isGalleryImage: Boolean,
    private var reqImageWidth: Int
) {

    private var mSampledImageAsyncResp: SampledImageAsyncResp? = null
    init {
        mSampledImageAsyncResp = mFragment as SampledImageAsyncResp
    }

    fun run() {
        coroutineScope.launch {
            // Perform the background task
            val result = doDownImageFile()

            // Call the listener to pass the result back to the fragment
            mSampledImageAsyncResp?.onSampledImageAsyncPostExecute(result!!)
        }
    }

    private fun doDownImageFile(): File? {
        try {
            val picturePath = picturePath
            val imageDirectory = imageDirectory
            val isGalleryImage = isGalleryImage
            val reqImageWidth = reqImageWidth
            val exif = ExifInterface(picturePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 1
            )
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(picturePath, options)
            options.inSampleSize = calculateInSampleSize(options, reqImageWidth, reqImageWidth)
            options.inJustDecodeBounds = false
            var imageBitmap: Bitmap? = BitmapFactory.decodeFile(picturePath, options)
            when (orientation) {
                6 -> {
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    imageBitmap = Bitmap.createBitmap(
                        imageBitmap!!, 0, 0,
                        imageBitmap.width, imageBitmap.height,
                        matrix, true
                    )
                }
                8 -> {
                    val matrix = Matrix()
                    matrix.postRotate(270f)
                    imageBitmap = Bitmap.createBitmap(
                        imageBitmap!!, 0, 0,
                        imageBitmap.width, imageBitmap.height,
                        matrix, true
                    )
                }
                3 -> {
                    val matrix = Matrix()
                    matrix.postRotate(180f)
                    imageBitmap = Bitmap.createBitmap(
                        imageBitmap!!, 0, 0,
                        imageBitmap.width, imageBitmap.height,
                        matrix, true
                    )
                }
            }
            if (null != imageBitmap) {
                return getImageFile(imageBitmap, picturePath, imageDirectory, isGalleryImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }


    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getImageFile(
        bmp: Bitmap, picturePath: String, imageDirectory: String,
        isGalleryImage: Boolean
    ): File? {
        try {
            val fOut: OutputStream?
            val file: File = if (isGalleryImage) {
                GeneralFunctions.setUpImageFile(imageDirectory)!!
            } else {
                File(picturePath)
            }
            fOut = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
            fOut.flush()
            fOut.close()
            MediaStore.Images.Media.insertImage(
                mFragment.requireActivity().contentResolver,
                file.absolutePath, file.name,
                file.name
            )
            return file
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    interface SampledImageAsyncResp {
        fun onSampledImageAsyncPostExecute(file: File)
    }

}