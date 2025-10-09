package com.hindustani.pitchdetector.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val SA_NOTE = stringPreferencesKey("sa_note")
        val TOLERANCE_CENTS = doublePreferencesKey("tolerance_cents")
        val USE_22_SHRUTI = booleanPreferencesKey("use_22_shruti")
        val DEFAULT_SA_NOTE = stringPreferencesKey("default_sa_note")
    }

    val userSettings: Flow<UserSettings> = dataStore.data.map {
        preferences ->
        val saNote = preferences[PreferencesKeys.SA_NOTE] ?: "C3"
        val toleranceCents = preferences[PreferencesKeys.TOLERANCE_CENTS] ?: 15.0
        val use22Shruti = preferences[PreferencesKeys.USE_22_SHRUTI] ?: false
        val defaultSaNote = preferences[PreferencesKeys.DEFAULT_SA_NOTE] ?: "C3"
        UserSettings(
            saNote = saNote,
            toleranceCents = toleranceCents,
            use22Shruti = use22Shruti,
            defaultSaNote = defaultSaNote
        )
    }

    suspend fun updateDefaultSaNote(defaultSaNote: String) {
        dataStore.edit {
            preferences ->
            preferences[PreferencesKeys.DEFAULT_SA_NOTE] = defaultSaNote
        }
    }


    suspend fun updateSaNote(saNote: String) {
        dataStore.edit {
            preferences ->
            preferences[PreferencesKeys.SA_NOTE] = saNote
        }
    }

    suspend fun updateTolerance(tolerance: Double) {
        dataStore.edit {
            preferences ->
            preferences[PreferencesKeys.TOLERANCE_CENTS] = tolerance
        }
    }

    suspend fun updateTuningSystem(use22Shruti: Boolean) {
        dataStore.edit {
            preferences ->
            preferences[PreferencesKeys.USE_22_SHRUTI] = use22Shruti
        }
    }
}
