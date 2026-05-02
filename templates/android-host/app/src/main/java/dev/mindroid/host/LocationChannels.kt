package dev.mindroid.host

import android.Manifest
import android.content.Context
import android.location.LocationManager

class LocationChannels(private val context: Context) : BridgeChannel {
    override val name: String = "location"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "getCurrentPosition" -> getCurrentPosition()
            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun getCurrentPosition(): Any {
        val coarse = PermissionGate.ensure(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fine = PermissionGate.ensure(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (coarse == "prompt" && fine == "prompt") {
            return mapOf("error" to "permission-required")
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = manager.getProviders(true)
        for (provider in providers) {
            val location = manager.getLastKnownLocation(provider) ?: continue
            return mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "accuracy" to location.accuracy,
                "provider" to provider,
                "timestamp" to location.time
            )
        }

        return mapOf("error" to "location-unavailable")
    }
}
