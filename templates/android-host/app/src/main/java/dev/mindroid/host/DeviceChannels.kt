package dev.mindroid.host

import android.os.Build

class DeviceChannels : BridgeChannel {
    override val name: String = "device"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "getInfo" -> mapOf(
                "manufacturer" to Build.MANUFACTURER,
                "model" to Build.MODEL,
                "osVersion" to Build.VERSION.RELEASE,
                "apiLevel" to Build.VERSION.SDK_INT,
                "abi" to Build.SUPPORTED_ABIS.toList()
            )
            else -> mapOf("error" to "method-not-found")
        }
    }
}
