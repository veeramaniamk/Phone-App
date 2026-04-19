package com.veera.core.telephony.repository

import android.content.Context
import android.provider.CallLog
import android.util.Log
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

    private  val TAG = "CallLogsDebug"

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
                Log.d(TAG, "Total rows in cursor = ${it.count}")

                val startIndex = page * pageSize
                Log.d(TAG, "Page: $page, PageSize: $pageSize, StartIndex: $startIndex")

                if (it.moveToPosition(startIndex)) {
                    var count = 0
                    do {
                        val id = it.getString(it.getColumnIndexOrThrow(CallLog.Calls._ID))
                        val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))
                        val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                        val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                        val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                        val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))

                        // Convert type to readable string
                        val typeStr = when (type) {
                            CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                            CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                            CallLog.Calls.MISSED_TYPE -> "MISSED"
                            CallLog.Calls.REJECTED_TYPE -> "REJECTED"
                            CallLog.Calls.BLOCKED_TYPE -> "BLOCKED"
                            else -> "UNKNOWN"
                        }

                        // Convert date to readable format
                        val formattedDate = java.text.SimpleDateFormat(
                            "dd-MM-yyyy HH:mm:ss",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(date))

                        // 🔥 LOG EVERYTHING
                        Log.d(TAG, """
                        ------------------------------
                        ID: $id
                        Name: ${name ?: "NULL"}
                        Number: $number
                        Date: $formattedDate
                        Type: $typeStr
                        Duration: $duration sec
                        ------------------------------
                    """.trimIndent())

                        list.add(CallLogEntry(id, name, number, date, type, duration))
                        count++
                    } while (it.moveToNext() && count < pageSize)
                } else {
                    Log.d(TAG, "moveToPosition failed. No data for this page.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error fetching call logs", e)
        }

        list
    }
}
