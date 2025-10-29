package com.hindustani.pitchdetector.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.hindustani.pitchdetector.data.PitchState
import com.hindustani.pitchdetector.music.HindustaniNoteConverter
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for TrainingViewModel
 * Tests training session logic including initialization, state management, and note progression
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TrainingViewModelTest {
    private lateinit var pitchViewModel: PitchViewModel
    private lateinit var trainingViewModel: TrainingViewModel
    private lateinit var context: Context
    private val testDispatcher = UnconfinedTestDispatcher()

    companion object {
        // Time to skip countdown (3 seconds) + small buffer
        private const val COUNTDOWN_AND_BUFFER_MS = 3100L

        // Time to hold note (2 seconds) + small buffer
        private const val HOLD_DURATION_AND_BUFFER_MS = 2100L

        // Max score for Level 1 (7 notes with perfect combo)
        private const val LEVEL_1_MAX_SCORE = 2100

        // Invalid level number for testing error cases
        private const val INVALID_LEVEL_NUMBER = 99

        // C3 frequency in Hz
        private const val C3_FREQUENCY = 130.81
    }

    private val mockPitchState =
        MutableStateFlow(
            PitchState(
                saNote = "C3",
                currentNote = null,
                confidence = 0.0f,
                toleranceCents = 15.0,
            ),
        )
    private val mockIsTanpuraPlaying = MutableStateFlow(false)
    private val mockIsRecording = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = ApplicationProvider.getApplicationContext()
        pitchViewModel = mockk(relaxed = true)
        every { pitchViewModel.pitchState } returns mockPitchState
        every { pitchViewModel.isTanpuraPlaying } returns mockIsTanpuraPlaying
        every { pitchViewModel.isRecording } returns mockIsRecording
        every { pitchViewModel.getSaFrequency() } returns C3_FREQUENCY
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization_level1_startsWithCorrectLevel`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.state.value.level).isEqualTo(1)
            assertThat(trainingViewModel.state.value.currentSwar).isEqualTo("S")
        }

    @Test
    fun `initialization_level2_startsWithCorrectLevel`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 2, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.state.value.level).isEqualTo(2)
            assertThat(trainingViewModel.state.value.currentSwar).isNotNull()
        }

    @Test
    fun `initialization_level3_startsWithCorrectLevel`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 3, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.state.value.level).isEqualTo(3)
            assertThat(trainingViewModel.state.value.currentSwar).isEqualTo("S")
        }

    @Test
    fun `initialization_level4_startsWithCorrectLevel`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 4, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.state.value.level).isEqualTo(4)
            assertThat(trainingViewModel.state.value.currentSwar).isNotNull()
        }

    @Test
    fun `initialization_setsTanpuraString1ToPa`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            verify { pitchViewModel.updateTanpuraString1("P") }
        }

    @Test
    fun `initialization_startsTanpuraIfNotPlaying`() =
        runTest {
            mockIsTanpuraPlaying.value = false

            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            verify { pitchViewModel.toggleTanpura() }
        }

    @Test
    fun `initialization_doesNotStartTanpuraIfAlreadyPlaying`() =
        runTest {
            mockIsTanpuraPlaying.value = true

            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            verify(exactly = 0) { pitchViewModel.toggleTanpura() }
        }

    @Test
    fun `initialization_startsCountdownAt3`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.state.value.countdown).isEqualTo(3)
        }

    @Test
    fun `observePitch_correctNoteDetected_setsIsHoldingCorrectly`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val correctNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )
            mockPitchState.value = mockPitchState.value.copy(currentNote = correctNote)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.isHoldingCorrectly).isTrue()
        }

    @Test
    fun `observePitch_incorrectNoteDetected_setsIsHoldingCorrectlyFalse`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val incorrectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "R",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )
            mockPitchState.value = mockPitchState.value.copy(currentNote = incorrectNote)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.isHoldingCorrectly).isFalse()
        }

    @Test
    fun `observePitch_noteNotPerfect_setsIsHoldingCorrectlyFalse`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val imperfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 20.0,
                    isPerfect = false,
                    isFlat = false,
                    isSharp = true,
                )
            mockPitchState.value = mockPitchState.value.copy(currentNote = imperfectNote)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.isHoldingCorrectly).isFalse()
        }

    @Test
    fun `observePitch_correctSwarButFlat_setsIsFlatTrue`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val flatNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = -20.0,
                    isPerfect = false,
                    isFlat = true,
                    isSharp = false,
                )
            mockPitchState.value = mockPitchState.value.copy(currentNote = flatNote)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.isFlat).isTrue()
            assertThat(trainingViewModel.state.value.isSharp).isFalse()
            assertThat(trainingViewModel.state.value.detectedSwar).isEqualTo("S")
        }

    @Test
    fun `observePitch_correctSwarButSharp_setsIsSharpTrue`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val sharpNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 20.0,
                    isPerfect = false,
                    isFlat = false,
                    isSharp = true,
                )
            mockPitchState.value = mockPitchState.value.copy(currentNote = sharpNote)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.isSharp).isTrue()
            assertThat(trainingViewModel.state.value.isFlat).isFalse()
            assertThat(trainingViewModel.state.value.detectedSwar).isEqualTo("S")
        }

    @Test
    fun `observePitch_wrongSwarEvenIfFlat_doesNotSetIsFlat`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val wrongNoteFlat =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "R",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = -20.0,
                    isPerfect = false,
                    isFlat = true,
                    isSharp = false,
                )
            mockPitchState.value = mockPitchState.value.copy(currentNote = wrongNoteFlat)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.isFlat).isFalse()
            assertThat(trainingViewModel.state.value.isSharp).isFalse()
            assertThat(trainingViewModel.state.value.detectedSwar).isEqualTo("R")
        }

    @Test
    fun `observePitch_perfectNote_setsBothFlatAndSharpFalse`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val perfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )
            mockPitchState.value = mockPitchState.value.copy(currentNote = perfectNote)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.isFlat).isFalse()
            assertThat(trainingViewModel.state.value.isSharp).isFalse()
            assertThat(trainingViewModel.state.value.isHoldingCorrectly).isTrue()
        }

    @Test
    fun `observePitch_nullNote_clearsDetectedSwarAndFlatSharpFlags`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            mockPitchState.value = mockPitchState.value.copy(currentNote = null)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.detectedSwar).isNull()
            assertThat(trainingViewModel.state.value.isFlat).isFalse()
            assertThat(trainingViewModel.state.value.isSharp).isFalse()
        }

    @Test
    fun `resetSession_resetsCurrentNoteIndexTo0`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            trainingViewModel.resetSession()
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.currentNoteIndex).isEqualTo(0)
        }

    @Test
    fun `resetSession_resetsHoldProgressTo0`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            trainingViewModel.resetSession()
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.holdProgress).isEqualTo(0f)
        }

    @Test
    fun `toggleTanpura_ensuresString1IsSetToPa`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            clearMocks(pitchViewModel, answers = false)

            trainingViewModel.toggleTanpura()

            verify { pitchViewModel.updateTanpuraString1("P") }
            verify { pitchViewModel.toggleTanpura() }
        }

    @Test
    fun `state_initiallyNotComplete`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.state.value.isSessionComplete).isFalse()
        }

    @Test
    fun `state_initialHoldProgressIsZero`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.state.value.holdProgress).isEqualTo(0f)
        }

    @Test
    fun `state_exposesCorrectLevel`() =
        runTest {
            val viewModel1 = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            val viewModel2 = TrainingViewModel(level = 2, pitchViewModel = pitchViewModel, context = context)
            val viewModel3 = TrainingViewModel(level = 3, pitchViewModel = pitchViewModel, context = context)
            val viewModel4 = TrainingViewModel(level = 4, pitchViewModel = pitchViewModel, context = context)

            assertThat(viewModel1.state.value.level).isEqualTo(1)
            assertThat(viewModel2.state.value.level).isEqualTo(2)
            assertThat(viewModel3.state.value.level).isEqualTo(3)
            assertThat(viewModel4.state.value.level).isEqualTo(4)
        }

    @Test
    fun `saNote_flowIsExposed`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.saNote.value).isEqualTo("C3")
        }

    @Test
    fun `isTanpuraPlaying_flowIsExposed`() =
        runTest {
            mockIsTanpuraPlaying.value = true
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.isTanpuraPlaying.value).isTrue()
        }

    @Test
    fun `scoring_completingFirstPerfectNote_updatesScoreAndIncrementsCombo`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val perfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )
            mockPitchState.value = mockPitchState.value.copy(currentNote = perfectNote)
            testScheduler.advanceUntilIdle()
            testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)

            // Score: 100 base points + (50 bonus * 1 combo) = 150
            assertThat(trainingViewModel.state.value.currentScore).isEqualTo(150)
            assertThat(trainingViewModel.state.value.comboCount).isEqualTo(1)
        }

    @Test
    fun `scoring_initialState_hasZeroScore`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            assertThat(trainingViewModel.state.value.currentScore).isEqualTo(0)
            assertThat(trainingViewModel.state.value.comboCount).isEqualTo(0)
            assertThat(trainingViewModel.state.value.sessionBestScore).isEqualTo(0)
            assertThat(trainingViewModel.state.value.earnedStars).isEqualTo(0)
        }

    @Test
    fun `scoring_comboProgression_increasesWithConsecutivePerfectNotes`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val perfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )

            // First note (S) - combo should be 1, score = 100 + 50*1 = 150
            mockPitchState.value = mockPitchState.value.copy(currentNote = perfectNote)
            testScheduler.advanceUntilIdle()
            testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)
            assertThat(trainingViewModel.state.value.currentScore).isEqualTo(150)
            assertThat(trainingViewModel.state.value.comboCount).isEqualTo(1)

            // Second note (R) - combo should be 2, score = 150 + 100 + 50*2 = 350
            mockPitchState.value =
                mockPitchState.value.copy(
                    currentNote = perfectNote.copy(swar = "R"),
                )
            testScheduler.advanceUntilIdle()
            testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)
            assertThat(trainingViewModel.state.value.currentScore).isEqualTo(350)
            assertThat(trainingViewModel.state.value.comboCount).isEqualTo(2)

            // Third note (G) - combo should be 3, score = 350 + 100 + 50*3 = 600
            mockPitchState.value =
                mockPitchState.value.copy(
                    currentNote = perfectNote.copy(swar = "G"),
                )
            testScheduler.advanceUntilIdle()
            testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)
            assertThat(trainingViewModel.state.value.currentScore).isEqualTo(600)
            assertThat(trainingViewModel.state.value.comboCount).isEqualTo(3)
        }

    @Test
    fun `sessionCompletion_allNotesCompleted_marksSessionComplete`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val perfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )

            // Level 1 has 7 notes - complete them all
            repeat(7) {
                val expectedSwar = trainingViewModel.state.value.currentSwar
                mockPitchState.value =
                    mockPitchState.value.copy(
                        currentNote = perfectNote.copy(swar = expectedSwar!!),
                    )
                testScheduler.advanceUntilIdle()
                testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)
                testScheduler.advanceUntilIdle()
            }

            assertThat(trainingViewModel.state.value.isSessionComplete).isTrue()
            assertThat(trainingViewModel.state.value.earnedStars).isGreaterThan(0)
            verify { pitchViewModel.toggleRecording() } // Should stop recording
            verify(atLeast = 1) { pitchViewModel.toggleTanpura() } // Should stop tanpura
        }

    @Test
    fun `sessionCompletion_calculatesStarsCorrectly_threeStars`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val perfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )

            // Complete all 7 notes perfectly
            repeat(7) {
                val expectedSwar = trainingViewModel.state.value.currentSwar
                mockPitchState.value =
                    mockPitchState.value.copy(
                        currentNote = perfectNote.copy(swar = expectedSwar!!),
                    )
                testScheduler.advanceUntilIdle()
                testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)
                testScheduler.advanceUntilIdle()
            }

            // Perfect score should give 3 stars
            // Max score = 2100 (7 notes with max combo)
            // Actual score = 2100 (all perfect)
            // Percentage = 100% >= 85% threshold
            assertThat(trainingViewModel.state.value.earnedStars).isEqualTo(3)
        }

    @Test
    fun `sessionBestScore_persistsAcrossReset`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val perfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )

            // First session: complete all 7 notes perfectly for high score
            repeat(7) {
                val expectedSwar = trainingViewModel.state.value.currentSwar
                mockPitchState.value =
                    mockPitchState.value.copy(
                        currentNote = perfectNote.copy(swar = expectedSwar!!),
                    )
                testScheduler.advanceUntilIdle()
                testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)
                testScheduler.advanceUntilIdle()
            }

            val firstSessionScore = trainingViewModel.state.value.currentScore
            val bestScore = trainingViewModel.state.value.sessionBestScore
            assertThat(firstSessionScore).isEqualTo(bestScore)
            assertThat(bestScore).isEqualTo(LEVEL_1_MAX_SCORE) // 7 perfect notes max score

            // Reset - session best score should persist
            trainingViewModel.resetSession()
            assertThat(trainingViewModel.state.value.sessionBestScore).isEqualTo(bestScore)
            assertThat(trainingViewModel.state.value.currentScore).isEqualTo(0) // Current score resets
            assertThat(trainingViewModel.state.value.comboCount).isEqualTo(0) // Combo resets
        }

    @Test
    fun `sessionBestScore_updatesWhenHigher`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)
            testScheduler.advanceTimeBy(COUNTDOWN_AND_BUFFER_MS)

            val perfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swar = "S",
                    octave = HindustaniNoteConverter.Octave.MADHYA,
                    centsDeviation = 0.0,
                    isPerfect = true,
                    isFlat = false,
                    isSharp = false,
                )

            // Complete first 3 notes for initial best score
            repeat(3) {
                val expectedSwar = trainingViewModel.state.value.currentSwar
                mockPitchState.value =
                    mockPitchState.value.copy(
                        currentNote = perfectNote.copy(swar = expectedSwar!!),
                    )
                testScheduler.advanceUntilIdle()
                testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)
                testScheduler.advanceUntilIdle()
            }
            val partialScore = trainingViewModel.state.value.currentScore
            assertThat(partialScore).isEqualTo(600) // 150 + 200 + 250

            // Complete remaining 4 notes to finish session with higher score
            repeat(4) {
                val expectedSwar = trainingViewModel.state.value.currentSwar
                mockPitchState.value =
                    mockPitchState.value.copy(
                        currentNote = perfectNote.copy(swar = expectedSwar!!),
                    )
                testScheduler.advanceUntilIdle()
                testScheduler.advanceTimeBy(HOLD_DURATION_AND_BUFFER_MS)
                testScheduler.advanceUntilIdle()
            }

            val finalScore = trainingViewModel.state.value.currentScore
            assertThat(finalScore).isEqualTo(LEVEL_1_MAX_SCORE) // All 7 notes perfect
            assertThat(trainingViewModel.state.value.sessionBestScore).isEqualTo(LEVEL_1_MAX_SCORE)
        }

    @Test
    fun `TrainingLevel_fromInt_invalidLevel_defaultsToLevel1`() =
        runTest {
            val invalidLevel = TrainingLevel.fromInt(INVALID_LEVEL_NUMBER)
            assertThat(invalidLevel).isEqualTo(TrainingLevel.LEVEL_1)
            assertThat(invalidLevel.levelNumber).isEqualTo(1)
            assertThat(invalidLevel.randomized).isFalse()
        }

    @Test
    fun `TrainingLevel_fromInt_validLevels_returnsCorrectLevel`() =
        runTest {
            assertThat(TrainingLevel.fromInt(1)).isEqualTo(TrainingLevel.LEVEL_1)
            assertThat(TrainingLevel.fromInt(2)).isEqualTo(TrainingLevel.LEVEL_2)
            assertThat(TrainingLevel.fromInt(3)).isEqualTo(TrainingLevel.LEVEL_3)
            assertThat(TrainingLevel.fromInt(4)).isEqualTo(TrainingLevel.LEVEL_4)
        }

    @Test
    fun `playReferenceNote_withCurrentSwar_doesNotCrash`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel, context = context)

            // Call playReferenceNote - should not crash
            // Note: ReferenceNotePlayer is created inside TrainingViewModel and can't be mocked
            // This is a smoke test to ensure the method executes without errors
            trainingViewModel.playReferenceNote()

            // Method should complete without throwing
            assertThat(trainingViewModel.state.value.currentSwar).isNotNull()
        }
}
