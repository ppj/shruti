package com.hindustani.pitchdetector.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.trainingDataStore: DataStore<Preferences> by preferencesDataStore(name = "training_progress")

/**
 * Repository for persisting and retrieving training progress data.
 * Uses DataStore to save progress across app sessions.
 */
class TrainingProgressRepository(context: Context) {
    private val dataStore = context.trainingDataStore

    private object PreferencesKeys {
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val COMPLETED_EXERCISES = intPreferencesKey("completed_exercises")
        val TOTAL_SCORE = intPreferencesKey("total_score")
        val UNLOCKED_SWARAS = stringSetPreferencesKey("unlocked_swaras")
    }

    val trainingProgress: Flow<TrainingProgress> =
        dataStore.data.map { preferences ->
            val currentLevel =
                preferences[PreferencesKeys.CURRENT_LEVEL]
                    ?: TrainingProgress.DEFAULT_CURRENT_LEVEL
            val completedExercises =
                preferences[PreferencesKeys.COMPLETED_EXERCISES]
                    ?: TrainingProgress.DEFAULT_COMPLETED_EXERCISES
            val totalScore =
                preferences[PreferencesKeys.TOTAL_SCORE]
                    ?: TrainingProgress.DEFAULT_TOTAL_SCORE
            val unlockedSwaras =
                preferences[PreferencesKeys.UNLOCKED_SWARAS]
                    ?: TrainingProgress.DEFAULT_UNLOCKED_SWARAS

            TrainingProgress(
                currentLevel = currentLevel,
                completedExercises = completedExercises,
                totalScore = totalScore,
                unlockedSwaras = unlockedSwaras,
            )
        }

    suspend fun updateCurrentLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LEVEL] = level
        }
    }

    suspend fun updateCompletedExercises(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPLETED_EXERCISES] = count
        }
    }

    suspend fun updateTotalScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_SCORE] = score
        }
    }

    suspend fun updateUnlockedSwaras(swaras: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.UNLOCKED_SWARAS] = swaras
        }
    }

    suspend fun incrementExercises() {
        dataStore.edit { preferences ->
            val current =
                preferences[PreferencesKeys.COMPLETED_EXERCISES]
                    ?: TrainingProgress.DEFAULT_COMPLETED_EXERCISES
            preferences[PreferencesKeys.COMPLETED_EXERCISES] = current + 1
        }
    }

    suspend fun addToScore(points: Int) {
        dataStore.edit { preferences ->
            val current =
                preferences[PreferencesKeys.TOTAL_SCORE]
                    ?: TrainingProgress.DEFAULT_TOTAL_SCORE
            preferences[PreferencesKeys.TOTAL_SCORE] = current + points
        }
    }
}
