package dev.mindroid.host

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony

class SmsChannels(private val context: Context) : BridgeChannel {
    override val name: String = "sms"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        val readPerm = PermissionGate.ensure(context, Manifest.permission.READ_SMS)
        val sendPerm = PermissionGate.ensure(context, Manifest.permission.SEND_SMS)

        return when (method) {
            "send" -> {
                if (sendPerm == "prompt") return mapOf("error" to "permission-required")
                val to = args?.get("to") as? String ?: return mapOf("error" to "invalid-to")
                val message = args["message"] as? String ?: return mapOf("error" to "invalid-message")
                sendSms(to, message)
            }

            "inbox" -> {
                if (readPerm == "prompt") return mapOf("error" to "permission-required")
                queryInbox(args?.get("limit") as? Int ?: 50)
            }

            "sent" -> {
                if (readPerm == "prompt") return mapOf("error" to "permission-required")
                querySent(args?.get("limit") as? Int ?: 50)
            }

            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun sendSms(to: String, message: String): Any {
        return try {
            android.telephony.SmsManager.getDefault().sendTextMessage(to, null, message, null, null)
            mapOf("ok" to true, "to" to to)
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "send-failed"))
        }
    }

    private fun queryInbox(limit: Int): List<Map<String, Any?>> =
        querySmsUri(Telephony.Sms.Inbox.CONTENT_URI, limit)

    private fun querySent(limit: Int): List<Map<String, Any?>> =
        querySmsUri(Telephony.Sms.Sent.CONTENT_URI, limit)

    private fun querySmsUri(uri: Uri, limit: Int): List<Map<String, Any?>> {
        val results = mutableListOf<Map<String, Any?>>()
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )
        context.contentResolver.query(
            uri, projection, null, null, "${Telephony.Sms.DATE} DESC LIMIT $limit"
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            while (cursor.moveToNext()) {
                results.add(
                    mapOf(
                        "id" to cursor.getLong(idIdx),
                        "address" to cursor.getString(addressIdx),
                        "body" to cursor.getString(bodyIdx),
                        "date" to cursor.getLong(dateIdx),
                        "type" to cursor.getInt(typeIdx)
                    )
                )
            }
        }
        return results
    }
}
