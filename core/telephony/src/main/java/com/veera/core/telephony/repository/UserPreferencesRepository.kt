package com.veera.core.telephony.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.veera.core.telephony.model.FilterType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val LAST_FILTER_TYPE = stringPreferencesKey("last_filter_type")
        val LAST_ACCOUNT_NAME = stringPreferencesKey("last_account_name")
        val LAST_ACCOUNT_TYPE = stringPreferencesKey("last_account_type")
    }

    val lastFilterType: Flow<FilterType> = context.dataStore.data.map { preferences ->
        val type = preferences[PreferencesKeys.LAST_FILTER_TYPE] ?: FilterType.ALL.name
        FilterType.valueOf(type)
    }

    val lastAccount: Flow<Pair<String?, String?>?> = context.dataStore.data.map { preferences ->
        val name = preferences[PreferencesKeys.LAST_ACCOUNT_NAME]
        val type = preferences[PreferencesKeys.LAST_ACCOUNT_TYPE]
        if (name != null && type != null) Pair(name, type) else null
    }

    suspend fun saveFilter(filterType: FilterType, accountName: String? = null, accountType: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_FILTER_TYPE] = filterType.name
            if (accountName != null) {
                preferences[PreferencesKeys.LAST_ACCOUNT_NAME] = accountName
            } else {
                preferences.remove(PreferencesKeys.LAST_ACCOUNT_NAME)
            }
            if (accountType != null) {
                preferences[PreferencesKeys.LAST_ACCOUNT_TYPE] = accountType
            } else {
                preferences.remove(PreferencesKeys.LAST_ACCOUNT_TYPE)
            }
        }
    }
}
