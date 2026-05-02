package dev.mindroid.host

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.NdefMessage
import android.nfc.NdefRecord

class NfcChannels(private val context: Context) : BridgeChannel {
    override val name: String = "nfc"

    private val adapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(context) }
    private var lastTag: Tag? = null

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        return when (method) {
            "isAvailable" -> mapOf(
                "available" to (adapter != null),
                "enabled" to (adapter?.isEnabled == true)
            )

            "readTag" -> {
                val tag = lastTag ?: return mapOf("error" to "no-tag-present")
                readNdef(tag)
            }

            "writeTag" -> {
                val tag = lastTag ?: return mapOf("error" to "no-tag-present")
                val payload = args?.get("payload") as? String ?: return mapOf("error" to "invalid-payload")
                writeNdef(tag, payload)
            }

            else -> mapOf("error" to "method-not-found")
        }
    }

    fun onTagDiscovered(tag: Tag) {
        lastTag = tag
    }

    private fun readNdef(tag: Tag): Any {
        val ndef = Ndef.get(tag) ?: return mapOf("error" to "ndef-not-supported")
        return try {
            ndef.connect()
            val message: NdefMessage? = ndef.ndefMessage
            val records = message?.records?.map { record ->
                mapOf(
                    "type" to String(record.type),
                    "payload" to String(record.payload)
                )
            } ?: emptyList()
            mapOf("ok" to true, "records" to records)
        } finally {
            if (ndef.isConnected) {
                ndef.close()
            }
        }
    }

    private fun writeNdef(tag: Tag, payload: String): Any {
        val ndef = Ndef.get(tag) ?: return mapOf("error" to "ndef-not-supported")
        return try {
            ndef.connect()
            if (!ndef.isWritable) {
                return mapOf("error" to "tag-read-only")
            }
            val record = NdefRecord.createTextRecord("en", payload)
            val message = NdefMessage(arrayOf(record))
            ndef.writeNdefMessage(message)
            mapOf("ok" to true)
        } finally {
            if (ndef.isConnected) {
                ndef.close()
            }
        }
    }
}
