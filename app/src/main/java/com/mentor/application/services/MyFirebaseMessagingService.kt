package com.mentor.application.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mentor.application.R
import com.mentor.application.repository.preferences.UserPrefsManager
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.comman.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.NotificationData
import com.mentor.application.utils.ApplicationGlobal
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.HomeActivity.Companion.NEW_INSTANT_REQUEST_INTENT
import com.mentor.application.views.comman.activities.MessageActivity
import com.mentor.application.views.comman.activities.NotificationActivity
import com.mentor.application.views.comman.activities.NotificationActivity.Companion.BUNDLE_NOTIFICATION_DATA
import com.mentor.application.views.comman.activities.NotificationActivity.Companion.BUNDLE_PUSH_TYPE
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.VIDEO_SESSION_INTENT
import com.mentor.application.views.customer.fragment.BookingDetailFragment.Companion.INTENT_BOOKING
import com.mentor.application.views.customer.fragment.HomeFragment.Companion.INTENT_NOTIFICATION
import com.mentor.application.views.customer.fragment.UpComingBookingsFragment.Companion.UPCOMING_BOOKING_INTENT
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.NEW_REQUEST_INTENT



class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        // Push notification types
        const val PUSH_TYPE_NEW_BOOKING = "1"
        const val PUSH_TYPE_ACCEPT_BOOKING = "2"
        const val PUSH_TYPE_REJECT_BOOKING = "3"
        const val PUSH_TYPE_BOOKING_COMPLETE = "4"
        const val PUSH_TYPE_REVIEW_ADDED = "5"
        const val PUSH_TYPE_SESSION_STARTED = "6"
        const val PUSH_TYPE_MESSAGE = "7"
        const val PUSH_TYPE_CANCEL_BOOKING = "8"
        const val PUSH_TYPE_RESCHEDULE_BOOKING = "9"
        const val PUSH_TYPE_VENDOR_CANCEL_BOOKING = "10"
        const val PUSH_TYPE_INSTANT_BOOKING = "11"
        const val PUSH_TYPE_CANCEL_INSTANT_BOOKING = "12"
        const val PUSH_TYPE_ADMIN = "13"
        const val PUSH_TYPE_JOIN_SESSION = "14"

        private const val PARAM_KEY_PUSH_TYPE = "push_type"
        private const val PARAM_KEY_MESSAGE_TO_DISPLAY = "body"
        private const val PARAM_KEY_TITLE = "title"
        private const val PARAM_TYPE_CONTRACT_ID = "contract_id"
        private const val PARAM_KEY_USER = "user"


        // Notification channel data
        private const val PACKAGE_NAME = "com.mentor.app"

        private const val CHANNEL_ID_BOOKING_NOTIFICATIONS = "$PACKAGE_NAME.bookingNotifications"
        private const val CHANNEL_NAME_BOOKING_NOTIFICATIONS = "Booking"

        private const val CHANNEL_ID_CONTRCT_REMINDER_NOTIFICATIONS =
            "$PACKAGE_NAME.contractReminderNotifications"
        private const val CHANNEL_NAME_CONTRCT_REMINDER_NOTIFICATIONS = "Contract Reminder"

        private const val CHANNEL_ID_ADMIN_NOTIFICATIONS = "$PACKAGE_NAME.adminNotifications"
        private const val CHANNEL_NAME_ADMIN_NOTIFICATIONS = "Admin"

         var isInstantNotificationGet = false

    }


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token)
    }

    private var mNotificationManager: NotificationManager? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val mapData = remoteMessage.data
        if (null != mapData && mapData.isNotEmpty()) {
            Log.e("remoteData", "onMessageReceived: " + mapData)

            if (null == mNotificationManager) {
                mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
            }

            if (mapData.containsKey(PARAM_KEY_PUSH_TYPE) &&
                mapData.containsKey(PARAM_KEY_MESSAGE_TO_DISPLAY)
            ) {
                if (UserPrefsManager(this).isLogin) {
                    when (mapData[PARAM_KEY_PUSH_TYPE]) {
                        PUSH_TYPE_NEW_BOOKING -> {
                            sendNewBookingNotifications(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_CANCEL_INSTANT_BOOKING -> {
                            sendCancelInstantBookingNotifications(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_INSTANT_BOOKING -> {
                            sendInstantBookingNotifications(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_ACCEPT_BOOKING, PUSH_TYPE_REJECT_BOOKING, PUSH_TYPE_VENDOR_CANCEL_BOOKING -> {
                            sendAcceptRejectBookingNotifications(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_BOOKING_COMPLETE -> {
                            sendBookingCompleteNotifications(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_REVIEW_ADDED -> {
                            sendReviewAddedNotifications(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_SESSION_STARTED -> {
                            sendSessionStartNotification(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_CANCEL_BOOKING -> {
                            sendBookingCancelNotifications(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_JOIN_SESSION -> {
                            joinSession(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_RESCHEDULE_BOOKING -> {
                            sendBookingRescheduleNotifications(
                                mapData[PARAM_TYPE_CONTRACT_ID] ?: "0",
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        PUSH_TYPE_MESSAGE -> {
                            sendMessageNotification(
                                mapData[PARAM_KEY_TITLE] ?: "0",
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                mapData[PARAM_KEY_USER] ?: "",
                                CHANNEL_ID_BOOKING_NOTIFICATIONS,
                                CHANNEL_NAME_BOOKING_NOTIFICATIONS
                            )
                        }

                        else -> {
                            sendGeneralNotification(
                                mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                                CHANNEL_ID_ADMIN_NOTIFICATIONS,
                                CHANNEL_NAME_ADMIN_NOTIFICATIONS
                            )
                        }
                    }
                } else if (PUSH_TYPE_ADMIN == mapData[PARAM_KEY_PUSH_TYPE]) {
                    sendGeneralNotification(
                        mapData[PARAM_KEY_MESSAGE_TO_DISPLAY] ?: "",
                        CHANNEL_ID_ADMIN_NOTIFICATIONS,
                        CHANNEL_NAME_ADMIN_NOTIFICATIONS
                    )
                }

            }
        }

        application.sendBroadcast(Intent(INTENT_NOTIFICATION))

    }

    private fun sendNewBookingNotifications(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {
        // Send broadcast
        application.sendBroadcast(Intent(NEW_REQUEST_INTENT))

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent =
                    Intent(this@MyFirebaseMessagingService, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }
    }

    private fun sendCancelInstantBookingNotifications(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {
        // Send broadcast
        application.sendBroadcast(Intent(NEW_REQUEST_INTENT))

//        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
//            with(data) {
//                val intent =
//                    Intent(this@MyFirebaseMessagingService, HomeActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
//                mNotificationManager?.notify(
//                    contractId.toInt(), getNotification(
//                        contentTitle = title,
//                        contentMessage = messageToDisplay,
//                        pendingIntent = PendingIntent
//                            .getActivity(
//                                this@MyFirebaseMessagingService,
//                                contractId.toInt(),
//                                intent,
//                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//                            ),
//                        channelId = channelId,
//                        channelName = channelName
//                    )
//                )
//            }
//        }
    }

    private fun sendInstantBookingNotifications(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {
        // Send broadcast
        isInstantNotificationGet=true
        if (ApplicationGlobal.mUserType==ApplicationGlobal.PROFESSIONAL){
            application.sendBroadcast(Intent(NEW_INSTANT_REQUEST_INTENT))
        }

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent =
                    Intent(this@MyFirebaseMessagingService, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }
    }


    private fun sendAcceptRejectBookingNotifications(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {

        application.sendBroadcast(Intent(UPCOMING_BOOKING_INTENT))

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent =
                    Intent(this@MyFirebaseMessagingService, NotificationActivity::class.java)
                intent.putExtra(BUNDLE_NOTIFICATION_DATA, data)
                intent.putExtra(BUNDLE_PUSH_TYPE, PUSH_TYPE_ACCEPT_BOOKING)
                intent.putExtra(BaseAppCompactActivity.INTENT_EXTRAS_IS_FROM_NOTIFICATION, true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }
    }

    private fun sendBookingCompleteNotifications(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {

        application.sendBroadcast(Intent(NEW_REQUEST_INTENT))
        application.sendBroadcast(Intent(VIDEO_SESSION_INTENT))

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent =
                    Intent(this@MyFirebaseMessagingService, NotificationActivity::class.java)
                intent.putExtra(BUNDLE_NOTIFICATION_DATA, data)
                intent.putExtra(BUNDLE_PUSH_TYPE, PUSH_TYPE_BOOKING_COMPLETE)
                intent.putExtra(BaseAppCompactActivity.INTENT_EXTRAS_IS_FROM_NOTIFICATION, true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }
    }

    private fun sendReviewAddedNotifications(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent =
                    Intent(this@MyFirebaseMessagingService, NotificationActivity::class.java)
                intent.putExtra(BUNDLE_NOTIFICATION_DATA, data)
                intent.putExtra(BUNDLE_PUSH_TYPE, PUSH_TYPE_REVIEW_ADDED)
                intent.putExtra(BaseAppCompactActivity.INTENT_EXTRAS_IS_FROM_NOTIFICATION, true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }
    }

    private fun sendBookingCancelNotifications(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {

        application.sendBroadcast(Intent(NEW_REQUEST_INTENT))

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent =
                    Intent(this@MyFirebaseMessagingService, NotificationActivity::class.java)
                intent.putExtra(BUNDLE_NOTIFICATION_DATA, data)
                intent.putExtra(BUNDLE_PUSH_TYPE, PUSH_TYPE_CANCEL_BOOKING)
                intent.putExtra(BaseAppCompactActivity.INTENT_EXTRAS_IS_FROM_NOTIFICATION, true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }
    }

    private fun joinSession(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {

        // Send broadcast
        application.sendBroadcast(Intent(INTENT_BOOKING))

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent = Intent(this@MyFirebaseMessagingService, NotificationActivity::class.java)
                intent.putExtra(BUNDLE_NOTIFICATION_DATA, data)
                intent.putExtra(BUNDLE_PUSH_TYPE, PUSH_TYPE_SESSION_STARTED)
                intent.putExtra(BaseAppCompactActivity.INTENT_EXTRAS_IS_FROM_NOTIFICATION, true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }
    }


    private fun sendBookingRescheduleNotifications(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {

        application.sendBroadcast(Intent(NEW_REQUEST_INTENT))

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent =
                    Intent(this@MyFirebaseMessagingService, NotificationActivity::class.java)
                intent.putExtra(BUNDLE_NOTIFICATION_DATA, data)
                intent.putExtra(BUNDLE_PUSH_TYPE, PUSH_TYPE_RESCHEDULE_BOOKING)
                intent.putExtra(BaseAppCompactActivity.INTENT_EXTRAS_IS_FROM_NOTIFICATION, true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }
    }

    private fun sendSessionStartNotification(
        contractId: String, title: String, messageToDisplay: String, userData: String,
        channelId: String, channelName: String
    ) {

        // Send broadcast
        application.sendBroadcast(Intent(INTENT_BOOKING))

        Gson().fromJson(userData, NotificationData::class.java)?.let { data ->
            with(data) {
                val intent =
                    Intent(this@MyFirebaseMessagingService, NotificationActivity::class.java)
                intent.putExtra(BUNDLE_NOTIFICATION_DATA, data)
                intent.putExtra(BUNDLE_PUSH_TYPE, PUSH_TYPE_SESSION_STARTED)
                intent.putExtra(BaseAppCompactActivity.INTENT_EXTRAS_IS_FROM_NOTIFICATION, true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                mNotificationManager?.notify(
                    contractId.toInt(), getNotification(
                        contentTitle = title,
                        contentMessage = messageToDisplay,
                        pendingIntent = PendingIntent
                            .getActivity(
                                this@MyFirebaseMessagingService,
                                contractId.toInt(),
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            ),
                        channelId = channelId,
                        channelName = channelName
                    )
                )
            }
        }

    }

    private fun sendMessageNotification(
        title: String, messageToDisplay: String, userData: String,
        channelId: String,
        channelName: String
    ) {

        Gson().fromJson(userData, NotificationData::class.java)?.let { conversation ->
            with(conversation) {
                if (bookingId != ApplicationGlobal.inChatConversationId) {

                    val booking = Booking(
                        _id = bookingId,
                        professionalName = userName,
                    )

                    val intent = Intent(
                        this@MyFirebaseMessagingService,
                        MessageActivity::class.java
                    )
                        .putExtra(MessageActivity.BUNDLE_BOOKING, booking)
                        .putExtra(BaseAppCompactActivity.INTENT_EXTRAS_IS_FROM_NOTIFICATION, true)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

                    mNotificationManager?.notify(
                        0, getNotification(
                            contentTitle = title, contentMessage = messageToDisplay,
                            pendingIntent = PendingIntent
                                .getActivity(
                                    this@MyFirebaseMessagingService,
                                    0,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                ),
                            channelId = channelId,
                            channelName = channelName
                        )
                    )

                }
            }
        }
    }

    private fun sendGeneralNotification(
        messageToDisplay: String,
        channelId: String, channelName: String
    ) {

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        mNotificationManager?.notify(
            0, getNotification(
                contentMessage = messageToDisplay,
                pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                ),
                channelId = channelId,
                channelName = channelName
            )
        )
    }

    @SuppressLint("NewApi")
    private fun getNotification(
        contentTitle: String = getString(R.string.app_name),
        contentMessage: String, pendingIntent: PendingIntent,
        channelId: String, channelName: String
    ): Notification {

//        val sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
//                packageName + "/" + R.raw.notification_sound)

        if (isOreoDevice && null == mNotificationManager?.getNotificationChannel(channelId)) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.setShowBadge(true)
//            notificationChannel.setSound(sound, AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            mNotificationManager?.createNotificationChannel(notificationChannel)
        }
        return NotificationCompat.Builder(
            this, channelId
        )
            .setContentTitle(contentTitle)
            .setContentText(contentMessage)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentMessage)
            )
            .setSmallIcon(getNotificationIcon())
            .setTicker(contentTitle)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
//                .setSound(sound)
            .setDefaults(Notification.DEFAULT_LIGHTS and Notification.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pendingIntent)
            .setAutoCancel(true).build()
    }

    private val isOreoDevice: Boolean
        get() = android.os.Build.VERSION_CODES.O <= android.os.Build.VERSION.SDK_INT

    private fun getNotificationIcon(): Int {
        return if (GeneralFunctions.isAboveLollipopDevice)
            R.mipmap.ic_launcher
        else
            R.mipmap.ic_launcher
    }
}