package com.mentor.application.utils

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mentor.application.R
import com.mentor.application.utils.AmazonS3.Companion.S3_BUCKET_BASE_URL_FOR_PHOTOS
import com.mentor.application.utils.AmazonS3.Companion.SERVER_CUSTOMER_PHOTOS
import com.mentor.application.utils.AmazonS3.Companion.SERVER_PROFESSIONAL_PHOTOS
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Mukesh on 20/7/18.
 */
object GeneralFunctions {

    private const val ALPHA_NUMERIC_CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    internal const val JPEG_FILE_PREFIX = "IMG_"
    internal const val JPEG_FILE_SUFFIX = ".jpg"
    internal const val VIDEO_FILE_PREFIX = "VID_"
    internal const val VIDEO_FILE_SUFFIX = ".mp4"
    internal const val VIDEO_THUMB_FILE_PREFIX = "Thumb_"
    internal const val VIDEO_THUMB_FILE_SUFFIX = ".jpg"
    private const val MIN_PASSWORD_LENGTH = 6
    private const val MAX_PASSWORD_LENGTH = 15
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    val isAboveLollipopDevice: Boolean
        get() = Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT

    @Throws(IOException::class)
    fun setUpImageFile(directory: String): File? {
        var imageFile: File? = null
        if (Environment.MEDIA_MOUNTED == Environment
                .getExternalStorageState()
        ) {
            val storageDir = File(directory)
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    Log.d("CameraSample", "failed to create directory")
                    return null
                }
            }

