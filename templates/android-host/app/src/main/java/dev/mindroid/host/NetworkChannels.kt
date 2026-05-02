package dev.mindroid.host

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkChannels(private val context: Context) : BridgeChannel {
    override val name: String = "network"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = manager.activeNetwork
        val capabilities = manager.getNetworkCapabilities(active)

        return when (method) {
            "getState" -> mapOf(
                "connected" to (capabilities != null),
                "transport" to transportOf(capabilities),
                "metered" to manager.isActiveNetworkMetered
            )
            "getWifiInfo" -> mapOf(
                "ssid" to null,
                "ip" to null,
                "connected" to (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true)
            )
            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun transportOf(capabilities: NetworkCapabilities?): String {
        if (capabilities == null) {
            return "unknown"
        }

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            else -> "unknown"
        }
    }
}
