package com.veera.core.telephony.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.veera.core.telephony.model.Contact
import com.veera.core.telephony.model.ContactAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getContacts(
        query: String = "",
        accountName: String? = null,
        accountType: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE
        )

        val selectionList = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (query.isNotEmpty()) {
            selectionList.add("${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?")
            selectionArgs.add("%$query%")
        }

        if (accountName != null && accountType != null) {
            selectionList.add("${ContactsContract.RawContacts.ACCOUNT_NAME} = ?")
            selectionArgs.add(accountName)
            selectionList.add("${ContactsContract.RawContacts.ACCOUNT_TYPE} = ?")
            selectionArgs.add(accountType)
        } else if (accountType != null) {
            // Filter by type only (e.g. SIM)
            selectionList.add("${ContactsContract.RawContacts.ACCOUNT_TYPE} LIKE ?")
            selectionArgs.add("%$accountType%")
        }

        val selection = if (selectionList.isEmpty()) null else selectionList.joinToString(" AND ")
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC LIMIT $limit OFFSET $offset"

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs.toTypedArray(),
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
            val accountNameIndex = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
            val accountTypeIndex = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: ""
                val photoUri = it.getString(photoIndex)
                val accName = it.getString(accountNameIndex)
                val accType = it.getString(accountTypeIndex)

                contacts.add(
                    Contact(
                        id = id,
                        name = name,
                        number = number,
                        photoUri = photoUri,
                        accountName = accName,
                        accountType = accType
                    )
                )
            }
        }
        contacts
    }

    suspend fun getContactById(id: String): Contact? = withContext(Dispatchers.IO) {
        // Try searching by CONTACT_ID first (common for recents/dialpad results)
        var contact = fetchContactByColumn(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, id)
        
        // If not found, try by _ID (common for direct contact list results)
        if (contact == null) {
            contact = fetchContactByColumn(ContactsContract.CommonDataKinds.Phone._ID, id)
        }
        
        contact
    }

    private fun fetchContactByColumn(column: String, value: String): Contact? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE
        )
        
        val selection = "$column = ?"
        val selectionArgs = arrayOf(value)

        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID)
                return Contact(
                    id = cursor.getString(idIndex),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "Unknown",
                    number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: "",
                    photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)),
                    accountName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_NAME)),
                    accountType = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE))
                )
            }
        }
        return null
    }

    suspend fun getAccounts(): List<ContactAccount> = withContext(Dispatchers.IO) {
        val accounts = mutableListOf<ContactAccount>()
        val uri = ContactsContract.RawContacts.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE
        )
        
        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        val seen = mutableSetOf<String>()

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
            val typeIndex = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: continue
                val type = it.getString(typeIndex) ?: continue
                
                val key = "$name|$type"
                if (!seen.contains(key)) {
                    accounts.add(ContactAccount(name, type))
                    seen.add(key)
                }
            }
        }
        accounts
    }

    suspend fun getTotalContactsCount(
        query: String = "",
        accountName: String? = null,
        accountType: String? = null
    ): Int = withContext(Dispatchers.IO) {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone._ID)
        
        val selectionList = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        if (query.isNotEmpty()) {
            selectionList.add("${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?")
            selectionArgs.add("%$query%")
        }

        if (accountName != null && accountType != null) {
            selectionList.add("${ContactsContract.RawContacts.ACCOUNT_NAME} = ?")
            selectionArgs.add(accountName)
            selectionList.add("${ContactsContract.RawContacts.ACCOUNT_TYPE} = ?")
            selectionArgs.add(accountType)
        } else if (accountType != null) {
            selectionList.add("${ContactsContract.RawContacts.ACCOUNT_TYPE} LIKE ?")
            selectionArgs.add("%$accountType%")
        }

        val selection = if (selectionList.isEmpty()) null else selectionList.joinToString(" AND ")
        
        var count = 0
        try {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs.toTypedArray(),
                null
            )
            count = cursor?.count ?: 0
            cursor?.close()
        } catch (e: Exception) { e.printStackTrace() }
        count
    }

    suspend fun getContactEmails(contactId: String): List<String> = withContext(Dispatchers.IO) {
        val emails = mutableListOf<String>()
        val uri = ContactsContract.CommonDataKinds.Email.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
        
        // We need to find the raw contact IDs for this contact first to be accurate, 
        // but for now, filtering by CONTACT_ID is usually sufficient.
        val selection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)
        
        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                if (index != -1) {
                    while (cursor.moveToNext()) {
                        emails.add(cursor.getString(index))
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        emails
    }

    suspend fun getContactCallHistory(
        phoneNumber: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntry>()
        val uri = android.provider.CallLog.Calls.CONTENT_URI
        val projection = arrayOf(
            android.provider.CallLog.Calls._ID,
            android.provider.CallLog.Calls.CACHED_NAME,
            android.provider.CallLog.Calls.NUMBER,
            android.provider.CallLog.Calls.DATE,
            android.provider.CallLog.Calls.TYPE,
            android.provider.CallLog.Calls.DURATION
        )
        
        val selection = "${android.provider.CallLog.Calls.NUMBER} LIKE ?"
        // Simple search for the last 7+ digits for better matching
        val normalized = phoneNumber.replace(Regex("[^0-9]"), "")
        val searchPattern = if (normalized.length >= 7) "%${normalized.takeLast(7)}" else "%$normalized%"
        val selectionArgs = arrayOf(searchPattern)
        val sortOrder = "${android.provider.CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"

        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(android.provider.CallLog.Calls._ID)
                val nameIdx = cursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME)
                val numIdx = cursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER)
                val dateIdx = cursor.getColumnIndex(android.provider.CallLog.Calls.DATE)
                val typeIdx = cursor.getColumnIndex(android.provider.CallLog.Calls.TYPE)
                val durIdx = cursor.getColumnIndex(android.provider.CallLog.Calls.DURATION)

                while (cursor.moveToNext()) {
                    list.add(
                        CallLogEntry(
                            id = cursor.getString(idIdx),
                            name = cursor.getString(nameIdx) ?: "",
                            number = cursor.getString(numIdx) ?: "",
                            date = cursor.getLong(dateIdx),
                            type = cursor.getInt(typeIdx),
                            duration = cursor.getLong(durIdx)
                        )
                    )
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        list
    }

    suspend fun findContactByNumber(number: String): Contact? = withContext(Dispatchers.IO) {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE
        )
        
        // Remove non-digit characters for matching
        val normalizedNumber = number.replace(Regex("[^0-9]"), "")
        if (normalizedNumber.isEmpty()) return@withContext null
        
        // Check for contacts that match this number
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val accNameIdx = cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
            val accTypeIdx = cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)
            
            while (cursor.moveToNext()) {
                val dbNumber = cursor.getString(numIdx) ?: ""
                val dbNormalized = dbNumber.replace(Regex("[^0-9]"), "")
                
                if (dbNormalized.takeLast(7) == normalizedNumber.takeLast(7)) {
                    return@withContext Contact(
                        id = cursor.getString(idIdx),
                        name = cursor.getString(nameIdx) ?: "",
                        number = dbNumber,
                        accountName = cursor.getString(accNameIdx),
                        accountType = cursor.getString(accTypeIdx)
                    )
                }
            }
        }
        null
    }

    suspend fun findContactByName(name: String): Contact? = withContext(Dispatchers.IO) {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE
        )
        
        // Use case-insensitive search
        val selection = "UPPER(${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}) = ?"
        val selectionArgs = arrayOf(name.uppercase())

        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val accNameIdx = cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
                val accTypeIdx = cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)
                
                return@withContext Contact(
                    id = cursor.getString(idIdx),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: name,
                    number = cursor.getString(numIdx) ?: "",
                    accountName = cursor.getString(accNameIdx),
                    accountType = cursor.getString(accTypeIdx)
                )
            }
        }
        null
    }

    suspend fun saveContact(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String?,
        photoUri: android.net.Uri?,
        accountName: String?,
        accountType: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val operations = arrayListOf<android.content.ContentProviderOperation>()

            // Add RAW_CONTACT
            operations.add(
                android.content.ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                    .build()
            )

            // Add Name
            operations.add(
                android.content.ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                    .build()
            )

            // Add Phone Number
            operations.add(
                android.content.ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            // Add Email
            if (!email.isNullOrBlank()) {
                operations.add(
                    android.content.ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        .build()
                )
            }

            // Add Photo
            if (photoUri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(photoUri)
                    val photoBytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (photoBytes != null) {
                        operations.add(
                            android.content.ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                                .build()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
