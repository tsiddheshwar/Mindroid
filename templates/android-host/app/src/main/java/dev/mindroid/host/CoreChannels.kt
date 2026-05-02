package dev.mindroid.host

import android.content.Context

class CoreChannels(
    private val context: Context,
    private val requestPermission: ((String, (Boolean) -> Unit) -> Unit)? = null
) : BridgeChannel {
    override val name: String = "permissions"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "ensure" -> {
                val permission = args?.get("permission") as? String ?: ""
                PermissionGate.ensure(context, permission)
            }
            "request" -> {
                val permission = args?.get("permission") as? String ?: ""
                if (permission.isBlank()) {
                    return mapOf("error" to "invalid-permission")
                }
                if (requestPermission == null) {
                    return mapOf("error" to "request-not-supported")
                }
                // Non-blocking: returns "pending"; JS must listen for the result event.
                requestPermission.invoke(permission) { granted ->
                    // Result is available for the next poll via "ensure"
                }
                mapOf("status" to "pending", "permission" to permission)
            }
            else -> mapOf("error" to "method-not-found")
        }
    }
}
