package dev.mindroid.host

import android.content.Context

interface BridgeChannel {
    val name: String
    fun invoke(method: String, args: Map<String, Any?>?): Any?
}

class MindroidRuntime(private val context: Context) {
    private val channels = mutableMapOf<String, BridgeChannel>()

    fun register(channel: BridgeChannel) {
        channels[channel.name] = channel
    }

    fun invoke(channel: String, method: String, args: Map<String, Any?>? = null): Any? {
        val target = channels[channel] ?: return mapOf("error" to "channel-not-found")
        return target.invoke(method, args)
    }
}
