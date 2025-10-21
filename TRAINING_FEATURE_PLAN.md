# Training Mode Feature Implementation Plan

## Overview
Add a training mode to the Shruti app where users practice holding specific swaras (notes) accurately for a fixed duration while the tanpura plays in their selected scale.

## Feature Requirements

### User Requirements (from discussion)
- **Accuracy**: Use existing tolerance setting from UserSettings (no new configuration)
- **Note Sequences**:
  - Level 1: 7 shuddha swaras (S, R, G, m, P, D, N)
  - Level 2: All 12 notes including komal/teevra variants (S, r, R, g, G, m, M, P, d, D, n, N)
- **Hold Duration**: Fixed 5 seconds
- **Progression**: Auto-advance to next note after successful hold

### Technical Requirements
- Tanpura auto-starts with user's Sa when training begins
- Visual progress indicator shows hold time (0-5 seconds)
- Color feedback when note is held correctly (green) vs incorrect (default)
- Display current target swara and preview of next swara
- Completion dialog when all notes in level are completed

---

## Phase 1: Data Layer

### 1.1 Create TrainingState Data Class
**File**: `app/src/main/java/com/hindustani/pitchdetector/data/TrainingState.kt`

**Checklist**:
- [ ] Create package `com.hindustani.pitchdetector.data` (already exists)
- [ ] Define `TrainingState` data class with fields:
  - [ ] `level: Int` (1 or 2)
  - [ ] `currentNoteIndex: Int` (position in sequence)
  - [ ] `currentSwara: String?` (e.g., "S", "R", "g")
  - [ ] `nextSwara: String?` (preview)
  - [ ] `holdProgress: Float` (0.0f to 1.0f for UI)
  - [ ] `isHoldingCorrectly: Boolean` (pitch within tolerance)
  - [ ] `isSessionComplete: Boolean` (all notes done)
  - [ ] `countdown: Int` (3-2-1 before starting, or 0 if started)
- [ ] Add KDoc documentation

**Estimated effort**: 15 minutes

---

## Phase 2: ViewModel Layer

### 2.1 Create TrainingViewModel
**File**: `app/src/main/java/com/hindustani/pitchdetector/viewmodel/TrainingViewModel.kt`

**Checklist**:
- [ ] Create `TrainingViewModel` extending `AndroidViewModel`
- [ ] Add constructor dependencies:
  - [ ] `Application` (for context)
  - [ ] Level parameter (passed from navigation)
- [ ] Define companion object with constants:
  - [ ] `HOLD_DURATION_MILLIS = 5000L`
  - [ ] `LEVEL_1_NOTES = listOf("S", "R", "G", "m", "P", "D", "N")`
  - [ ] `LEVEL_2_NOTES = listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N")`
  - [ ] `COUNTDOWN_START = 3`

#### 2.2 State Management
- [ ] Create `MutableStateFlow<TrainingState>` (private)
- [ ] Expose `StateFlow<TrainingState>` (public)
- [ ] Initialize state based on level parameter

#### 2.3 Integration with Existing Components
- [ ] Get `PitchViewModel` instance (inject or obtain from ViewModelProvider)
- [ ] Get `UserSettingsRepository` instance
- [ ] Collect tolerance setting from UserSettings on init
- [ ] Access `TanpuraPlayer` through PitchViewModel

#### 2.4 Countdown Logic
- [ ] Implement countdown coroutine (3-2-1)
- [ ] Update state every second
- [ ] After countdown, start listening for pitch

#### 2.5 Pitch Tracking Logic
- [ ] Collect `pitchState` flow from `PitchViewModel`
- [ ] Implement `isPitchCorrect()` function:
  - [ ] Extract detected `HindustaniNote.swara` from pitch state
  - [ ] Compare with current target swara
  - [ ] Check if `isPerfect` flag is true (within tolerance)
  - [ ] Return boolean
- [ ] Track hold timer:
  - [ ] Record `holdStartTime` when pitch becomes correct
  - [ ] Update `holdProgress` continuously (0.0-1.0)
  - [ ] Reset timer if pitch goes incorrect
  - [ ] Call `advanceToNextNote()` when 5 seconds reached

#### 2.6 Progression Logic
- [ ] Implement `advanceToNextNote()`:
  - [ ] Increment `currentNoteIndex`
  - [ ] Update `currentSwara` and `nextSwara`
  - [ ] Reset `holdProgress` to 0
  - [ ] If reached end of sequence, set `isSessionComplete = true`
