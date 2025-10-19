# Training Feature Implementation Plan

## Overview
Add a new "Training" section to develop users' ear training and tuning skills using interactive exercises where they adjust a virtual slider to match target notes.

## Core Requirements

### User Interface
- **Navigation:** New bottom navigation tab for Training
- **Interaction:** Virtual slider/dial to adjust pitch + "Check" button to submit
- **Visual Feedback:** Target note name displayed (e.g., "P"), NO audio playback of target
- **Result Display:** Show cents deviation + success/fail ONLY after user submits

### Audio System
- **Out-of-tune Note:** String pluck sound playable initially + on-demand replay button
- **Target Note:** Visual display only (note name), no audio playback
- **Implementation:** Synthesized string pluck at variable frequencies

### Game Mechanics
- **Feedback:** Show deviation and success/fail after user clicks "Check"
- **Scoring:** Time + accuracy based points system
- **Progression:** Start with 7 basic swaras (S, R, G, M, P, D, N), unlock 12 chromatic (including r, g, m, d, n) after mastery
- **Tolerance:** Progressive difficulty
  - Level 1: ±20 cents (relaxed)
  - Level 2: ±10 cents (moderate)
  - Level 3: ±5 cents (strict)
- **Pitch System:** Relative to user's configured Sa setting

### Training Flow
1. App displays target note name (e.g., "G")
2. App plays out-of-tune string pluck (randomly detuned ±30-80 cents)
3. User adjusts slider to tune the note by ear
4. User can replay the out-of-tune note anytime
5. User clicks "Check" when ready
6. App shows:
   - Actual cents deviation
   - Success/fail based on current level tolerance
   - Score earned (based on accuracy + time)
   - "Next" button to continue

## Implementation Phases

### Phase 1: Audio Infrastructure
**File:** `app/src/main/java/com/hindustani/pitchdetector/audio/StringPluckPlayer.kt`

**Checklist:**
- [ ] Create `StringPluckPlayer.kt` class
- [ ] Implement sine wave generation at variable frequencies
- [ ] Apply exponential decay envelope to simulate string pluck
- [ ] Create public method: `play(frequency: Double)`
- [ ] Follow existing audio architecture patterns from `TanpuraPlayer.kt`
- [ ] Configure sample rate: 44100 Hz
- [ ] Set duration: ~1 second with decay
- [ ] Apply envelope: Exponential decay (`exp(-t/τ)`)
- [ ] Use audio format: PCM 16-bit mono
- [ ] Implement proper resource cleanup (release AudioTrack)
- [ ] Test audio playback at various frequencies (100 Hz - 1000 Hz)

### Phase 2: Data Layer
**Files:**
- `app/src/main/java/com/hindustani/pitchdetector/data/TrainingProgress.kt`
- `app/src/main/java/com/hindustani/pitchdetector/data/TrainingProgressRepository.kt`

**Checklist:**
- [ ] Create `TrainingProgress.kt` data class with fields:
  - [ ] `currentLevel: Int = 1`
  - [ ] `completedExercises: Int = 0`
  - [ ] `totalScore: Int = 0`
  - [ ] `unlockedSwaras: Set<String> = setOf("S", "R", "G", "M", "P", "D", "N")`
- [ ] Create `TrainingProgressRepository.kt` following `UserSettingsRepository` pattern
- [ ] Set up DataStore with name "training_progress"
- [ ] Define PreferencesKeys for all fields
- [ ] Implement `val trainingProgress: Flow<TrainingProgress>`
- [ ] Create method: `suspend fun updateLevel(level: Int)`
- [ ] Create method: `suspend fun incrementExercises()`
- [ ] Create method: `suspend fun updateScore(score: Int)`
- [ ] Create method: `suspend fun unlockChromatic()`
- [ ] Test data persistence across app restarts

### Phase 3: State Management
**Files:**
- `app/src/main/java/com/hindustani/pitchdetector/data/TrainingState.kt`
- `app/src/main/java/com/hindustani/pitchdetector/viewmodel/TrainingViewModel.kt`

