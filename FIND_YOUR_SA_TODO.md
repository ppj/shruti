# Find Your Sa - Implementation Checklist

## Feature Overview
Help users discover their ideal Sa (tonic) note based on their comfortable vocal range through a guided vocal test.

## Phase 1: State & ViewModel Foundation
- [ ] Create `ui/findsa/FindSaState.kt`
  - [ ] Define `FindSaState` sealed class (NotStarted, Recording, Analyzing, Finished)
  - [ ] Define `FindSaUiState` data class
- [ ] Create `viewmodel/FindSaViewModel.kt`
  - [ ] Set up StateFlow for FindSaUiState
  - [ ] Instantiate AudioCaptureManager and PYINDetector
  - [ ] Create stub functions: startTest(), stopTest(), acceptRecommendation()
  - [ ] Add temporary list for collecting valid pitches

## Phase 2: UI Components & Navigation
- [ ] Create `ui/findsa/FindSaScreen.kt`
  - [ ] Onboarding view with instructions
  - [ ] Recording view with real-time feedback
  - [ ] Analyzing view with progress indicator
  - [ ] Results view with recommendation display
- [ ] Create `ui/components/PianoKeyboard.kt`
  - [ ] Draw piano keyboard visualization
  - [ ] Highlight recommended Sa note
  - [ ] Show detected range on keyboard
- [ ] Modify `ui/MainActivity.kt`
  - [ ] Add "findSa" route to NavHost
  - [ ] Instantiate FindSaViewModel
  - [ ] Pass to FindSaScreen
- [ ] Modify `ui/SettingsScreen.kt`
  - [ ] Add "Find Your Sa" button/card
  - [ ] Wire up navigation to findSa route

## Phase 3: Core Algorithm Implementation
- [ ] Create standard Sa notes map (C3-A3 with frequencies)
- [ ] Implement `startTest()` in FindSaViewModel
  - [ ] Start audio capture
  - [ ] Collect pitches with confidence > 0.8
  - [ ] Store in temporary list
- [ ] Implement `stopTest()` in FindSaViewModel
  - [ ] Stop audio capture
  - [ ] Sort collected frequencies
  - [ ] Remove outliers (bottom/top 10%)
  - [ ] Find minimum comfortable frequency
  - [ ] Calculate ideal Sa: `min × 2^(7/12)`
  - [ ] Snap to nearest standard Sa note
  - [ ] Update state to Finished with result
- [ ] Create Hz to note name conversion helpers (if needed)

## Phase 4: Integration & Polish
- [ ] Pass TanpuraPlayer instance to FindSaViewModel
- [ ] Implement "Listen" button functionality
  - [ ] Play recommended Sa note
- [ ] Implement `acceptRecommendation()`
  - [ ] Call PitchViewModel.updateSa()
  - [ ] Persist to DataStore
  - [ ] Navigate back to settings
- [ ] Implement adjustment buttons (±1 semitone)
- [ ] Test tanpura updates correctly with new Sa

## Phase 5: Testing
- [ ] Create `test/.../FindSaViewModelTest.kt`
  - [ ] Test outlier removal logic
  - [ ] Test semitone calculation (7 semitones formula)
  - [ ] Test snapping to nearest standard note
  - [ ] Test state transitions
- [ ] Manual E2E testing
  - [ ] Test with different voice types
  - [ ] Verify Sa persists after app restart
  - [ ] Confirm tanpura updates correctly
  - [ ] Test edge cases (very high/low voices)

## Technical Notes

### Standard Sa Notes by Voice Type
- **Male**: C3 (130.81 Hz), C#3 (138.59 Hz), D3 (146.83 Hz)
- **Female**: G3 (196.00 Hz), G#3 (207.65 Hz), A3 (220.00 Hz)

### Algorithm Formula
```
idealSaFreq = lowestComfortableFreq × 2^(7/12)
```
This places Sa approximately 7 semitones (a perfect fifth) above the lowest comfortable note.

### Filtering Strategy
- Confidence threshold: > 0.8 (stricter than display threshold of 0.5)
- Outlier removal: Discard bottom 10% and top 10% of collected frequencies
- Duration: 10-15 second test recommended

---

**Created:** 2025-10-13
**Status:** Not Started
