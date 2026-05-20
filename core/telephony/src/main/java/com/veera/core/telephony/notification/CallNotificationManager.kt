package com.veera.core.telephony.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
)
{
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "incoming_calls"
    private val ONGOING_CHANNEL_ID = "ongoing_calls"
    
    val INCOMING_NOTIFICATION_ID = 1001
    val ONGOING_NOTIFICATION_ID = 1002

    private fun createProfileBitmap(name: String): android.graphics.Bitmap {
        val size = 150
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 60f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val text = if (name.isNotBlank()) name.take(1).uppercase() else "?"
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        canvas.drawText(text, size / 2f, size / 2f + textBounds.height() / 2f, textPaint)
        
        return bitmap
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val incomingChannel = NotificationChannel(
                CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows incoming call notifications"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                val ringtoneUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setSound(ringtoneUri, audioAttributes)
            }
            
            val ongoingChannel = NotificationChannel(
                ONGOING_CHANNEL_ID,
                "Ongoing Calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing call status"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }
            
            notificationManager.createNotificationChannel(incomingChannel)
            notificationManager.createNotificationChannel(ongoingChannel)
        }
    }

    /**
     * Resolves the contact photo from a URI string.
     * Uses ImageDecoder on modern Android versions (P+) and MediaStore for older versions.
     * 
     * @param uriString The URI of the contact photo as a string.
     * @return A Bitmap of the contact photo, or null if it cannot be loaded.
     */
    private fun getPhotoBitmap(uriString: String?): android.graphics.Bitmap? {
        if (uriString == null) return null
        return try {
            val uri = android.net.Uri.parse(uriString)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Builds the notification specifically for an INCOMING call.
     * Android 12+ requires using NotificationCompat.CallStyle.forIncomingCall.
     * We explicitly set SubText, ContentTitle, and ContentText so that OEM Dynamic Islands 
     * can intercept and display these status strings.
     *
     * @param callerName The resolved name of the caller (or "Unknown").
     * @param callerNumber The phone number of the incoming call.
     * @param photoUri Optional photo URI for the caller.
     * @return The built Notification ready for foreground service or notification manager.
     */

    fun buildIncomingCallNotification(callerName: String, callerNumber: String, photoUri: String? = null): Notification {
        val fullScreenIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val answerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_ANSWER"
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val declineIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_DECLINE"
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val largeIcon = getPhotoBitmap(photoUri) ?: createProfileBitmap(callerName)
        val iconCompat = IconCompat.createWithBitmap(largeIcon)

        val caller = Person.Builder()
            .setName(callerName)
            .setIcon(iconCompat)
            .setImportant(true)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, declinePendingIntent, answerPendingIntent))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentTitle(callerName)
            .setContentText("Incoming Call")
            .setSubText("Incoming Call")
            .setTicker("Incoming Call")
            .build()

        return notification
    }

    /**
     * Builds the notification for OUTGOING and ACTIVE (ongoing) calls.
     * Android 12+ requires NotificationCompat.CallStyle.forOngoingCall for active/dialing calls.
     * 
     * @param callerName The name of the person being called.
     * @param callerNumber The phone number.
     * @param isSpeakerOn Boolean representing if the speakerphone is currently active.
     * @param connectTime The epoch time in milliseconds when the call connected. Null if dialing.
     * @param photoUri Optional photo URI for the contact.
     * @param isDialing True if the call is still establishing (dialing). Hides the timer.
     * @return The built Notification.
     */
    fun buildOngoingCallNotification(callerName: String, callerNumber: String, isSpeakerOn: Boolean, connectTime: Long?, photoUri: String?, isDialing: Boolean = false): Notification {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_SHOW_ONGOING_CALL", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val speakerIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_TOGGLE_SPEAKER"
        }
        val speakerPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            speakerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val endCallIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = "ACTION_DISCONNECT"
        }
        val endCallPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            endCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val speakerText = if (isSpeakerOn) "Speaker Off" else "Speaker On"
        val speakerIcon = if (isSpeakerOn) android.R.drawable.stat_sys_speakerphone else android.R.drawable.ic_btn_speak_now

        val largeIcon = getPhotoBitmap(photoUri) ?: createProfileBitmap(callerName)
        val iconCompat = IconCompat.createWithBitmap(largeIcon)

        val caller = Person.Builder()
            .setName(callerName)
            .setIcon(iconCompat)
            .setImportant(true)
            .build()

        val builder = NotificationCompat.Builder(context, ONGOING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setStyle(NotificationCompat.CallStyle.forOngoingCall(caller, endCallPendingIntent))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // User requested to close notification on click
            .addAction(speakerIcon, speakerText, speakerPendingIntent)
            .setContentTitle(callerName)

        val stateText = if (isDialing) "Dialing..." else "Ongoing Call"
        builder.setContentText(stateText)
        builder.setSubText(stateText)
        builder.setTicker(stateText)

        if (!isDialing && connectTime != null && connectTime > 0) {
            builder.setWhen(connectTime)
            builder.setUsesChronometer(true)
            builder.setShowWhen(true)
        }

        return builder.build()


    }

    /**
     * Forces the NotificationManager to immediately broadcast the updated notification.
     */
    fun updateIncomingNotification(notification: Notification) {
        notificationManager.notify(INCOMING_NOTIFICATION_ID, notification)
    }

    fun updateOngoingNotification(notification: Notification) {
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification)
    }

    /**
     * Clears the call notifications from the system.
     */
    fun cancelIncomingNotification() {
        notificationManager.cancel(INCOMING_NOTIFICATION_ID)
    }

    fun cancelAllNotifications() {
        notificationManager.cancel(INCOMING_NOTIFICATION_ID)
        notificationManager.cancel(ONGOING_NOTIFICATION_ID)
    }
}