            imageFile = File.createTempFile(
                JPEG_FILE_PREFIX
                        + System.currentTimeMillis() + "_",
                JPEG_FILE_SUFFIX, storageDir
            )
        }
        return imageFile
    }

    @Throws(IOException::class)
    fun setUpVideoFile(directory: String): File? {
        var videoFile: File? = null
        if (Environment.MEDIA_MOUNTED == Environment
                .getExternalStorageState()
        ) {
            val storageDir = File(directory)
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    return null
                }
            }

            videoFile = File.createTempFile(
                VIDEO_FILE_PREFIX
                        + System.currentTimeMillis() + "_",
                VIDEO_FILE_SUFFIX, storageDir
            )
        }
        return videoFile
    }

    fun getCurrentDate(format:String): String {
        val c = Calendar.getInstance().time
        println("Current time => $c")

        val df =
            SimpleDateFormat(format, Locale.getDefault())
        val formattedDate = df.format(c)


        return formattedDate
    }

    fun getCurrentServerDate(): String {
        val c = Calendar.getInstance().time
        println("Current time => $c")

        val df =
            SimpleDateFormat(Constants.DATE_FORMAT_SERVER, Locale.getDefault())
        val formattedDate = df.format(c)


        return formattedDate
    }

    /** Helper function used to create a timestamped file */
    internal fun createFile(
        context: Context

    ): File {
        val filePrefix = JPEG_FILE_PREFIX
        return File(
            getOutputDirectory(context), filePrefix + SimpleDateFormat(
                FILENAME_FORMAT,
                Locale.US
            ).format(System.currentTimeMillis()) + JPEG_FILE_SUFFIX
        )
    }

    fun getOutputDirectory(context: Context): File {
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, context.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context.filesDir
    }

    @Throws(IOException::class)
    fun setUpVideoThumbFile(directory: String): File? {
        var videoThumbFile: File? = null
        if (Environment.MEDIA_MOUNTED == Environment
                .getExternalStorageState()
        ) {
            val storageDir = File(directory)
            if (!storageDir.mkdirs()) {
                if (!storageDir.exists()) {
                    return null
                }
            }

            videoThumbFile = File.createTempFile(
                VIDEO_THUMB_FILE_PREFIX
                        + System.currentTimeMillis() + "_",
                VIDEO_THUMB_FILE_SUFFIX, storageDir
            )
        }
        return videoThumbFile
    }

    fun generateRandomString(randomStringLength: Int): String {
        val buffer = StringBuffer()
        val charactersLength = ALPHA_NUMERIC_CHARS.length
        for (i in 0 until randomStringLength) {
            val index = Math.random() * charactersLength
            buffer.append(ALPHA_NUMERIC_CHARS.get(index.toInt()))
        }
        return buffer.toString()
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return null != target && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH
    }


    fun getResizedImage(imageUrl: String, imageWidth: Int, imageHeight: Int): String {
        return imageUrl + (if (0 != imageWidth) "/$imageWidth" else "") +
                if (0 != imageHeight) "/$imageHeight" else ""
    }

    fun getLocalImageFile(file: File): String {
        return "file://$file"
    }

    internal fun getLocalMediaFile(context: Context, mediaName: String): File {
        return File("${getOutputDirectory(context)}/$mediaName")
    }

    fun changeDateFormat(input: String, currentFormat: String, requiredFormat: String): String {
        return try {
            // If input already has "am" or "pm" and required format is also 12-hour, skip conversion
            if (input.lowercase().contains("am") || input.lowercase().contains("pm")) {
                if (requiredFormat.contains("a", ignoreCase = true)) return input
            }

            val parser = SimpleDateFormat(currentFormat, Locale.US)
            val formatter = SimpleDateFormat(requiredFormat, Locale.US)
            val date = parser.parse(input) ?: return ""

            return formatter.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }



    fun isValidSelection(value: String, placeHolder: String): Boolean {
        return value != placeHolder
    }

    fun isBelow18(dob: String): Boolean {
        return false
    }

    fun calculateAge(dob: String): Int {
        try {
            val currentDate = Calendar.getInstance()

            val dateOfBirth = Calendar.getInstance()
            dateOfBirth.time =
                SimpleDateFormat(Constants.DATE_FORMAT_SERVER, Locale.US).parse(dob) ?: Date()

            var age = currentDate.get(Calendar.YEAR) - dateOfBirth[Calendar.YEAR]

            if (currentDate.get(Calendar.MONTH) < dateOfBirth[Calendar.MONTH] ||
                (currentDate.get(Calendar.MONTH) == dateOfBirth[Calendar.MONTH] &&
                        currentDate.get(Calendar.DAY_OF_MONTH) < dateOfBirth[Calendar.DAY_OF_MONTH])
            ) {
                age--
            }

            return age
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    fun isRemoteImage(image: String): Boolean {
        return image.startsWith("/")
    }

    fun getImage(image: String): String {
        return S3_BUCKET_BASE_URL_FOR_PHOTOS + image
    }

    fun getUserImage(image: String): String {
        val imagePath =
            if (image.contains(SERVER_CUSTOMER_PHOTOS) || image.contains(SERVER_PROFESSIONAL_PHOTOS)
            ) {
                getImage(image)
            } else {
                image
            }

        return imagePath
    }


    fun viewAddressOnMap(context: Context, latitude: Double, longitude: Double) {
        val uri =
            String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    fun imageLoad(url: String, image: ImageView, context: Context) {
        Glide.with(context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(image)
    }

    internal fun getAddress(mLatitide: Double, mLongitude: Double, context: Context): String {
        var mLocationName = ""
        val geoCoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address> = geoCoder.getFromLocation(
                mLatitide, mLongitude,
                1
            ) as List<Address>
            val obj: Address = addresses[0]
            val add = obj.getAddressLine(0)
            mLocationName = add.toString()

        } catch (e: Exception) {

        }
        return mLocationName
    }

    fun getPrice(rate:String,time:String,context: Context):SpannableString {
        val price = "$${rate}"
        val duration = "/$time"
        val formattedText = SpannableString(price + duration)
        val boldFont = ResourcesCompat.getFont(context!!, R.font.font_roboto_bold)
        val regularFont = ResourcesCompat.getFont(context!!, R.font.font_roboto_regular)

        boldFont?.let { font ->
            formattedText.setSpan(
                com.mentor.application.views.comman.utils.CustomTypefaceSpan(font),
                0,
                price.length, // Apply to "24"
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        regularFont?.let { font ->
            formattedText.setSpan(
                com.mentor.application.views.comman.utils.CustomTypefaceSpan(font),
                price.length,
                price.length + duration.length, // Apply to "/hr"
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return formattedText
    }

    fun openPdfInChromeCustomTab(path: String, context: Context) {
        // If the file exist with same name in local show from local else from server
        val mImageFile = getLocalMediaFile(context!!, File(path).name)

        if (mImageFile.exists()) {
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                mImageFile
            )

            // Create a Chrome Custom Tabs Intent
            val customTabsIntent = CustomTabsIntent.Builder().build()

            // Grant temporary read permission to Chrome to access the file
            customTabsIntent.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Launch Chrome Custom Tabs with the file URI
            customTabsIntent.launchUrl(context, fileUri)
        } else {
            // Create a Chrome Custom Tabs Intent
            val customTabsIntent = CustomTabsIntent.Builder().build()

            // Grant temporary read permission to Chrome to access the file
            customTabsIntent.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Launch Chrome Custom Tabs with the file URI
            customTabsIntent.launchUrl(context, Uri.parse(getImage(path)))
        }
    }

    fun getFilePathFromUri(uri: Uri, context: Context): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        context?.contentResolver?.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val fileName = cursor.getString(columnIndex)
                // Save the file to a temp location in your app's cache directory
                val inputStream = context?.contentResolver?.openInputStream(uri)
                val file = File(getOutputDirectory(context), fileName)
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                filePath = file.absolutePath
            }
        }
        return filePath
    }

    // Helper function to check if the media is a video
    fun isVideo(media: String): Boolean {
        // Implement your logic to check if the media type is video
        // This can be based on file extension or mime type
        return media.endsWith(".mp4", ignoreCase = true) || // Add other video extensions as needed
                media.endsWith(".mkv", ignoreCase = true) ||
                media.endsWith(".avi", ignoreCase = true)
    }


    // Helper function to copy the video to a own directory
    fun copyVideoToDirectory(originalPath: String, context: Context): String {
        val originalFile = File(originalPath)

        // Ensure the new directory exists
        val destDir = getOutputDirectory(context) // new directory
        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        val newFilePath = File(
            destDir, VIDEO_FILE_PREFIX + SimpleDateFormat(
                FILENAME_FORMAT,
                Locale.US
            ).format(System.currentTimeMillis()) + VIDEO_FILE_SUFFIX
        ) // Create a new file path in the new directory
        try {
            FileInputStream(originalFile).use { inputStream ->
                FileOutputStream(newFilePath).use { outputStream ->
                    inputStream.copyTo(outputStream) // Copy the file
                }
            }
        } catch (e: IOException) {
            e.printStackTrace() // Handle exceptions as needed
            return originalPath // Return the original path if there's an error
        }

        Log.e("ederded", "copyVideoToDirectory: " + originalPath)
        Log.e("ederded", "copyVideoToDirectory: " + newFilePath.absolutePath)
        return newFilePath.absolutePath // Return the new file path
    }

    fun changeUtcToLocal(dateStr: String, currentFormat: String, requiredFormat: String): String {
        val df = SimpleDateFormat(currentFormat, Locale.ENGLISH)
        df.timeZone = TimeZone.getTimeZone("UTC")
        val date = df.parse(dateStr)
        df.timeZone = TimeZone.getDefault()
        val formattedDate = df.format(date!!)

        try {
            return SimpleDateFormat(requiredFormat, Locale.US)
                .format(SimpleDateFormat(currentFormat, Locale.US).parse(formattedDate))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun isValidExperience(input: String): Boolean {
        val regex = Regex("^\\d+(\\.\\d+)?\\s*(day|days|month|months|yr|yrs)\$", RegexOption.IGNORE_CASE)
        return regex.matches(input.trim())
    }

     fun getLocalMillisFromISOString(isoString: String): Long {
        return try {
            val formatter = SimpleDateFormat(Constants.DATE_FORMAT_SERVER, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC") // Parse in UTC
            val date = formatter.parse(isoString)
            date?.time
                ?: 0L // This is already in UTC millis; local time is automatic when displaying
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    fun windowDecoder(view:FrameLayout){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 (API 34)
//            ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
//                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//
//                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                    leftMargin = systemBars.left
//                    topMargin = systemBars.top
//                    rightMargin = systemBars.right
//                    bottomMargin = systemBars.bottom
//                }
//
//                WindowInsetsCompat.CONSUMED
//            }
//
//            // In case the view is already attached and visible
//            ViewCompat.requestApplyInsets(view)
//        }
    }


}