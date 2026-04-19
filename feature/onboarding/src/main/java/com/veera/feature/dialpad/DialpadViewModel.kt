package com.veera.feature.dialpad

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veera.core.telephony.repository.CallLogRepository
import com.veera.core.telephony.repository.DialpadSuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DialpadViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    val phoneNumber = mutableStateOf("")
    val suggestions = mutableStateListOf<DialpadSuggestion>()
    val isLoading = mutableStateOf(false)
    val isEndReached = mutableStateOf(false)
    private var page = 0
    private var searchJob: Job? = null
    private val pageSize = 20

    fun onNumberChanged(newNumber: String) {
        phoneNumber.value = newNumber
        suggestions.clear()
        page = 0
        isEndReached.value = false
        
        searchJob?.cancel()
        if (newNumber.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                delay(200)
                loadSuggestions()
            }
        }
    }

    fun loadMoreSuggestions() {
        if (isEndReached.value || isLoading.value || phoneNumber.value.isEmpty()) return
        viewModelScope.launch {
            loadSuggestions()
        }
    }

    private suspend fun loadSuggestions() {
        isLoading.value = true
        val result = callLogRepository.getDialpadSuggestions(phoneNumber.value, page, pageSize)
        if (result.isEmpty()) {
            isEndReached.value = true
        } else {
            suggestions.addAll(result)
            page++
        }
        isLoading.value = false
    }
}
