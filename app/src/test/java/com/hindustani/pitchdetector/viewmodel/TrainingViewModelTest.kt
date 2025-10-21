package com.hindustani.pitchdetector.viewmodel

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

/**
 * Unit tests for TrainingViewModel
 * Tests training session logic including initialization, state management, and note progression
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {
    private lateinit var pitchViewModel: PitchViewModel
    private lateinit var trainingViewModel: TrainingViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

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

        pitchViewModel = mockk(relaxed = true)
        every { pitchViewModel.pitchState } returns mockPitchState
        every { pitchViewModel.isTanpuraPlaying } returns mockIsTanpuraPlaying
        every { pitchViewModel.isRecording } returns mockIsRecording
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========== INITIALIZATION TESTS ==========

    @Test
    fun `initialization_level1_startsWithCorrectLevel`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.state.value.level).isEqualTo(1)
            assertThat(trainingViewModel.state.value.currentSwara).isEqualTo("S")
        }

    @Test
    fun `initialization_level2_startsWithCorrectLevel`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 2, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.state.value.level).isEqualTo(2)
            assertThat(trainingViewModel.state.value.currentSwara).isNotNull()
        }

    @Test
    fun `initialization_level3_startsWithCorrectLevel`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 3, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.state.value.level).isEqualTo(3)
            assertThat(trainingViewModel.state.value.currentSwara).isEqualTo("S")
        }

    @Test
    fun `initialization_level4_startsWithCorrectLevel`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 4, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.state.value.level).isEqualTo(4)
            assertThat(trainingViewModel.state.value.currentSwara).isNotNull()
        }

    @Test
    fun `initialization_setsTanpuraString1ToPa`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            verify { pitchViewModel.updateTanpuraString1("P") }
        }

    @Test
    fun `initialization_startsTanpuraIfNotPlaying`() =
        runTest {
            mockIsTanpuraPlaying.value = false

            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            verify { pitchViewModel.toggleTanpura() }
        }

    @Test
    fun `initialization_doesNotStartTanpuraIfAlreadyPlaying`() =
        runTest {
            mockIsTanpuraPlaying.value = true

            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            verify(exactly = 0) { pitchViewModel.toggleTanpura() }
        }

    @Test
    fun `initialization_startsCountdownAt3`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.state.value.countdown).isEqualTo(3)
        }

    // ========== PITCH OBSERVATION TESTS ==========

    @Test
    fun `observePitch_correctNoteDetected_setsIsHoldingCorrectly`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            testScheduler.advanceTimeBy(3100) // Skip countdown

            val correctNote =
                HindustaniNoteConverter.HindustaniNote(
                    swara = "S",
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
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            testScheduler.advanceTimeBy(3100)

            val incorrectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swara = "R",
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
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            testScheduler.advanceTimeBy(3100)

            val imperfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swara = "S",
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
    fun `observePitch_correctSwaraButFlat_setsIsFlatTrue`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            testScheduler.advanceTimeBy(3100)

            val flatNote =
                HindustaniNoteConverter.HindustaniNote(
                    swara = "S",
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
            assertThat(trainingViewModel.state.value.detectedSwara).isEqualTo("S")
        }

    @Test
    fun `observePitch_correctSwaraButSharp_setsIsSharpTrue`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            testScheduler.advanceTimeBy(3100)

            val sharpNote =
                HindustaniNoteConverter.HindustaniNote(
                    swara = "S",
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
            assertThat(trainingViewModel.state.value.detectedSwara).isEqualTo("S")
        }

    @Test
    fun `observePitch_wrongSwaraEvenIfFlat_doesNotSetIsFlat`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            testScheduler.advanceTimeBy(3100)

            val wrongNoteFlat =
                HindustaniNoteConverter.HindustaniNote(
                    swara = "R",
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
            assertThat(trainingViewModel.state.value.detectedSwara).isEqualTo("R")
        }

    @Test
    fun `observePitch_perfectNote_setsBothFlatAndSharpFalse`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            testScheduler.advanceTimeBy(3100)

            val perfectNote =
                HindustaniNoteConverter.HindustaniNote(
                    swara = "S",
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
    fun `observePitch_nullNote_clearsDetectedSwaraAndFlatSharpFlags`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            testScheduler.advanceTimeBy(3100)

            mockPitchState.value = mockPitchState.value.copy(currentNote = null)
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.detectedSwara).isNull()
            assertThat(trainingViewModel.state.value.isFlat).isFalse()
            assertThat(trainingViewModel.state.value.isSharp).isFalse()
        }

    // ========== RESET SESSION TESTS ==========

    @Test
    fun `resetSession_resetsCurrentNoteIndexTo0`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            trainingViewModel.resetSession()
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.currentNoteIndex).isEqualTo(0)
        }

    @Test
    fun `resetSession_resetsHoldProgressTo0`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            trainingViewModel.resetSession()
            testScheduler.advanceUntilIdle()

            assertThat(trainingViewModel.state.value.holdProgress).isEqualTo(0f)
        }

    // ========== TOGGLE TANPURA TESTS ==========

    @Test
    fun `toggleTanpura_ensuresString1IsSetToPa`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            clearMocks(pitchViewModel, answers = false)

            trainingViewModel.toggleTanpura()

            verify { pitchViewModel.updateTanpuraString1("P") }
            verify { pitchViewModel.toggleTanpura() }
        }

    // ========== STATE STRUCTURE TESTS ==========

    @Test
    fun `state_initiallyNotComplete`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.state.value.isSessionComplete).isFalse()
        }

    @Test
    fun `state_initialHoldProgressIsZero`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.state.value.holdProgress).isEqualTo(0f)
        }

    @Test
    fun `state_exposesCorrectLevel`() =
        runTest {
            val viewModel1 = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)
            val viewModel2 = TrainingViewModel(level = 2, pitchViewModel = pitchViewModel)
            val viewModel3 = TrainingViewModel(level = 3, pitchViewModel = pitchViewModel)
            val viewModel4 = TrainingViewModel(level = 4, pitchViewModel = pitchViewModel)

            assertThat(viewModel1.state.value.level).isEqualTo(1)
            assertThat(viewModel2.state.value.level).isEqualTo(2)
            assertThat(viewModel3.state.value.level).isEqualTo(3)
            assertThat(viewModel4.state.value.level).isEqualTo(4)
        }

    @Test
    fun `saNote_flowIsExposed`() =
        runTest {
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.saNote.value).isEqualTo("C3")
        }

    @Test
    fun `isTanpuraPlaying_flowIsExposed`() =
        runTest {
            mockIsTanpuraPlaying.value = true
            trainingViewModel = TrainingViewModel(level = 1, pitchViewModel = pitchViewModel)

            assertThat(trainingViewModel.isTanpuraPlaying.value).isTrue()
        }
}
