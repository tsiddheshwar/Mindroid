package dev.mindroid.host

import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var runtime: MindroidRuntime
    private lateinit var nfcChannels: NfcChannels
    private lateinit var jsEngine: JsEngine
    private lateinit var renderer: NativeRenderer

    private val pendingPermissionCallbacks = mutableMapOf<String, (Boolean) -> Unit>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (permission, granted) ->
            pendingPermissionCallbacks.remove(permission)?.invoke(granted)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val renderTarget = FrameLayout(this)
        renderer = NativeRenderer(this)
        jsEngine = createJsEngine()

        jsEngine.registerNativeFunction("__mindroid_render") { argsJson ->
            runOnUiThread { renderer.render(renderTarget, argsJson) }
            "{\"ok\":true}"
        }

        nfcChannels = NfcChannels(this)

        runtime = MindroidRuntime(this)
        runtime.register(CoreChannels(this, ::requestPermission))
        runtime.register(DeviceChannels())
        runtime.register(NetworkChannels(this))
        runtime.register(FileChannels(this))
        runtime.register(LocationChannels(this))
        runtime.register(NotificationChannels(this))
        runtime.register(CameraChannels(this))
        runtime.register(BluetoothChannels(this))
        runtime.register(nfcChannels)
        runtime.register(BiometricChannels(this))
        runtime.register(MediaChannels(this))
        runtime.register(BackgroundChannels(this))
        runtime.register(SmsChannels(this))
        runtime.register(ContactsChannels(this))

        requestCriticalPermissionsOnStart()

        findViewById<TextView>(R.id.statusText).text = "Mindroid runtime ready"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        handleNfcIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        jsEngine.destroy()
    }

    fun requestPermission(permission: String, callback: (Boolean) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            callback(true)
            return
        }
        pendingPermissionCallbacks[permission] = callback
        permissionLauncher.launch(arrayOf(permission))
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            @Suppress("DEPRECATION")
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
            nfcChannels.onTagDiscovered(tag)
        }
    }

    private fun requestCriticalPermissionsOnStart() {
        val critical = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.CAMERA
        )
        val needed = critical.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}
