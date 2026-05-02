package dev.mindroid.host

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationChannels(private val context: Context) : BridgeChannel {
    override val name: String = "notifications"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "notify" -> notify(args)
            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun notify(args: Map<String, Any?>?): Any {
        val channelId = args?.get("channelId") as? String ?: "mindroid_default"
        val title = args?.get("title") as? String ?: "Mindroid"
        val body = args?.get("body") as? String ?: "Notification"
        val id = (args?.get("id") as? Number)?.toInt() ?: 1001

        ensureChannel(channelId)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
        return mapOf("ok" to true, "id" to id)
    }

    private fun ensureChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(channelId)
        if (existing != null) {
            return
        }

        val channel = NotificationChannel(channelId, "Mindroid", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }
}
