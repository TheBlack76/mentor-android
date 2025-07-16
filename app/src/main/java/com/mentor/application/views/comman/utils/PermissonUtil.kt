package com.mentor.application.views.comman.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.mentor.application.BuildConfig
import java.io.File

class PermissonUtil {
    /**
     * Refreshes gallery on adding new image/video. Gallery won't be refreshed
     * on older devices until device is rebooted
     */
    fun refreshGallery(context: Context, filePath: String) {
        // ScanFile so it will be appeared on Gallery
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath), null
        ) { path, uri -> }
    }

   public fun checkPermissions(context: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) === PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) === PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) === PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) === PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) === PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.WAKE_LOCK
        ) === PackageManager.PERMISSION_GRANTED)

    }

    /**
     * Downsizing the bitmap to avoid OutOfMemory exceptions
     */
    fun optimizeBitmap(sampleSize: Int, filePath: String): Bitmap {
        // bitmap factory
        val options = BitmapFactory.Options()

        // downsizing image as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = sampleSize

        return BitmapFactory.decodeFile(filePath, options)
    }

    /**
     * Checks whether device has camera or not. This method not necessary if
     * android:required="true" is used in manifest file
     */
    fun isDeviceSupportCamera(context: Context): Boolean {
        return if (context.packageManager.hasSystemFeature(
                PackageManager.FEATURE_CAMERA
            )
        ) {
            // this device has a camera
            true
        } else {
            // no camera on this device
            false
        }
    }

    /**
     * Open device app settings to allow user to enable permissions
     */
    fun openSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun getOutputMediaFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    }

    /**
     * Creates and returns the image or video file before opening the camera
     */
}
 // External sdcard location
/*        val mediaStorageDir = File(
Environment
.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
CreateAccount.GALLERY_DIRECTORY_NAME)*/

 // Create the storage directory if it does not exist
/*        if (!mediaStorageDir.exists())
{
if (!mediaStorageDir.mkdirs())
{
Log.e(CreateAccount.GALLERY_DIRECTORY_NAME, "Oops! Failed create "
+ CreateAccount.GALLERY_DIRECTORY_NAME + " directory"
)
return null
}
}*/

 // Preparing media file naming convention
        // adds timestamp
/*        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
Locale.getDefault()).format(Date())
val mediaFile: File
if (type == CreateAccount.MEDIA_TYPE_IMAGE)
{
mediaFile = File(
    mediaStorageDir.path + File.separator
    + "IMG_" + timeStamp + "." + CreateAccount.IMAGE_EXTENSION
)
}
else if (type == CreateAccount.MEDIA_TYPE_VIDEO)
{
mediaFile = File((mediaStorageDir.path + File.separator
+ "VID_" + timeStamp + "." + CreateAccount.VIDEO_EXTENSION))
}
else
{
return null
}

return mediaFile
}

}*/