**Checklist:**
- [ ] Create `TrainingState.kt` data class with all required fields
- [ ] Create `TrainingViewModel.kt` extending `AndroidViewModel`
- [ ] Set up `MutableStateFlow<TrainingState>` and expose immutable `StateFlow`
- [ ] Inject `TrainingProgressRepository` into ViewModel
- [ ] Implement `generateExercise()` function:
  - [ ] Select random target note from available swaras (based on level)
  - [ ] Calculate target frequency using user's Sa and note ratios
  - [ ] Generate detuned frequency (random ±30-80 cents from target)
  - [ ] Update state with new exercise
- [ ] Implement `onSliderChanged(frequency: Double)` to update current frequency
- [ ] Implement `onReplayNote()` to call `StringPluckPlayer.play()`
- [ ] Implement `onCheckTuning()`:
  - [ ] Calculate deviation in cents between slider and target
  - [ ] Determine success based on tolerance
  - [ ] Calculate score using formula: `basePoints * (1 - deviation/tolerance) * timeBonus`
  - [ ] Update state with results
  - [ ] Update `showResult = true`
- [ ] Implement `onNextExercise()`:
  - [ ] Save progress to repository if successful
  - [ ] Check for level progression (10 successful exercises)
  - [ ] Generate new exercise
  - [ ] Reset `showResult = false`
- [ ] Implement level progression logic
- [ ] Initialize first exercise on ViewModel creation
- [ ] Observe user's Sa from UserSettingsRepository

### Phase 4: Navigation Update
**Files to modify:**
- `app/src/main/java/com/hindustani/pitchdetector/AppRoutes.kt`
- `app/src/main/java/com/hindustani/pitchdetector/ui/MainActivity.kt`

**Checklist:**
- [ ] Update `AppRoutes.kt` - add `const val TRAINING = "training"`
- [ ] Create `TrainingViewModel` in `MainActivity`
- [ ] Refactor `AppNavigation` to use `Scaffold` with `NavigationBar`
- [ ] Define bottom navigation items:
  - [ ] Main screen (Home icon - `Icons.Default.Home`)
  - [ ] Training screen (School icon - `Icons.Default.School`)
- [ ] Update `NavigationBar` with click handlers:
  - [ ] Navigate to correct route
  - [ ] Pop up to start destination
  - [ ] Set `launchSingleTop = true`
  - [ ] Enable `restoreState = true`
- [ ] Add training composable route to `NavHost`:
  - [ ] Route: `AppRoutes.TRAINING`
  - [ ] Pass `TrainingViewModel` to `TrainingScreen`
- [ ] Keep Settings and FindSa as full-screen destinations (no bottom nav)
- [ ] Test navigation between Main and Training tabs
- [ ] Verify state is preserved when switching tabs

### Phase 5: UI Components
**File:** `app/src/main/java/com/hindustani/pitchdetector/ui/training/TrainingScreen.kt`

**Checklist:**
- [ ] Create `TrainingScreen.kt` composable
- [ ] Collect `TrainingViewModel.uiState` as Compose state
- [ ] **Header Section:**
  - [ ] Display level indicator (e.g., "Level 2: ±10 cents")
  - [ ] Display total score from progress
  - [ ] Show progress indicator (exercises completed in current level)
- [ ] **Exercise Area:**
  - [ ] Display large target note (similar to `NoteDisplay` component)
  - [ ] Show instruction text "Tune to: [note]"
- [ ] **Tuning Controls:**
  - [ ] Implement frequency slider:
    - [ ] Range: target frequency ±100 cents
    - [ ] OnValueChange calls `viewModel.onSliderChanged()`
    - [ ] Disabled when showing result
  - [ ] Add "Play" button to replay out-of-tune note:
    - [ ] OnClick calls `viewModel.onReplayNote()`
    - [ ] Show speaker/play icon
  - [ ] Optionally display current slider frequency (Hz)
- [ ] **Action Buttons:**
  - [ ] "Check" button:
    - [ ] Enabled only when slider has moved from initial position
    - [ ] OnClick calls `viewModel.onCheckTuning()`
    - [ ] Disabled after clicking (until next exercise)
  - [ ] Timer display showing elapsed time since exercise start
