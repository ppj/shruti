package com.hindustani.pitchdetector.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hindustani.pitchdetector.music.SaParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepository(context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val SA_NOTE = stringPreferencesKey("sa_note")
        val TOLERANCE_CENTS = doublePreferencesKey("tolerance_cents")
        val USE_22_SHRUTI = booleanPreferencesKey("use_22_shruti")
        val IS_TANPURA_ENABLED = booleanPreferencesKey("is_tanpura_enabled")
        val TANPURA_STRING1 = stringPreferencesKey("tanpura_string1")
        val TANPURA_VOLUME = floatPreferencesKey("tanpura_volume")
    }

    val userSettings: Flow<UserSettings> =
        dataStore.data.map {
                preferences ->
            val saNote = preferences[PreferencesKeys.SA_NOTE] ?: UserSettings.DEFAULT_SA_NOTE
            val toleranceCents = preferences[PreferencesKeys.TOLERANCE_CENTS] ?: UserSettings.DEFAULT_TOLERANCE_CENTS
            val use22Shruti = preferences[PreferencesKeys.USE_22_SHRUTI] ?: UserSettings.DEFAULT_USE_22_SHRUTI
            val isTanpuraEnabled = preferences[PreferencesKeys.IS_TANPURA_ENABLED] ?: UserSettings.DEFAULT_TANPURA_ENABLED
            val tanpuraString1 = preferences[PreferencesKeys.TANPURA_STRING1] ?: UserSettings.DEFAULT_TANPURA_STRING1
            val tanpuraVolume = preferences[PreferencesKeys.TANPURA_VOLUME] ?: UserSettings.DEFAULT_TANPURA_VOLUME
            // Calculate saFrequency from saNote
            val saFrequency = SaParser.parseToFrequency(saNote) ?: UserSettings.DEFAULT_SA_FREQUENCY
            UserSettings(
                saNote = saNote,
                saFrequency = saFrequency,
                toleranceCents = toleranceCents,
                use22Shruti = use22Shruti,
                isTanpuraEnabled = isTanpuraEnabled,
                tanpuraString1 = tanpuraString1,
                tanpuraVolume = tanpuraVolume,
            )
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

    suspend fun updateTanpuraEnabled(enabled: Boolean) {
        dataStore.edit {
                preferences ->
            preferences[PreferencesKeys.IS_TANPURA_ENABLED] = enabled
        }
    }

    suspend fun updateTanpuraString1(string1: String) {
        dataStore.edit {
                preferences ->
            preferences[PreferencesKeys.TANPURA_STRING1] = string1
        }
    }

    suspend fun updateTanpuraVolume(volume: Float) {
        dataStore.edit {
                preferences ->
            preferences[PreferencesKeys.TANPURA_VOLUME] = volume
        }
    }
}
