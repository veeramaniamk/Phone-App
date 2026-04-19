package com.veera.feature.home

import android.provider.CallLog
import android.text.format.DateUtils
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veera.core.telephony.repository.CallLogEntry
import com.veera.core.telephony.repository.CallLogRepository
import com.veera.feature.home.ui.CallType
import com.veera.feature.home.ui.RecentCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    val allRecents = mutableStateListOf<RecentCall>()
    val isLoading = mutableStateOf(false)
    val isInitialLoading = mutableStateOf(true)
    val isEndReached = mutableStateOf(false)
    private var page = 0

    fun loadNextPage() {
        if (isLoading.value || isEndReached.value) return

        isLoading.value = true
        viewModelScope.launch {
            val nextPageData = callLogRepository.getCallLogs(page, 20)
            if (nextPageData.isEmpty()) {
                isEndReached.value = true
            } else {
                val mappedData = nextPageData.map { it.toRecentCall() }
                allRecents.addAll(mappedData)
                page++
            }
            isLoading.value = false
            isInitialLoading.value = false
        }
    }

    private fun CallLogEntry.toRecentCall(): RecentCall {
        val name = if(name.isNullOrEmpty() || name.isNullOrBlank()) number else name?:""
        return RecentCall(
            id = id,
            name = name,
            number = number,
            timestamp = formatTimestamp(date),
            type = mapCallType(type),
            isMissed = type == CallLog.Calls.MISSED_TYPE
        )
    }

    private fun formatTimestamp(date: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            date,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    private fun mapCallType(type: Int): CallType {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
            CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
            CallLog.Calls.MISSED_TYPE -> CallType.MISSED
            else -> CallType.INCOMING
        }
    }
}