- [ ] **Result Overlay** (conditional on `state.showResult`):
  - [ ] Animated entry (fade + slide)
  - [ ] Display cents deviation (e.g., "+12¢" or "-8¢")
  - [ ] Show visual indicator:
    - [ ] ✓ Success (green) if within tolerance
    - [ ] ✗ Try Again (red) if outside tolerance
  - [ ] Display score earned this round
  - [ ] Color-coded background (green/orange/red based on deviation)
  - [ ] "Next" button calling `viewModel.onNextExercise()`
- [ ] **Design & Polish:**
  - [ ] Follow Material 3 design system
  - [ ] Use theme colors and typography from existing codebase
  - [ ] Create stateless sub-composables for reusability
  - [ ] Add smooth animations for result overlay
  - [ ] Ensure responsive layout for different screen sizes
- [ ] Test UI interactions with real ViewModel

### Phase 6: Game Logic

**Checklist:**
- [ ] **Exercise Generation:**
  - [ ] Implement swara selection based on level:
    - [ ] Level 1: 7 basic swaras (S, R, G, M, P, D, N)
    - [ ] Level 2+: All 12 chromatic swaras
  - [ ] Random target note selection from available swaras
  - [ ] Calculate target frequency using `HindustaniNoteConverter` and user's Sa
  - [ ] Generate random detuning (±30 to ±80 cents)
  - [ ] Calculate detuned frequency: `targetFreq * 2^(detuneCents/1200)`
- [ ] **Tolerance Configuration:**
  - [ ] Level 1: ±20 cents tolerance
  - [ ] Level 2: ±10 cents tolerance
  - [ ] Level 3: ±5 cents tolerance
  - [ ] Store current tolerance in state
- [ ] **Level Progression Logic:**
  - [ ] Track successful exercises per level
  - [ ] Require 10 successful exercises to unlock next level
  - [ ] Unlock chromatic swaras when advancing to Level 2
  - [ ] Allow user to practice previous levels after unlocking
  - [ ] Persist level progress to repository
- [ ] **Score Calculation:**
  - [ ] Implement accuracy multiplier: `max(0, 1 - abs(deviation) / tolerance)`
  - [ ] Implement time bonus: `max(0.5, 1.5 - (elapsedSeconds / 30))`
    - [ ] Bonus for completion < 20 seconds
    - [ ] No penalty after 30 seconds
  - [ ] Final score: `round(100 * accuracyMultiplier * timeBonus)`
  - [ ] Award 0 points if outside tolerance
- [ ] **Success Criteria:**
  - [ ] Check if `abs(deviation) <= tolerance`
  - [ ] No minimum time requirement
- [ ] **Integration:**
  - [ ] Use `HindustaniNoteConverter` for frequency calculations
  - [ ] Integrate with user's Sa setting from `UserSettingsRepository`
  - [ ] Handle edge cases (Sa not set, invalid frequencies)

### Phase 7: Testing

**Unit Tests Checklist:**

**`TrainingViewModelTest.kt`:**
- [ ] Create test file: `app/src/test/.../viewmodel/TrainingViewModelTest.kt`
- [ ] Test exercise generation:
  - [ ] `generateExercise_level1_usesBasicSwaras()`
  - [ ] `generateExercise_level2_usesChromaticSwaras()`
  - [ ] `generateExercise_calculatesCorrectTargetFrequency()`
  - [ ] `generateExercise_detunesWithinRange()` (±30-80 cents)
- [ ] Test scoring algorithm:
  - [ ] `calculateScore_perfectTuning_gives100Points()`
  - [ ] `calculateScore_withinTolerance_givesPartialPoints()`
  - [ ] `calculateScore_outsideTolerance_givesZeroPoints()`
  - [ ] `calculateScore_fastCompletion_givesTimeBonus()`
  - [ ] `calculateScore_slowCompletion_reducesTimeBonus()`
