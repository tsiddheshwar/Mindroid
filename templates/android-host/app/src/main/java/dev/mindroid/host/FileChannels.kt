package dev.mindroid.host

import android.content.Context
import java.io.File

class FileChannels(private val context: Context) : BridgeChannel {
    override val name: String = "storage"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "writeText" -> {
                val path = args?.get("path") as? String ?: return mapOf("error" to "invalid-path")
                val text = args["text"] as? String ?: ""
                val file = File(context.filesDir, path)
                file.parentFile?.mkdirs()
                file.writeText(text)
                mapOf("ok" to true, "path" to file.absolutePath)
            }
            "readText" -> {
                val path = args?.get("path") as? String ?: return mapOf("error" to "invalid-path")
                val file = File(context.filesDir, path)
                if (!file.exists()) {
                    return mapOf("error" to "not-found")
                }
                mapOf("path" to file.absolutePath, "text" to file.readText())
            }
            "list" -> {
                val path = args?.get("path") as? String ?: ""
                val dir = File(context.filesDir, path)
                if (!dir.exists() || !dir.isDirectory) {
                    return emptyList<String>()
                }
                dir.list()?.toList() ?: emptyList<String>()
            }
            else -> mapOf("error" to "method-not-found")
        }
    }
}
