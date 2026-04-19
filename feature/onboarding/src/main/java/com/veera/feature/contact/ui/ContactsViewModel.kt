package com.veera.feature.contact.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.veera.core.telephony.model.Contact
import com.veera.core.telephony.model.ContactAccount
import com.veera.core.telephony.model.FilterType
import com.veera.core.telephony.repository.CallLogEntry
import com.veera.core.telephony.repository.ContactRepository
import com.veera.core.telephony.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.paging.PagingSource
import androidx.paging.PagingState

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType = _filterType.asStateFlow()

    private val _selectedAccount = MutableStateFlow<ContactAccount?>(null)
    val selectedAccount = _selectedAccount.asStateFlow()

    private val _accounts = MutableStateFlow<List<ContactAccount>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _totalContactsCount = MutableStateFlow(0)
    val totalContactsCount = _totalContactsCount.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.lastFilterType.collect { type ->
                _filterType.value = type
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.lastAccount.collect { accountPair ->
                accountPair?.let { (name, type) ->
                    _selectedAccount.value = ContactAccount(name ?: "", type ?: "")
                }
            }
        }
        loadAccounts()
        observeCount()
    }

    private fun observeCount() {
        combine(_searchQuery, _filterType, _selectedAccount) { query, filter, account ->
            Triple(query, filter, account)
        }.onEach { (query, filter, account) ->
            val accName = if (filter == FilterType.EMAIL) account?.name else null
            val accType = when (filter) {
                FilterType.SIM -> "sim"
                FilterType.EMAIL -> account?.type
                else -> null
            }
            _totalContactsCount.value = contactRepository.getTotalContactsCount(query, accName, accType)
        }.launchIn(viewModelScope)
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _accounts.value = contactRepository.getAccounts()
        }
    }

    val contacts: Flow<PagingData<Contact>> = combine(
        _searchQuery,
        _filterType,
        _selectedAccount
    ) { query, filter, account ->
        Triple(query, filter, account)
    }.flatMapLatest { (query, filter, account) ->
        val accName = if (filter == FilterType.EMAIL) account?.name else null
        val accType = when (filter) {
            FilterType.SIM -> "sim"
            FilterType.EMAIL -> account?.type
            else -> null
        }

        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                ContactsPagingSource(contactRepository, query, accName, accType)
            }
        ).flow.cachedIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterTypeChange(type: FilterType) {
        _filterType.value = type
        if (type != FilterType.EMAIL) {
            _selectedAccount.value = null
        }
        savePrefs()
    }

    fun onAccountSelected(account: ContactAccount) {
        _selectedAccount.value = account
        savePrefs()
    }

    private fun savePrefs() {
        viewModelScope.launch {
            userPreferencesRepository.saveFilter(
                _filterType.value,
                _selectedAccount.value?.name,
                _selectedAccount.value?.type
            )
        }
    }

    fun getEmails(contactId: String): Flow<List<String>> = flow {
        emit(contactRepository.getContactEmails(contactId))
    }

    fun getCallHistory(phoneNumber: String): Flow<List<CallLogEntry>> = flow {
        emit(contactRepository.getContactCallHistory(phoneNumber, limit = 10))
    }

    fun getCallHistoryPaged(phoneNumber: String): Flow<PagingData<CallLogEntry>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { ContactHistoryPagingSource(contactRepository, phoneNumber) }
        ).flow.cachedIn(viewModelScope)
    }
}

class ContactHistoryPagingSource(
    private val repository: ContactRepository,
    private val phoneNumber: String
) : PagingSource<Int, CallLogEntry>() {
    override fun getRefreshKey(state: PagingState<Int, CallLogEntry>): Int? = state.anchorPosition

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CallLogEntry> {
        val page = params.key ?: 0
        return try {
            val history = repository.getContactCallHistory(phoneNumber, limit = params.loadSize, offset = page * params.loadSize)
            LoadResult.Page(
                data = history,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (history.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