- [ ] Implement `resetSession()` for restarting

#### 2.7 Lifecycle Management
- [ ] Auto-start tanpura in `init` block
- [ ] Stop tanpura in `onCleared()`
- [ ] Cancel coroutines in `onCleared()`

**Estimated effort**: 2-3 hours

---

## Phase 3: UI Layer

### 3.1 Create TrainingScreen Composable
**File**: `app/src/main/java/com/hindustani/pitchdetector/ui/training/TrainingScreen.kt`

**Checklist**:
- [ ] Create package `com.hindustani.pitchdetector.ui.training`
- [ ] Create `TrainingScreen` composable function
- [ ] Accept `NavController` and `TrainingViewModel` parameters
- [ ] Collect state: `val state by viewModel.state.collectAsState()`

#### 3.2 Countdown UI
- [ ] Show countdown number (3, 2, 1) in large text
- [ ] Animate countdown (scale animation or fade)
- [ ] Hide when countdown reaches 0

#### 3.3 Main Training UI
- [ ] Display current target swara:
  - [ ] Large text (displayLarge typography)
  - [ ] Center of screen
- [ ] Display next swara preview:
  - [ ] Smaller text above main display
  - [ ] Format: "Next: {swara}" or "Next: -" if last note
- [ ] Circular progress indicator:
  - [ ] Size: 200.dp
  - [ ] Stroke width: 16.dp
  - [ ] Progress: `state.holdProgress`
  - [ ] Color: Green when `state.isHoldingCorrectly`, else MaterialTheme primary
  - [ ] Place target swara text in center of circle
- [ ] Optional: Add microphone icon or visual indicator that app is listening

#### 3.4 Completion Dialog
- [ ] Create `CompletionDialog` composable
- [ ] Show when `state.isSessionComplete == true`
- [ ] Display congratulations message
- [ ] Show level completed ("Level 1 Complete!" or "Level 2 Complete!")
- [ ] Buttons:
  - [ ] "Back to Main" - navigate back
  - [ ] Optional: "Repeat Level" - call `viewModel.resetSession()`

#### 3.5 Layout and Styling
- [ ] Use Material 3 theming
- [ ] Proper spacing and padding
- [ ] Responsive to different screen sizes
- [ ] Consider landscape orientation

**Estimated effort**: 2-3 hours

---

## Phase 4: Navigation Integration

### 4.1 Update AppRoutes
**File**: `app/src/main/java/com/hindustani/pitchdetector/constants/AppRoutes.kt`

**Checklist**:
- [ ] Add `const val TRAINING = "training"`

**Estimated effort**: 2 minutes

### 4.2 Update MainActivity Navigation
**File**: `app/src/main/java/com/hindustani/pitchdetector/ui/MainActivity.kt`

**Checklist**:
- [ ] Add `TrainingViewModel` to MainActivity:
  - [ ] Create ViewModelProvider.Factory to pass level parameter
  - [ ] Or use SavedStateHandle in ViewModel
- [ ] Add route to NavHost:
  - [ ] Route pattern: `"${AppRoutes.TRAINING}/{level}"`
  - [ ] Add navigation argument for level (IntType)
  - [ ] Composable lambda: `TrainingScreen(navController, viewModel)`
- [ ] Import TrainingScreen

**Estimated effort**: 30 minutes

### 4.3 Update MainScreen with Training Buttons
**File**: `app/src/main/java/com/hindustani/pitchdetector/ui/MainScreen.kt`

**Checklist**:
- [ ] Read current MainScreen layout to find appropriate location
- [ ] Add navigation parameter: `onNavigateToTraining: (Int) -> Unit`
- [ ] Add two buttons or a single button with dropdown:
  - [ ] "Training: Level 1" → `onNavigateToTraining(1)`
  - [ ] "Training: Level 2" → `onNavigateToTraining(2)`
- [ ] Style buttons consistently with existing UI
- [ ] Update MainActivity to pass navigation lambda

**Estimated effort**: 30 minutes

---

## Phase 5: Testing

### 5.1 Unit Tests for TrainingViewModel
**File**: `app/src/test/java/com/hindustani/pitchdetector/viewmodel/TrainingViewModelTest.kt`

**Checklist**:
- [ ] Test note sequences:
  - [ ] `testLevel1NotesSequence()` - verify 7 notes
  - [ ] `testLevel2NotesSequence()` - verify 12 notes
