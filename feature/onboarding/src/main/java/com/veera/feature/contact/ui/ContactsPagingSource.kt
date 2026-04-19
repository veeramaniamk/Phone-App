package com.veera.feature.contact.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.veera.core.telephony.model.Contact
import com.veera.core.telephony.repository.ContactRepository

class ContactsPagingSource(
    private val repository: ContactRepository,
    private val query: String,
    private val accountName: String?,
    private val accountType: String?
) : PagingSource<Int, Contact>() {

    override fun getRefreshKey(state: PagingState<Int, Contact>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Contact> {
        val page = params.key ?: 0
        return try {
            val contacts = repository.getContacts(
                query = query,
                accountName = accountName,
                accountType = accountType,
                limit = params.loadSize,
                offset = page * params.loadSize
            )
            LoadResult.Page(
                data = contacts,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (contacts.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
