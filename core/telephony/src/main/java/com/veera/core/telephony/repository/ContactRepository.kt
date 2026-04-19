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
}
