package com.veera.core.telephony.repository

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class CallLogEntry(
    val id: String,
    val name: String?,
    val number: String,
    val date: Long,
    val type: Int,
    val duration: Long,
    val photoUri: String? = null,
    val contactId: String? = null
)

data class DialpadSuggestion(
    val name: String,
    val number: String,
    val source: String, // "Recent" or "Contact"
    val photoUri: String? = null,
    val contactId: String? = null
)

@Singleton
class CallLogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getCallLogs(page: Int, pageSize: Int): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntry>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_PHOTO_URI,
            CallLog.Calls.CACHED_LOOKUP_URI
        )

        val sortOrder = "${CallLog.Calls.DATE} DESC"
        
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val startIndex = page * pageSize
                if (it.moveToPosition(startIndex)) {
                    var count = 0
                    do {
                        val id = it.getString(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                        var name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                        val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                        val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                        val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                        val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                        var photoUri = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI))

                        if (name.isNullOrEmpty()) {
                            name = getContactName(number)
                        }
                        
                        if (photoUri.isNullOrEmpty()) {
                            photoUri = getContactPhotoUri(number)
                        }
                        
                        val contactId = getContactId(number)

                        list.add(CallLogEntry(id, name, number, date, type, duration, photoUri, contactId))
                        count++
                    } while (it.moveToNext() && count < pageSize)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    suspend fun searchCallLogs(query: String, page: Int, pageSize: Int): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntry>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.CACHED_PHOTO_URI
        )

        val selection = "${CallLog.Calls.NUMBER} LIKE ? OR ${CallLog.Calls.CACHED_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%", "%$query%")
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val startIndex = page * pageSize
                if (it.moveToPosition(startIndex)) {
                    var count = 0
                    do {
                        val id = it.getString(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                        var name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                        val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                        val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                        val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                        val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                        var photoUri = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI))

                        if (name.isNullOrEmpty()) {
                            name = getContactName(number)
                        }
                        
                        if (photoUri.isNullOrEmpty()) {
                            photoUri = getContactPhotoUri(number)
                        }
                        
                        val contactId = getContactId(number)

                        list.add(CallLogEntry(id, name, number, date, type, duration, photoUri, contactId))
                        count++
                    } while (it.moveToNext() && count < pageSize)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    suspend fun getTotalCallLogCount(): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls._ID),
                null,
                null,
                null
            )
            count = cursor?.count ?: 0
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        count
    }

    suspend fun getDialpadSuggestions(query: String, page: Int, pageSize: Int): List<DialpadSuggestion> = withContext(Dispatchers.IO) {
        val suggestions = mutableListOf<DialpadSuggestion>()
        if (query.isEmpty()) return@withContext emptyList()

        // 1. Search in Recent Calls
        val recentProjection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_PHOTO_URI
        )
        val recentSelection = "${CallLog.Calls.NUMBER} LIKE ?"
        val recentArgs = arrayOf("%$query%")
        
        try {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                recentProjection,
                recentSelection,
                recentArgs,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                val startIndex = page * pageSize
                if (cursor.moveToPosition(startIndex)) {
                    var count = 0
                    do {
                        val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                        var name = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                        var photoUri = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_PHOTO_URI))
                        
                        if (name.isNullOrEmpty()) name = getContactName(number)
                        if (photoUri.isNullOrEmpty()) photoUri = getContactPhotoUri(number)
                        val contactId = getContactId(number)
                        
                        suggestions.add(DialpadSuggestion(name ?: number, number, "Recent", photoUri, contactId))
                        count++
                    } while (cursor.moveToNext() && count < pageSize)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        // 2. Search in Contacts (if suggestions from recents are not enough)
        if (suggestions.size < (page + 1) * pageSize) {
            val contactProjection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            )
            val contactSelection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            val contactArgs = arrayOf("%$query%", "%$query%")
            
            try {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    contactProjection,
                    contactSelection,
                    contactArgs,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext() && suggestions.size < (page + 1) * pageSize) {
                        val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        val number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI))
                        val contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                        
                        // Avoid duplicates
                        if (suggestions.none { it.number == number }) {
                            suggestions.add(DialpadSuggestion(name, number, "Contact", photoUri, contactId))
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        suggestions.distinctBy { it.number }
    }

    fun getContactId(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup._ID)
        var contactId: String? = null
        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contactId
    }

    fun getContactName(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName: String? = null
        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    contactName = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contactName
    }

    fun getContactPhotoUri(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.PHOTO_URI)
        var photoUri: String? = null
        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    photoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return photoUri
    }
}
