package capstone.safeline.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import capstone.safeline.R

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "safeline_messages"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "New Messages",
                NotificationManager.IMPORTANCE_HIGH // HIGH makes it pop up on the screen
            ).apply {
                description = "Notifications for new incoming messages"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewMessageNotification(senderName: String, messageText: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismisses the notification when the user clicks it
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
}