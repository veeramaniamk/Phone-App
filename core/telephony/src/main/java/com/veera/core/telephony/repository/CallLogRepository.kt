package com.veera.core.telephony.repository

import android.content.Context
import android.provider.CallLog
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
    val duration: Long
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
            CallLog.Calls.DURATION
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
                        val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                        val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                        val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                        val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                        val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))

                        list.add(CallLogEntry(id, name, number, date, type, duration))
                        count++
                    } while (it.moveToNext() && count < pageSize)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        list
    }
}