- [ ] Test hold timer:
  - [ ] `testHoldTimerStartsWhenCorrect()`
  - [ ] `testHoldTimerResetsWhenIncorrect()`
  - [ ] `testAdvancesAfter5Seconds()`
- [ ] Test progression:
  - [ ] `testAdvanceToNextNote()`
  - [ ] `testSessionCompleteAfterLastNote()`
- [ ] Test state updates:
  - [ ] `testHoldProgressUpdates()`
  - [ ] `testIsHoldingCorrectlyFlag()`
- [ ] Use MockK for mocking dependencies
- [ ] Use Google Truth for assertions

**Estimated effort**: 2 hours

### 5.2 UI Tests for TrainingScreen
**File**: `app/src/androidTest/java/com/hindustani/pitchdetector/ui/training/TrainingScreenTest.kt`

**Checklist**:
- [ ] Test countdown display:
  - [ ] `testCountdownDisplays()`
- [ ] Test note display:
  - [ ] `testTargetNoteDisplays()`
  - [ ] `testNextNotePreviewDisplays()`
- [ ] Test progress indicator:
  - [ ] `testProgressIndicatorUpdates()`
- [ ] Test completion dialog:
  - [ ] `testCompletionDialogShows()`
  - [ ] `testBackButtonNavigates()`
- [ ] Test navigation:
  - [ ] `testNavigationFromMainScreen()`

**Estimated effort**: 1.5 hours

---

## Phase 6: Documentation & Polish

### 6.1 Update Documentation
**Checklist**:
- [ ] Update README.md:
  - [ ] Add training mode feature description
  - [ ] Add screenshots (once UI is ready)
- [ ] Update CLAUDE.md:
  - [ ] Document training mode architecture
  - [ ] Add to project structure
  - [ ] Update component descriptions
- [ ] Add inline KDoc comments to all new classes

**Estimated effort**: 1 hour

### 6.2 Code Review & Cleanup
**Checklist**:
- [ ] Run ktlint: `./gradlew ktlintFormat`
- [ ] Run all tests: `./gradlew test connectedAndroidTest`
- [ ] Fix any lint warnings
- [ ] Review for DRY violations
- [ ] Ensure consistent naming conventions
- [ ] Check for proper resource cleanup

**Estimated effort**: 1 hour

---

## Implementation Timeline

| Phase | Estimated Time | Dependencies |
|-------|---------------|--------------|
| Phase 1: Data Layer | 15 min | None |
| Phase 2: ViewModel | 2-3 hours | Phase 1 |
| Phase 3: UI Layer | 2-3 hours | Phase 1, 2 |
| Phase 4: Navigation | 1 hour | Phase 1, 2, 3 |
| Phase 5: Testing | 3.5 hours | Phase 1, 2, 3, 4 |
| Phase 6: Documentation | 2 hours | All phases |

**Total Estimated Time**: 11-13 hours

---

## Git Commit Strategy

1. **Commit 1** (Phase 1): "Add TrainingState data class"
2. **Commit 2** (Phase 2): "Implement TrainingViewModel with hold tracking logic"
3. **Commit 3** (Phase 3): "Create TrainingScreen UI with progress indicator"
4. **Commit 4** (Phase 4): "Integrate training mode navigation"
5. **Commit 5** (Phase 5): "Add unit and UI tests for training mode"
6. **Commit 6** (Phase 6): "Update documentation and polish training feature"

---

## Open Questions / Future Enhancements

- [ ] Add haptic feedback when note is held correctly?
- [ ] Add sound effects for successful completion?
- [ ] Store training progress/statistics (completed levels, accuracy scores)?
- [ ] Add more levels (specific ragas, custom note sequences)?
- [ ] Add adjustable hold duration setting?
- [ ] Add accuracy statistics display during training?

---

## Technical Notes

### Accessing PitchViewModel from TrainingViewModel
Since both ViewModels need to be active simultaneously:
- Option A: Pass PitchViewModel instance to TrainingViewModel via factory
- Option B: Make audio capture a shared component/repository
- **Recommended**: Option A for simplicity

### Swara Matching Logic
The detected `HindustaniNote` already contains:
- `swara`: String representation (e.g., "S", "r", "R")
- `isPerfect`: Boolean (within tolerance)

Simply compare: `detectedNote.swara == targetSwara && detectedNote.isPerfect`

### Progress Calculation
```kotlin
val elapsedMillis = System.currentTimeMillis() - holdStartTime
val progress = (elapsedMillis.toFloat() / HOLD_DURATION_MILLIS).coerceIn(0f, 1f)
```
