package dev.mindroid.host

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionGate {
    fun ensure(context: Context, permission: String): String {
        if (permission.isBlank()) {
            return "denied"
        }

        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        return if (granted) "granted" else "prompt"
    }
}
