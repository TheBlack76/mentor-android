package com.mentor.application.views.comman.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.mentor.application.R
import com.mentor.application.databinding.FragmentVideoSessionBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.SessionData
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.MessageViewModel
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_BOOKING
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_RESPONSE_TYPE
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_TYPE_REFRESH
import com.mentor.application.views.customer.fragment.DashboardFragment.Companion.BUNDLE_TAB_BOOKINGS
import com.mentor.application.views.customer.fragment.DashboardFragment.Companion.BUNDLE_TAB_NUMBER
import com.opentok.android.AudioDeviceManager
import com.opentok.android.BaseAudioDevice
import com.opentok.android.BaseVideoRenderer
import com.opentok.android.OpentokError
import com.opentok.android.Publisher
import com.opentok.android.PublisherKit
import com.opentok.android.PublisherKit.PublisherListener
import com.opentok.android.Session
import com.opentok.android.Session.SessionListener
import com.opentok.android.Stream
import com.opentok.android.Subscriber
import com.opentok.android.SubscriberKit
import com.opentok.android.SubscriberKit.SubscriberListener
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import kotlin.math.hypot

@AndroidEntryPoint
class VideoSessionActivity :
    BaseAppCompactActivity<FragmentVideoSessionBinding>(FragmentVideoSessionBinding::inflate),
    OnClickListener, EasyPermissions.PermissionCallbacks {

    companion object {
        const val VIDEO_SESSION_INTENT = "videoSessionIntent"

        const val BOOKING_TYPE = "bookingType"
        const val BOOKING_TYPE_INSTANT = 0
        const val BOOKING_TYPE_NON_INSTANT = 1
        const val BOOKING_SESSION = "bookingSession"

        private val TAG = MainActivity::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 124
        const val BUNDLE_BOOKING = "booking"
        const val SIGNAL_TYPE_COMPLETE = "signalComplete"
    }

    private val mViewModel: MessageViewModel by viewModels()

    private var mBooking = Booking()
    private var session: Session? = null
    private var publisher: Publisher? = null
    private var subscriber: Subscriber? = null
    private var isSpeaker = true
    private var isSwapped = true

    private var appId = ""
    private var sessionId = ""
    private var token = ""
    private var mBookingType= BOOKING_TYPE_NON_INSTANT


    override fun init() {
        // Get intent
        mBooking = intent.getParcelableExtra(BUNDLE_BOOKING) ?: Booking()
        mBookingType = intent.getIntExtra(BOOKING_TYPE,BOOKING_TYPE_NON_INSTANT)

        if (intent.getIntExtra(
                 BOOKING_TYPE,
                 BOOKING_TYPE_NON_INSTANT
            ) == BOOKING_TYPE_NON_INSTANT
        ) {
            // Api call
            mViewModel.joinSession(mBooking._id)
        } else {
            val mSession = intent.getParcelableExtra(BOOKING_SESSION) ?: SessionData()

            appId = mSession.appId
            sessionId = mSession.sessionId
            token = mSession.token

            // Request for permission to start the session
            requestPermissions()
        }

        // Set click listener
        binding.ivRejectCall.setOnClickListener(this)
        binding.ivCycleCamera.setOnClickListener(this)
        binding.ivRejectCall.setOnClickListener(this)
        binding.ivDisableVideo.setOnClickListener(this)
        binding.ivMute.setOnClickListener(this)
        binding.ivSpeaker.setOnClickListener(this)
        binding.ivSpeaker.setOnClickListener(this)

        initObserver()
        movePublisherView()
    }

    override val isMakeStatusBarTransparent: Boolean
        get() = false

    override val navHostFragment: NavHostFragment?
        get() = null

    private val publisherListener: PublisherListener = object : PublisherListener {
        override fun onStreamCreated(publisherKit: PublisherKit, stream: Stream) {
            Log.d(TAG, "onStreamCreated: Publisher Stream Created. Own stream ${stream.streamId}")
        }

        override fun onStreamDestroyed(publisherKit: PublisherKit, stream: Stream) {
            Log.d(
                TAG,
                "onStreamDestroyed: Publisher Stream Destroyed. Own stream ${stream.streamId}"
            )
        }

        override fun onError(publisherKit: PublisherKit, opentokError: OpentokError) {
            Log.d(
                TAG,
                "onStreamError: Publisher Stream Destroyed. Own stream ${opentokError.message}"
            )
        }
    }

    private val sessionListener: SessionListener = object : SessionListener {
        override fun onConnected(session: Session) {
            publisher = Publisher.Builder(this@VideoSessionActivity).build()
            publisher?.setPublisherListener(publisherListener)
            publisher?.renderer?.setStyle(
                BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL
            )
            binding.publisherView.addView(publisher?.view)
            if (publisher?.view is GLSurfaceView) {
                (publisher?.view as GLSurfaceView).apply {
                    setZOrderOnTop(true)
                }
            }
            session.publish(publisher)

            binding.tvPleaseWait.visibility = View.VISIBLE
            binding.tvConnecting.visibility = View.GONE
        }

        override fun onDisconnected(session: Session) {
            Log.d(TAG, "onDisconnected: Disconnected from session: ${session.sessionId}")
        }

        override fun onStreamReceived(session: Session, stream: Stream) {
            Log.d(
                TAG,
                "onStreamReceived: New Stream Received ${stream.streamId} in session: ${session.sessionId}"
            )
            if (subscriber == null) {
                subscriber = Subscriber.Builder(this@VideoSessionActivity, stream).build().also {
                    it.renderer?.setStyle(
                        BaseVideoRenderer.STYLE_VIDEO_SCALE,
                        BaseVideoRenderer.STYLE_VIDEO_FILL
                    )

                    it.setSubscriberListener(subscriberListener)
                }

                session.subscribe(subscriber)
                binding.subscriberView.addView(subscriber?.view)
                binding.tvPleaseWait.visibility = View.GONE
            }
        }

        override fun onStreamDropped(session: Session, stream: Stream) {
            Log.d(
                TAG,
                "onStreamDropped: Stream Dropped: ${stream.streamId} in session: ${session.sessionId}"
            )
            if (subscriber != null) {
                subscriber = null
                binding.subscriberView.removeAllViews()
                binding.tvPleaseWait.visibility = View.VISIBLE
            }
        }

        override fun onError(session: Session, opentokError: OpentokError) {
            Log.d(
                TAG,
                "Session error: ${opentokError.message}"
            )
        }
    }

    var subscriberListener: SubscriberListener = object : SubscriberListener {
        override fun onConnected(subscriberKit: SubscriberKit) {
            Log.d(
                TAG,
                "onConnected: Subscriber connected. Stream: ${subscriberKit.stream.streamId}"
            )
        }

        override fun onDisconnected(subscriberKit: SubscriberKit) {
            Log.d(
                TAG,
                "onDisconnected: Subscriber disconnected. Stream: ${subscriberKit.stream.streamId}"
            )
        }

        override fun onError(subscriberKit: SubscriberKit, opentokError: OpentokError) {
            Log.d(
                TAG,
                "SubscriberKit onError: ${opentokError.message}"
            )
        }
    }

    private val signalListener: Session.SignalListener =
        Session.SignalListener { session, type, data, connection ->
            when (type) {
                SIGNAL_TYPE_COMPLETE -> {
                    sendBroadcast(
                        Intent(INTENT_BOOKING).putExtra(
                            INTENT_RESPONSE_TYPE, INTENT_TYPE_REFRESH
                        )
                    )
                    finish()
                }

                else -> {

                }
            }
        }


    override fun onPause() {
        super.onPause()
        session?.onPause()

        // Initialize receiver
        unregisterReceiver(
            mGetUpdateDataBroadcastReceiver
        )
    }

    override fun onResume() {
        super.onResume()
        session?.onResume()

        // Initialize receiver
        registerReceiver(
            mGetUpdateDataBroadcastReceiver,
            IntentFilter(VIDEO_SESSION_INTENT), Context.RECEIVER_EXPORTED
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsGranted:$requestCode: $perms")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsGranted: $requestCode: $perms")
    }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_CODE)
    private fun requestPermissions() {
        val perms = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            initializeSession(appId, sessionId, token)
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.rationale_video_app),
                PERMISSIONS_REQUEST_CODE,
                *perms
            )
        }
    }

    private fun initializeSession(apiKey: String, sessionId: String, token: String) {
        Log.i(TAG, "apiKey: $apiKey")
        Log.i(TAG, "sessionId: $sessionId")
        Log.i(TAG, "token: $token")

        /*
        The context used depends on the specific use case, but usually, it is desired for the session to
        live outside of the Activity e.g: live between activities. For a production applications,
        it's convenient to use Application context instead of Activity context.
         */
        session = Session.Builder(this, apiKey, sessionId).build().also {
            it.setSessionListener(sessionListener)
            it.setSignalListener(signalListener)
            it.connect(token)
        }
    }

    private fun finishWithMessage(message: String) {
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivRejectCall -> {
                if (mBookingType == BOOKING_TYPE_INSTANT) {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra(BUNDLE_TAB_NUMBER, BUNDLE_TAB_BOOKINGS)
                    startActivity(intent)
                }

                finish()
            }

            R.id.ivDisableVideo -> {
                if (publisher?.publishVideo == true) {
                    publisher?.publishVideo = false
                    binding.ivDisableVideo.setImageResource(R.drawable.ic_disable_video)
                } else {
                    publisher?.publishVideo = true
                    binding.ivDisableVideo.setImageResource(R.drawable.ic_enable_video)
                }
            }

            R.id.ivMute -> {
                if (publisher?.publishAudio == true) {
                    publisher?.publishAudio = false
                    binding.ivMute.setImageResource(R.drawable.ic_mute)
                } else {
                    publisher?.publishAudio = true
                    binding.ivMute.setImageResource(R.drawable.ic_unmute)
                }
            }

            R.id.ivCycleCamera -> {
                if (publisher == null) {
                    return
                }
                publisher?.cycleCamera()
            }

            R.id.ivSpeaker -> {
                // Set audio manager
                if (isSpeaker) {
                    binding.ivSpeaker.setImageResource(R.drawable.ic_headset)
                    AudioDeviceManager.getAudioDevice().outputMode =
                        BaseAudioDevice.OutputMode.Handset
                    isSpeaker = false
                } else {
                    binding.ivSpeaker.setImageResource(R.drawable.ic_speaker)
                    AudioDeviceManager.getAudioDevice().outputMode =
                        BaseAudioDevice.OutputMode.SpeakerPhone
                    isSpeaker = true
                }
            }

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun movePublisherView() {
        // Variables to store the initial touch position and the view's position
        var dX = 0f
        var dY = 0f

        // GestureDetector to handle single tap (click)
        val gestureDetector = GestureDetector(
            binding.publisherView.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    swapViews()
                    return true
                }
            })

        binding.publisherView.setOnTouchListener { view, event ->
            // Pass the event to GestureDetector first for click detection
            gestureDetector.onTouchEvent(event)

            val parent = view.parent as View
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Capture the initial offset for dragging
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    // Update the view's position as it is dragged
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }

                MotionEvent.ACTION_UP -> {
                    // Define margins
                    val topMargin = 32f
                    val bottomMargin = 316f
                    val horizontalMargin = 32f

                    // Determine the final position of the view
                    val viewX = view.x
                    val viewY = view.y

                    // Calculate corner positions with specified margins
                    val topLeftX = horizontalMargin
                    val topLeftY = topMargin
                    val topRightX = parent.width - view.width - horizontalMargin
                    val topRightY = topMargin
                    val bottomLeftX = horizontalMargin
                    val bottomLeftY = parent.height - view.height - bottomMargin
                    val bottomRightX = topRightX
                    val bottomRightY = bottomLeftY

                    // Calculate distances to each corner
                    val distances = mapOf(
                        Pair(topLeftX, topLeftY) to distance(viewX, viewY, topLeftX, topLeftY),
                        Pair(topRightX, topRightY) to distance(viewX, viewY, topRightX, topRightY),
                        Pair(bottomLeftX, bottomLeftY) to distance(
                            viewX,
                            viewY,
                            bottomLeftX,
                            bottomLeftY
                        ),
                        Pair(bottomRightX, bottomRightY) to distance(
                            viewX,
                            viewY,
                            bottomRightX,
                            bottomRightY
                        )
                    )

                    // Find the nearest corner
                    val nearestCorner = distances.minByOrNull { it.value }?.key
                    if (nearestCorner != null) {
                        view.animate()
                            .x(nearestCorner.first)
                            .y(nearestCorner.second)
                            .setDuration(100) // Snap effect duration
                            .start()
                    }
                }
            }
            true
        }
    }

    // Distance function
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()
    }


    private fun swapViews() {
        if (isSwapped) {
            binding.publisherView.removeAllViews()
            binding.subscriberView.removeAllViews()

            binding.publisherView.addView(subscriber?.view ?: View(this))
            binding.subscriberView.addView(publisher?.view ?: View(this))

            if (publisher?.view is GLSurfaceView) {
                (publisher?.view as GLSurfaceView).apply {
                    setZOrderOnTop(false)
                }
            }

            if (subscriber?.view is GLSurfaceView) {
                (subscriber?.view as GLSurfaceView).apply {
                    setZOrderOnTop(true)
                }
            }

            isSwapped = false
        } else {
            binding.publisherView.removeAllViews()
            binding.subscriberView.removeAllViews()

            binding.publisherView.addView(publisher?.view ?: View(this))
            binding.subscriberView.addView(subscriber?.view ?: View(this))

            if (publisher?.view is GLSurfaceView) {
                (publisher?.view as GLSurfaceView).apply {
                    setZOrderOnTop(true)
                }
            }

            if (subscriber?.view is GLSurfaceView) {
                (subscriber?.view as GLSurfaceView).apply {
                    setZOrderOnTop(false)
                }
            }

            isSwapped = true
        }
    }

    private fun initObserver() {
        mViewModel.onGetSessionData().observe(this) {
            appId = it.appId
            sessionId = it.sessionId
            token = it.token

            // Request for permission to start the session
            requestPermissions()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectSession()
    }

    private fun disconnectSession() {
        if (session == null) {
            return
        }

        session?.unsubscribe(subscriber)
        session?.unpublish(publisher)
        subscriber = null
        publisher = null

        session?.disconnect()
    }

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    finish()
                } catch (e: Exception) {
                    println(e)

                }
            }
        }
    }

}








