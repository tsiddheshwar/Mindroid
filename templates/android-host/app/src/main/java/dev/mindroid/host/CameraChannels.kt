package dev.mindroid.host

import android.content.Context
import android.hardware.camera2.CameraManager

class CameraChannels(private val context: Context) : BridgeChannel {
    override val name: String = "camera"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        return when (method) {
            "isAvailable" -> mapOf("available" to context.packageManager.hasSystemFeature("android.hardware.camera.any"))
            "listCameras" -> manager.cameraIdList.toList()
            else -> mapOf("error" to "method-not-found")
        }
    }
}
