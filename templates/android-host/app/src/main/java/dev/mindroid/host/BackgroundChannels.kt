package dev.mindroid.host

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

const val BACKGROUND_CHANNEL_ID = "mindroid_bg"

class BackgroundChannels(private val context: Context) : BridgeChannel {
    override val name: String = "background"

    private val runningServices = mutableMapOf<String, ComponentName?>()

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "startService" -> {
                val serviceName = args?.get("name") as? String ?: return mapOf("error" to "invalid-name")
                val strategy = args["strategy"] as? String ?: "foreground"
                startService(serviceName, strategy)
            }

            "stopService" -> {
                val serviceName = args?.get("name") as? String ?: return mapOf("error" to "invalid-name")
                stopService(serviceName)
            }

            "listRunning" -> runningServices.keys.toList()

            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun startService(name: String, strategy: String): Any {
        ensureChannel()
        val intent = Intent(context, MindroidBackgroundService::class.java).apply {
            putExtra("serviceName", name)
            putExtra("strategy", strategy)
        }
        if (strategy == "foreground") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.startService(intent)
        }
        runningServices[name] = null
        return mapOf("ok" to true, "name" to name, "strategy" to strategy)
    }

    private fun stopService(name: String): Any {
        val intent = Intent(context, MindroidBackgroundService::class.java).apply {
            putExtra("serviceName", name)
        }
        context.stopService(intent)
        runningServices.remove(name)
        return mapOf("ok" to true, "name" to name)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(BACKGROUND_CHANNEL_ID) != null) {
            return
        }
        manager.createNotificationChannel(
            NotificationChannel(BACKGROUND_CHANNEL_ID, "Mindroid Background", NotificationManager.IMPORTANCE_LOW)
        )
    }
}

class MindroidBackgroundService : Service() {
    inner class LocalBinder : Binder() {
        fun service() = this@MindroidBackgroundService
    }

    private val binder = LocalBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val name = intent?.getStringExtra("serviceName") ?: "mindroid"
        val strategy = intent?.getStringExtra("strategy") ?: "foreground"

        if (strategy == "foreground") {
            val notification: Notification = NotificationCompat.Builder(this, BACKGROUND_CHANNEL_ID)
                .setContentTitle("Mindroid: $name")
                .setContentText("Running in background")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            startForeground(name.hashCode().coerceIn(1, Int.MAX_VALUE), notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder
}
