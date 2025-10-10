package com.hindustani.pitchdetector.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UserSettingsRepositoryTest {

    private lateinit var context: Context
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var testScope: CoroutineScope
    private lateinit var repository: UserSettingsRepository

    @Before
    fun setup() {
        // Use Robolectric context
        context = ApplicationProvider.getApplicationContext()

        // Delete existing DataStore files to ensure clean state
        context.preferencesDataStoreFile("user_settings").delete()
        context.preferencesDataStoreFile("test_user_settings").delete()

        // Create a test DataStore with test dispatcher
        val testDispatcher = StandardTestDispatcher()
        testScope = CoroutineScope(testDispatcher + Job())

        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                context.preferencesDataStoreFile("test_user_settings")
            }
        )

        // Create repository with test context
        repository = UserSettingsRepository(context)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        // Clean up DataStore files after test
        context.preferencesDataStoreFile("user_settings").delete()
        context.preferencesDataStoreFile("test_user_settings").delete()
    }

    @Test
    fun `userSettings flow emits current settings`() = runTest {
        // Set known values first
        repository.updateSaNote("D4")
        repository.updateTolerance(20.0)
        repository.updateTuningSystem(true)

        val settings = repository.userSettings.first()

        assertThat(settings.saNote).isEqualTo("D4")
        assertThat(settings.toleranceCents).isEqualTo(20.0)
        assertThat(settings.use22Shruti).isTrue()
    }

    @Test
    fun `updateSaNote persists value correctly`() = runTest {
        repository.updateSaNote("D4")

        val settings = repository.userSettings.first()
        assertThat(settings.saNote).isEqualTo("D4")
    }

    @Test
    fun `saFrequency is calculated correctly from saNote`() = runTest {
        // Test G#2
        repository.updateSaNote("G#2")
        var settings = repository.userSettings.first()
        assertThat(settings.saNote).isEqualTo("G#2")
        assertThat(settings.saFrequency).isWithin(0.01).of(103.83)

        // Test C3
        repository.updateSaNote("C3")
        settings = repository.userSettings.first()
        assertThat(settings.saNote).isEqualTo("C3")
        assertThat(settings.saFrequency).isWithin(0.01).of(130.81)

        // Test C#3
        repository.updateSaNote("C#3")
        settings = repository.userSettings.first()
        assertThat(settings.saNote).isEqualTo("C#3")
        assertThat(settings.saFrequency).isWithin(0.01).of(138.59)

        // Test A3
        repository.updateSaNote("A3")
        settings = repository.userSettings.first()
        assertThat(settings.saNote).isEqualTo("A3")
        assertThat(settings.saFrequency).isWithin(0.01).of(220.0)
    }

    @Test
    fun `updateTolerance persists value correctly`() = runTest {
        repository.updateTolerance(20.0)

        val settings = repository.userSettings.first()
        assertThat(settings.toleranceCents).isEqualTo(20.0)
    }

    @Test
    fun `updateTuningSystem persists value correctly`() = runTest {
        repository.updateTuningSystem(true)

        val settings = repository.userSettings.first()
        assertThat(settings.use22Shruti).isTrue()
    }

    @Test
    fun `updateTanpuraEnabled persists value correctly`() = runTest {
        repository.updateTanpuraEnabled(true)

        val settings = repository.userSettings.first()
        assertThat(settings.isTanpuraEnabled).isTrue()
    }

    @Test
    fun `updateTanpuraString1 persists value correctly`() = runTest {
        repository.updateTanpuraString1("M")

        val settings = repository.userSettings.first()
        assertThat(settings.tanpuraString1).isEqualTo("M")
    }

    @Test
    fun `updateTanpuraVolume persists value correctly`() = runTest {
        repository.updateTanpuraVolume(0.8f)

        val settings = repository.userSettings.first()
        assertThat(settings.tanpuraVolume).isEqualTo(0.8f)
    }

    @Test
    fun `multiple updates persist correctly`() = runTest {
        // Update multiple settings
        repository.updateSaNote("E4")
        repository.updateTolerance(10.0)
        repository.updateTuningSystem(true)
        repository.updateTanpuraEnabled(true)
        repository.updateTanpuraString1("S")
        repository.updateTanpuraVolume(0.7f)

        // Verify all persisted
        val settings = repository.userSettings.first()
        assertThat(settings.saNote).isEqualTo("E4")
        assertThat(settings.toleranceCents).isEqualTo(10.0)
        assertThat(settings.use22Shruti).isTrue()
        assertThat(settings.isTanpuraEnabled).isTrue()
        assertThat(settings.tanpuraString1).isEqualTo("S")
        assertThat(settings.tanpuraVolume).isEqualTo(0.7f)
    }

    @Test
    fun `sequential updates to same setting persist latest value`() = runTest {
        repository.updateSaNote("C3")
        repository.updateSaNote("D3")
        repository.updateSaNote("E3")

        val settings = repository.userSettings.first()
        assertThat(settings.saNote).isEqualTo("E3")
    }

    @Test
    fun `updateTolerance handles edge case values`() = runTest {
        // Test minimum reasonable tolerance
        repository.updateTolerance(0.0)
        var settings = repository.userSettings.first()
        assertThat(settings.toleranceCents).isEqualTo(0.0)

        // Test maximum reasonable tolerance
        repository.updateTolerance(50.0)
        settings = repository.userSettings.first()
        assertThat(settings.toleranceCents).isEqualTo(50.0)
    }

    @Test
    fun `updateTanpuraVolume handles edge case values`() = runTest {
        // Test minimum volume
        repository.updateTanpuraVolume(0.0f)
        var settings = repository.userSettings.first()
        assertThat(settings.tanpuraVolume).isEqualTo(0.0f)

        // Test maximum volume
        repository.updateTanpuraVolume(1.0f)
        settings = repository.userSettings.first()
        assertThat(settings.tanpuraVolume).isEqualTo(1.0f)
    }

    @Test
    fun `userSettings flow emits updated values after changes`() = runTest {
        // Set initial known value
        repository.updateSaNote("G3")
        val initialSettings = repository.userSettings.first()
        assertThat(initialSettings.saNote).isEqualTo("G3")

        // Update to new value
        repository.updateSaNote("F#3")

        // Flow should emit new value
        val updatedSettings = repository.userSettings.first()
        assertThat(updatedSettings.saNote).isEqualTo("F#3")
    }

    @Test
    fun `updating one setting does not affect other settings`() = runTest {
        // Set all settings to non-default values
        repository.updateSaNote("A3")
        repository.updateTolerance(25.0)
        repository.updateTuningSystem(true)

        // Now update just one setting
        repository.updateSaNote("B3")

        // Verify only saNote changed
        val settings = repository.userSettings.first()
        assertThat(settings.saNote).isEqualTo("B3")
        assertThat(settings.toleranceCents).isEqualTo(25.0)
        assertThat(settings.use22Shruti).isTrue()
    }

    @Test
    fun `all tanpura settings can be updated independently`() = runTest {
        // Set all to known values first
        repository.updateTanpuraEnabled(false)
        repository.updateTanpuraString1("P")
        repository.updateTanpuraVolume(0.5f)

        // Update tanpura enabled
        repository.updateTanpuraEnabled(true)
        var settings = repository.userSettings.first()
        assertThat(settings.isTanpuraEnabled).isTrue()
        val originalVolume = settings.tanpuraVolume

        // Update tanpura string
        repository.updateTanpuraString1("N")
        settings = repository.userSettings.first()
        assertThat(settings.isTanpuraEnabled).isTrue() // Should remain true
        assertThat(settings.tanpuraString1).isEqualTo("N")
        assertThat(settings.tanpuraVolume).isEqualTo(originalVolume) // Should remain unchanged

        // Update tanpura volume
        repository.updateTanpuraVolume(0.9f)
        settings = repository.userSettings.first()
        assertThat(settings.isTanpuraEnabled).isTrue()
        assertThat(settings.tanpuraString1).isEqualTo("N")
        assertThat(settings.tanpuraVolume).isEqualTo(0.9f)
    }

    @Test
    fun `updateTanpuraString1 handles all valid swara options`() = runTest {
        val validSwaras = listOf("P", "m", "M", "S", "N")

        validSwaras.forEach { swara ->
            repository.updateTanpuraString1(swara)
            val settings = repository.userSettings.first()
            assertThat(settings.tanpuraString1).isEqualTo(swara)
        }
    }
}