- [ ] Test level progression:
  - [ ] `levelProgression_10SuccessfulExercises_advancesToLevel2()`
  - [ ] `levelProgression_advancingLevel_reducesTolerance()`
  - [ ] `levelProgression_level2_unlocksChromatic()`
- [ ] Test ViewModel methods:
  - [ ] `onSliderChanged_updatesState()`
  - [ ] `onCheckTuning_calculatesDeviation()`
  - [ ] `onNextExercise_generatesNewExercise()`

**`StringPluckPlayerTest.kt`:**
- [ ] Create test file: `app/src/test/.../audio/StringPluckPlayerTest.kt`
- [ ] Test PCM generation:
  - [ ] `generatePCM_producesCorrectBufferSize()`
  - [ ] `generatePCM_variousFrequencies_producesValidWaveform()`
- [ ] Test envelope:
  - [ ] `applyEnvelope_producesDecay()`
  - [ ] `applyEnvelope_peakAtStart()`

**UI Tests Checklist:**

**`TrainingScreenTest.kt`:**
- [ ] Create test file: `app/src/androidTest/.../ui/training/TrainingScreenTest.kt`
- [ ] Test UI interactions:
  - [ ] `trainingScreen_displays_targetNote()`
  - [ ] `slider_whenMoved_updatesViewModel()`
  - [ ] `playButton_whenClicked_replaysNote()`
  - [ ] `checkButton_whenClicked_showsResult()`
  - [ ] `resultOverlay_displaysDeviation()`
  - [ ] `resultOverlay_displaysScore()`
  - [ ] `nextButton_whenClicked_loadsNewExercise()`
  - [ ] `scoreDisplay_updates_afterSuccessfulExercise()`
  - [ ] `levelIndicator_updates_afterProgression()`

**Manual Testing Checklist:**
- [ ] Audio playback on physical device works correctly
- [ ] Slider is responsive and smooth
- [ ] Score calculation matches expected values
- [ ] Level progression triggers at 10 successful exercises
- [ ] Chromatic swaras unlock at Level 2
- [ ] Bottom navigation switches between Main and Training smoothly
- [ ] Training state persists across app restarts
- [ ] Result overlay animates smoothly
- [ ] Timer updates correctly during exercise
- [ ] "Check" button disabled state works correctly
- [ ] Replay button produces audible sound
- [ ] Different tolerance levels feel appropriately challenging

## Architecture Alignment

### Following Existing Patterns

**Audio:** `StringPluckPlayer` follows `TanpuraPlayer` pattern
- AudioTrack for playback
- PCM generation in Kotlin
- Proper resource cleanup

**ViewModel:** `TrainingViewModel` follows `PitchViewModel` pattern
- StateFlow for reactive state
- `viewModelScope` for coroutines
- `_uiState.update { }` for state modifications

**Persistence:** `TrainingProgressRepository` follows `UserSettingsRepository` pattern
- DataStore for preferences
- Flow-based reactive API
- Suspend functions for updates

**UI:** `TrainingScreen` follows `MainScreen` pattern
- Stateless composables
- Material 3 design
- Compose Navigation integration

## Success Metrics

**User Experience:**
- Smooth 60fps UI performance
- Audio playback latency < 100ms
- Immediate response to "Check" button
- Clear visual feedback on success/failure

**Educational Value:**
- Progressive difficulty encourages skill development
- No real-time visual feedback forces ear training
- Time bonus encourages confident decision-making
- Score system provides motivation

## Future Enhancements (Out of Scope)

1. **22-shruti mode:** Advanced microtonal training
2. **Custom exercise sets:** User-defined note sequences
3. **Interval training:** Recognize intervals between notes
4. **Melodic patterns:** Multi-note phrase recognition
5. **Leaderboard:** Compare scores with other users
6. **Statistics:** Track accuracy trends over time
7. **Haptic feedback:** Vibration on success/failure

## References

- Existing codebase patterns in `PitchViewModel.kt`, `TanpuraPlayer.kt`, `UserSettingsRepository.kt`
- Hindustani note ratios in `HindustaniNoteConverter.kt`
- Material 3 Design Guidelines
- Jetpack Compose best practices
