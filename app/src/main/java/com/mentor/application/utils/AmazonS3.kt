package com.mentor.application.utils

import android.content.Context
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.mentor.application.BuildConfig
import java.io.File


/**
 * Created by Mukesh on 01/03/20.
 */
class AmazonS3(private val context: Context) {


    companion object {
        private const val COGNITO_POOL_REGION = "us-west-1"
        private const val S3_BUCKET_REGION = "us-west-1"

        internal const val S3_CUSTOMER_PHOTOS = "mentorappbuket/customerPhotos"
        internal const val S3_PROFESSIONAL_PHOTOS = "mentorappbuket/professionalPhotos"
        internal const val S3_PROFESSIONAL_CERTIFICATE = "mentorappbuket/professionalCertificates"
        internal const val S3_PROFESSIONAL_PAST_WORK = "mentorappbuket/professionalPastWork"
        internal const val S3_CHAT_PHOTOS = "mentorappbuket/chatPhotos"

        internal const val SERVER_CUSTOMER_PHOTOS = "customerPhotos/"
        internal const val SERVER_PROFESSIONAL_PHOTOS = "professionalPhotos/"
        internal const val SERVER_CHAT_PHOTOS = "chatPhotos/"
        internal const val SERVER_CHAT_VIDEOS = "chatVideos/"
        internal const val SERVER_PROFESSIONAL_CERTIFICATE = "professionalCertificates/"
        internal const val SERVER_PROFESSIONAL_PAST_WORK = "professionalPastWork/"


        internal const val S3_BUCKET_BASE_URL_FOR_PHOTOS =
            "https://mentorappbuket.s3-us-west-1.amazonaws.com/"

    }

    private val mAmazonS3Client by lazy {
        AmazonS3Client(
            CognitoCachingCredentialsProvider(
                context,
                BuildConfig.COGNITO_POOL_ID,
                Regions.fromName(COGNITO_POOL_REGION)
            ), Region.getRegion(Regions.fromName(S3_BUCKET_REGION))
        )
    }

    private val mTransferUtility by lazy {
        TransferUtility.builder()
            .context(context)
//            .awsConfiguration(AWSConfiguration(context))
            .s3Client(mAmazonS3Client)
            .build()
    }

    /**
     * This method is used to upload file to S3 by using TransferUtility class
     * Accepts a callback function as a parameter to handle upload completion.
     */
    fun uploadFileToS3(file: File, s3Bucket: String, callback: (Boolean) -> Unit) {
        val observer = mTransferUtility.upload(
            s3Bucket,
            file.name,
            file,
            CannedAccessControlList.PublicRead
        )

        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                Log.e("onStateChanged", state.toString() + "")
                if (state == TransferState.COMPLETED) {
                    callback(true) // Upload completed successfully

                } else if (state == TransferState.FAILED || state == TransferState.CANCELED) {
                    callback(false) // Upload failed or canceled
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                // Handle progress changes if needed
                val percentage = (bytesCurrent / bytesTotal * 100).toInt()
                Log.e("onProgressChanged", percentage.toString() + "")
            }

            override fun onError(id: Int, ex: Exception) {
                Log.e("error", "Error uploading file: $ex")
                callback(false) // Upload failed due to error
            }
        })
    }
}
