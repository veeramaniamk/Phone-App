package com.veera.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import com.veera.core.util.DialerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DefaultDialerViewModel @Inject constructor(
    private val dialerManager: DialerManager
) : ViewModel() {

    private val _isDefaultDialer = MutableStateFlow(dialerManager.isDefaultDialer())
    val isDefaultDialer: StateFlow<Boolean> = _isDefaultDialer

    fun checkStatus() {
        _isDefaultDialer.value = dialerManager.isDefaultDialer()
    }

    fun getRequestIntent() = dialerManager.createRequestDefaultDialerIntent()
}
