package com.veera.feature.home

import android.provider.CallLog
import android.text.format.DateUtils
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veera.core.telephony.repository.CallLogEntry
import com.veera.core.telephony.repository.CallLogRepository
import com.veera.core.telephony.repository.CallRepository
import com.veera.feature.home.ui.CallType
import com.veera.feature.home.ui.RecentCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val callRepository: CallRepository
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
        observeCallFinished()
    }

    private fun observeCallFinished() {
        callRepository.callFinishedEvent
            .onEach {
                delay(1000) // Small delay to let system write to call log
                refreshLogs()
            }
            .launchIn(viewModelScope)
    }

    private fun refreshLogs() {
        viewModelScope.launch {
            page = 0
            isEndReached.value = false
            val firstPage = callLogRepository.getCallLogs(0, pageSize)
            allRecents.clear()
            val mappedData = firstPage.map { it.toRecentCall() }
            allRecents.addAll(mergeCalls(mappedData))
            page = 1
            updateMetrics()
        }
    }

    private fun mergeCalls(calls: List<RecentCall>): List<RecentCall> {
        if (calls.isEmpty()) return emptyList()
        val merged = mutableListOf<RecentCall>()
        var current = calls.first()

        for (i in 1 until calls.size) {
            val next = calls[i]
            if (current.number == next.number && current.type == next.type && isSameDay(current.rawDate, next.rawDate)) {
                current = current.copy(count = current.count + next.count)
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }

    private fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
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
                val mergedMapped = mergeCalls(mappedData)
                if (allRecents.isNotEmpty() && mergedMapped.isNotEmpty()) {
                    val last = allRecents.last()
                    val firstNew = mergedMapped.first()
                    if (last.number == firstNew.number && last.type == firstNew.type && isSameDay(last.rawDate, firstNew.rawDate)) {
                        allRecents[allRecents.size - 1] = last.copy(count = last.count + firstNew.count)
                        allRecents.addAll(mergedMapped.drop(1))
                    } else {
                        allRecents.addAll(mergedMapped)
                    }
                } else {
                    allRecents.addAll(mergedMapped)
                }
                page++
            }
            isLoading.value = false
            isInitialLoading.value = false
            updateMetrics()
        }
    }

    fun updateCurrentPage(newPage: Int) {
        if (currentPage.value != newPage) {
            currentPage.value = newPage
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
            val mergedMapped = mergeCalls(mappedData)
            if (searchResults.isNotEmpty() && mergedMapped.isNotEmpty()) {
                val last = searchResults.last()
                val firstNew = mergedMapped.first()
                if (last.number == firstNew.number && last.type == firstNew.type && isSameDay(last.rawDate, firstNew.rawDate)) {
                    searchResults[searchResults.size - 1] = last.copy(count = last.count + firstNew.count)
                    searchResults.addAll(mergedMapped.drop(1))
                } else {
                    searchResults.addAll(mergedMapped)
                }
            } else {
                searchResults.addAll(mergedMapped)
            }
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
            isMissed = type == CallLog.Calls.MISSED_TYPE,
            photoUri = photoUri,
            contactId = contactId,
            count = 1,
            rawDate = date
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
