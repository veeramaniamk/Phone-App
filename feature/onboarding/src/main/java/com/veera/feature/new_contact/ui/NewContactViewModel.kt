package com.veera.feature.new_contact.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veera.core.telephony.model.Contact
import com.veera.core.telephony.model.ContactAccount
import com.veera.core.telephony.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<ContactAccount>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _duplicateContactByNumber = MutableStateFlow<Contact?>(null)
    val duplicateContactByNumber = _duplicateContactByNumber.asStateFlow()

    private val _duplicateContactByName = MutableStateFlow<Contact?>(null)
    val duplicateContactByName = _duplicateContactByName.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val allAccounts = contactRepository.getAccounts()
            _accounts.value = allAccounts
        }
    }

    fun checkDuplicateNumber(number: String) {
        viewModelScope.launch {
            if (number.length >= 7) {
                _duplicateContactByNumber.value = contactRepository.findContactByNumber(number)
            } else {
                _duplicateContactByNumber.value = null
            }
        }
    }

    fun checkDuplicateName(firstName: String, lastName: String) {
        viewModelScope.launch {
            val fullName = if (lastName.isNotBlank()) "$firstName $lastName" else firstName
            if (fullName.isNotBlank()) {
                _duplicateContactByName.value = contactRepository.findContactByName(fullName)
            } else {
                _duplicateContactByName.value = null
            }
        }
    }

    fun saveContact(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String?,
        photoUri: android.net.Uri?,
        account: ContactAccount?,
        onSuccess: () -> Unit
    ) {
        if (_isSaving.value) return
        
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val success = contactRepository.saveContact(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    photoUri = photoUri,
                    accountName = account?.name,
                    accountType = account?.type
                )
                if (success) {
                    onSuccess()
                }
            } finally {
                _isSaving.value = false
            }
        }
    }
}
