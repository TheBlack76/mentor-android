import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.regions.Regions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

class S3ImageLoader private constructor(var context: Context) {

    private val credentialsProvider: CognitoCachingCredentialsProvider by lazy {
        CognitoCachingCredentialsProvider(
            context,
            poolId,
            region
        )
    }

    private val s3Client: AmazonS3Client by lazy {
        AmazonS3Client(credentialsProvider)
    }

    // Cache for storing downloaded images (memory and disk cache)
    private val imageCache = ConcurrentHashMap<String, Bitmap>()

    /**
     * Fetches the image from S3 and displays it in the given ImageView.
     * Uses caching to avoid redundant network requests.
     *
     * @param bucketName The name of the S3 bucket
     * @param objectKey The key of the image object in S3
     * @param imageView The ImageView where the image will be displayed
     */
    fun loadImage(bucketName: String, objectKey: String, imageView: ImageView) {
        // Check cache first
        imageCache[objectKey]?.let { cachedBitmap ->
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        // Fetch the image from S3 if not cached
        Thread {
            try {
                val s3Object = s3Client.getObject(GetObjectRequest(bucketName, objectKey))
                val inputStream: InputStream = s3Object.objectContent

                // Convert InputStream to Bitmap
                val bitmap = inputStreamToBitmap(inputStream)

                // Cache the image
                bitmap?.let {
                    imageCache[objectKey] = it
                }

                // Set the Bitmap to the ImageView on the UI thread
                (context as? android.app.Activity)?.runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * Converts InputStream to Bitmap
     */
    private fun inputStreamToBitmap(inputStream: InputStream): Bitmap? {
        val buffer = ByteArrayOutputStream()
        inputStream.copyTo(buffer)
        val byteArray = buffer.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    /**
     * Glide caching with Base64 images
     */
    fun loadBase64Image(base64Image: String, imageView: ImageView) {
        // First, check if the image is in the memory cache
        imageCache[base64Image]?.let { cachedBitmap ->
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        // Glide with base64 as URI
        Glide.with(context)
            .load("data:image/jpeg;base64,$base64Image")
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache both original and resized images
                .skipMemoryCache(false)  // Don't skip memory cache
            )
            .into(imageView)

        // Glide has internal caching, but let's manually add it to the cache for better control
        Glide.with(context)
            .asBitmap()
            .load("data:image/jpeg;base64,$base64Image")
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Cache the bitmap manually
                    imageCache[base64Image] = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle clearing
                }
            })
    }

    companion object {
        // Static properties for pool ID and region
        private lateinit var poolId: String
        private lateinit var region: Regions

        /**
         * Initializes the pool ID and region
         */
        fun initialize(poolId: String, region: Regions) {
            this.poolId = poolId
            this.region = region
        }

        /**
         * Creates an instance of S3ImageLoader
         */
        fun getInstance(context: Context): S3ImageLoader {
            return S3ImageLoader(context)
        }
    }
}


