package dev.mindroid.host

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import java.util.concurrent.CopyOnWriteArrayList

class BluetoothChannels(private val context: Context) : BridgeChannel {
    override val name: String = "bluetooth"

    private val scanResults: CopyOnWriteArrayList<Map<String, Any?>> = CopyOnWriteArrayList()
    private var scanning = false
    private var scanCallback: ScanCallback? = null

    private fun adapter(): BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        val scanPerm = PermissionGate.ensure(context, Manifest.permission.BLUETOOTH_SCAN)
        val connectPerm = PermissionGate.ensure(context, Manifest.permission.BLUETOOTH_CONNECT)

        return when (method) {
            "isAvailable" -> mapOf(
                "available" to (adapter() != null),
                "enabled" to (adapter()?.isEnabled == true)
            )

            "scan" -> {
                if (scanPerm == "prompt" || connectPerm == "prompt") {
                    return mapOf("error" to "permission-required")
                }
                startScan()
                mapOf("ok" to true, "scanning" to true)
            }

            "stopScan" -> {
                stopScan()
                mapOf("ok" to true, "results" to scanResults.toList())
            }

            "scanResults" -> scanResults.toList()

            "connect" -> {
                if (connectPerm == "prompt") {
                    return mapOf("error" to "permission-required")
                }
                val id = args?.get("id") as? String ?: return mapOf("error" to "invalid-id")
                val adapter = adapter() ?: return mapOf("error" to "bluetooth-unavailable")
                val device = adapter.bondedDevices?.firstOrNull { it.address == id }
                    ?: return mapOf("error" to "device-not-bonded")
                mapOf("ok" to true, "name" to device.name, "address" to device.address)
            }

            "bondedDevices" -> {
                if (connectPerm == "prompt") {
                    return mapOf("error" to "permission-required")
                }
                adapter()?.bondedDevices?.map {
                    mapOf("name" to it.name, "address" to it.address)
                } ?: emptyList<Map<String, Any?>>()
            }

            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun startScan() {
        if (scanning) {
            return
        }
        scanResults.clear()
        val leScanner = adapter()?.bluetoothLeScanner ?: return
        scanning = true
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                val entry = mapOf(
                    "name" to device.name,
                    "address" to device.address,
                    "rssi" to result.rssi
                )
                if (scanResults.none { it["address"] == device.address }) {
                    scanResults.add(entry)
                }
            }
        }
        leScanner.startScan(scanCallback)
    }

    private fun stopScan() {
        if (!scanning) {
            return
        }
        scanning = false
        val callback = scanCallback ?: return
        adapter()?.bluetoothLeScanner?.stopScan(callback)
        scanCallback = null
    }
}
