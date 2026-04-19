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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    // Main list state
    val allRecents = mutableStateListOf<RecentCall>()
    val isLoading = mutableStateOf(false)
    val isInitialLoading = mutableStateOf(true)
    val isEndReached = mutableStateOf(false)
    private var page = 0

    // Metrics state
    val totalItemCount = mutableStateOf(0)
    val currentPage = mutableStateOf(0)
    val pageSize = 20

    // Search state
    val searchQuery = mutableStateOf("")
    val isSearching = mutableStateOf(false)
    val searchResults = mutableStateListOf<RecentCall>()
    val isSearchLoading = mutableStateOf(false)
    val isSearchEndReached = mutableStateOf(false)
    private var searchPage = 0
    private var searchJob: Job? = null

    init {
        updateMetrics()
    }

    private fun updateMetrics() {
        viewModelScope.launch {
            totalItemCount.value = callLogRepository.getTotalCallLogCount()
        }
    }

    fun loadNextPage() {
        if (isLoading.value || isEndReached.value) return

        isLoading.value = true
        viewModelScope.launch {
            val nextPageData = callLogRepository.getCallLogs(page, pageSize)
            if (nextPageData.isEmpty()) {
                isEndReached.value = true
            } else {
                val mappedData = nextPageData.map { it.toRecentCall() }
                allRecents.addAll(mappedData)
                page++
                currentPage.value = page
            }
            isLoading.value = false
            isInitialLoading.value = false
            updateMetrics()
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        searchJob?.cancel()
        
        if (query.isBlank()) {
            isSearching.value = false
            searchResults.clear()
            searchPage = 0
            isSearchEndReached.value = false
            return
        }

        isSearching.value = true
        isSearchLoading.value = true
        searchResults.clear()
        searchPage = 0
        isSearchEndReached.value = false
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            performSearch()
        }
    }

    fun loadNextSearchPage() {
        if (isSearchLoading.value || isSearchEndReached.value || searchQuery.value.isBlank()) return
        
        isSearchLoading.value = true
        viewModelScope.launch {
            performSearch()
        }
    }

    private suspend fun performSearch() {
        val results = callLogRepository.searchCallLogs(searchQuery.value, searchPage, pageSize)
        if (results.isEmpty()) {
            isSearchEndReached.value = true
        } else {
            val mappedData = results.map { it.toRecentCall() }
            searchResults.addAll(mappedData)
            searchPage++
        }
        isSearchLoading.value = false
    }

    private fun CallLogEntry.toRecentCall(): RecentCall {
        return RecentCall(
            id = id,
            name = name ?: number,
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
