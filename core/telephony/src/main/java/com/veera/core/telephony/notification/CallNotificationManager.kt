package com.veera.core.telephony.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
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
    private val NOTIFICATION_ID = 1001

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
                setSound(null, null)
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

    fun showIncomingCallNotification(callerName: String, callerNumber: String, photoUri: String? = null) {
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setLargeIcon(largeIcon)
            .setContentTitle("Incoming Call")
            .setContentText("$callerName • $callerNumber")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(android.R.drawable.ic_menu_call, "Answer", answerPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", declinePendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showOngoingCallNotification(callerName: String, callerNumber: String, isSpeakerOn: Boolean, connectTime: Long, photoUri: String? = null) {
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

        val notification = NotificationCompat.Builder(context, ONGOING_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setLargeIcon(largeIcon)
            .setContentTitle("Ongoing Call")
            .setContentText("$callerName • $callerNumber")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setWhen(connectTime)
            .setUsesChronometer(true)
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // User requested to close notification on click
            .addAction(speakerIcon, speakerText, speakerPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Call", endCallPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
