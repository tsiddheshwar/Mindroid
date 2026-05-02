package dev.mindroid.host

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract

class ContactsChannels(private val context: Context) : BridgeChannel {
    override val name: String = "contacts"

    override fun invoke(method: String, args: Map<String, Any?>?): Any {
        val readPerm = PermissionGate.ensure(context, Manifest.permission.READ_CONTACTS)
        val writePerm = PermissionGate.ensure(context, Manifest.permission.WRITE_CONTACTS)

        return when (method) {
            "list" -> {
                if (readPerm == "prompt") return mapOf("error" to "permission-required")
                queryContacts(args?.get("limit") as? Int ?: 100)
            }

            "search" -> {
                if (readPerm == "prompt") return mapOf("error" to "permission-required")
                val query = args?.get("query") as? String ?: return mapOf("error" to "invalid-query")
                searchContacts(query)
            }

            "create" -> {
                if (writePerm == "prompt") return mapOf("error" to "permission-required")
                val name = args?.get("name") as? String ?: return mapOf("error" to "invalid-name")
                val phone = args["phone"] as? String
                createContact(name, phone)
            }

            else -> mapOf("error" to "method-not-found")
        }
    }

    private fun queryContacts(limit: Int): List<Map<String, Any?>> {
        val results = mutableListOf<Map<String, Any?>>()
        val uri = ContactsContract.Contacts.CONTENT_URI.buildUpon()
            .appendQueryParameter(ContactsContract.LIMIT_PARAM_KEY, limit.toString())
            .build()
        context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            ),
            null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val hasPhoneIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
            while (cursor.moveToNext()) {
                val id = cursor.getString(idIdx)
                val hasPhone = cursor.getInt(hasPhoneIdx) > 0
                val phones = if (hasPhone) queryPhones(id) else emptyList()
                results.add(
                    mapOf(
                        "id" to id,
                        "name" to cursor.getString(nameIdx),
                        "phones" to phones
                    )
                )
            }
        }
        return results
    }

    private fun searchContacts(query: String): List<Map<String, Any?>> {
        val results = mutableListOf<Map<String, Any?>>()
        val uri = ContactsContract.Contacts.CONTENT_FILTER_URI.buildUpon()
            .appendPath(query)
            .build()
        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            null, null, null
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            while (cursor.moveToNext()) {
                results.add(mapOf("id" to cursor.getString(idIdx), "name" to cursor.getString(nameIdx)))
            }
        }
        return results
    }

    private fun queryPhones(contactId: String): List<String> {
        val phones = mutableListOf<String>()
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )?.use { cursor ->
            val numIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                phones.add(cursor.getString(numIdx))
            }
        }
        return phones
    }

    private fun createContact(name: String, phone: String?): Any {
        return try {
            val values = ContentValues().apply {
                putNull(ContactsContract.RawContacts.ACCOUNT_TYPE)
                putNull(ContactsContract.RawContacts.ACCOUNT_NAME)
            }
            val rawUri = context.contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, values)
                ?: return mapOf("error" to "insert-failed")
            val rawId = rawUri.lastPathSegment?.toLong() ?: return mapOf("error" to "invalid-id")

            val nameValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, rawId)
                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            }
            context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

            if (!phone.isNullOrBlank()) {
                val phoneValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                }
                context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)
            }
            mapOf("ok" to true, "id" to rawId)
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: "create-failed"))
        }
    }
}
