# Training Mode Gamification Plan

## Overview
Add gamification elements to the existing training mode to make practice sessions more engaging, rewarding, and fun. Focus on quick wins with minimal implementation complexity.

## Goals
1. **Make practice fun**: Provide immediate satisfaction through visible progress and rewards
2. **Build habits**: Encourage users to replay levels and improve their performance
3. **Keep it simple**: Session-based scoring (no persistence), minimal UI changes

## Gamification Mechanics

### 1. Points Scoring System

**Point Awards per Note:**
- **Base Points**: 100 points for completing any note (holding for 2 seconds)
- **Accuracy Bonus**: +50 points if held perfectly throughout entire duration
  - "Perfect" = green circle throughout (isHoldingCorrectly = true for entire hold)
- **Combo Multiplier**: Consecutive perfect notes increase bonus multiplier
  - Perfect note #1: 100 + 50 = 150 points
  - Perfect note #2: 100 + 100 (50×2) = 200 points
  - Perfect note #3: 100 + 150 (50×3) = 250 points
  - Perfect note #N: 100 + (50×N) points
  - Combo resets if note is completed without being perfect throughout

**Maximum Possible Scores:**
- **Level 1** (7 notes): Base = 700, Max with combos = 100×7 + 50×(1+2+3+4+5+6+7) = 700 + 1400 = **2,100 points**
- **Level 2** (12 notes): Base = 1200, Max with combos = 100×12 + 50×(1+2+...+12) = 1200 + 3900 = **5,100 points**

### 2. Star Rating System

**Stars Awarded at Session Completion:**
- ⭐ **1 Star**: Complete all notes (baseline - always achieved if session finishes)
- ⭐⭐ **2 Stars**: Score ≥ 60% of max possible
  - Level 1: ≥ 1,260 points
  - Level 2: ≥ 3,060 points
- ⭐⭐⭐ **3 Stars**: Score ≥ 85% of max possible
  - Level 1: ≥ 1,785 points
  - Level 2: ≥ 4,335 points

**Session Replay:**
- Track "Session Best" score (resets when leaving training section)
- Display "Session Best: X" in completion dialog if user replays and improves

---

## Phase 1: Data Layer Updates

### 1.1 Update TrainingState Data Class
**File**: `app/src/main/java/com/hindustani/pitchdetector/data/TrainingState.kt`

**Checklist**:
- [ ] Add `currentScore: Int = 0` field
- [ ] Add `comboCount: Int = 0` field (tracks consecutive perfect notes)
- [ ] Add `sessionBestScore: Int = 0` field (best score in current session)
- [ ] Add `earnedStars: Int = 0` field (1-3, calculated at completion)
- [ ] Add `wasLastNotePerfect: Boolean = false` field (track for combo logic)
- [ ] Update KDoc documentation for new fields

**Estimated effort**: 15 minutes

---

## Phase 2: ViewModel Logic

### 2.1 Update TrainingViewModel Constants
**File**: `app/src/main/java/com/hindustani/pitchdetector/viewmodel/TrainingViewModel.kt`

**Checklist**:
- [ ] Add scoring constants to companion object:
  ```kotlin
  private const val BASE_POINTS = 100
  private const val ACCURACY_BONUS_BASE = 50
  private const val LEVEL_1_MAX_SCORE = 2100
  private const val LEVEL_2_MAX_SCORE = 5100
  private const val TWO_STAR_THRESHOLD = 0.60f  // 60%
  private const val THREE_STAR_THRESHOLD = 0.85f  // 85%
  ```

**Estimated effort**: 5 minutes

### 2.2 Implement Scoring Logic

**Checklist**:
- [ ] Track if note was held perfectly throughout duration:
  - [ ] Add private var `wasPerfectThroughout: Boolean = true`
  - [ ] In `observePitch()`: set to `false` if `isHoldingCorrectly` becomes false during hold
  - [ ] Reset to `true` when starting to track new note
- [ ] Update `advanceToNextNote()` to calculate and award points:
  - [ ] Award BASE_POINTS (100)
  - [ ] If `wasPerfectThroughout`:
    - [ ] Increment comboCount
    - [ ] Award combo bonus: `ACCURACY_BONUS_BASE × comboCount`
    - [ ] Set `wasLastNotePerfect = true`
  - [ ] Else:
    - [ ] Reset comboCount to 0
    - [ ] Set `wasLastNotePerfect = false`
  - [ ] Add points to `currentScore`
- [ ] Test edge cases (first note, combo breaks, last note)

**Estimated effort**: 45 minutes

### 2.3 Implement Star Calculation

**Checklist**:
- [ ] Create `calculateStars()` private function:
  - [ ] Takes final score as parameter
  - [ ] Determines max possible based on level
  - [ ] Calculates percentage: `score.toFloat() / maxScore`
  - [ ] Returns stars (1-3) based on thresholds
- [ ] Call `calculateStars()` in `advanceToNextNote()` when session completes
- [ ] Update state with earned stars
- [ ] Test with various score scenarios

**Estimated effort**: 30 minutes

### 2.4 Session Best Tracking

**Checklist**:
- [ ] In `advanceToNextNote()` when completing session:
  - [ ] Compare `currentScore` with `sessionBestScore`
  - [ ] Update `sessionBestScore` if current is higher
- [ ] In `resetSession()`:
  - [ ] Reset `currentScore` to 0
  - [ ] Reset `comboCount` to 0
  - [ ] Reset `earnedStars` to 0
  - [ ] Keep `sessionBestScore` unchanged (persists across replays)
- [ ] Test replay scenarios

**Estimated effort**: 15 minutes

**Total Phase 2 Effort**: ~1.5 hours

---

## Phase 3: UI Updates - TrainingScreen

### 3.1 Add Score Display to Top Bar
**File**: `app/src/main/java/com/hindustani/pitchdetector/ui/training/TrainingScreen.kt`

**Checklist**:
- [ ] Add score text to top bar Row:
  - [ ] Position: far right (after level indicator)
  - [ ] Format: "Score: {currentScore}"
  - [ ] Style: MaterialTheme.typography.titleMedium
  - [ ] Color: MaterialTheme.colorScheme.primary
- [ ] Ensure layout doesn't break on small screens
- [ ] Test with different score values (1-5 digits)

**Estimated effort**: 20 minutes

### 3.2 Add Combo Indicator

**Checklist**:
- [ ] Create `ComboIndicator` composable function:
  - [ ] Parameters: `comboCount: Int, modifier: Modifier = Modifier`
  - [ ] Only show if `comboCount >= 2`
  - [ ] Display: "×{comboCount} Combo!"
  - [ ] Style: Bold, medium size, accent color
  - [ ] Optional: Add subtle scale animation when combo increases
- [ ] Add below circular progress indicator:
  - [ ] Center-aligned
  - [ ] Small spacing (8.dp) from feedback text
- [ ] Test visibility based on combo state

**Estimated effort**: 30 minutes

### 3.3 Point Animation (Optional - Nice to Have)

**Checklist**:
- [ ] Create `PointsPopup` composable for "+X" animation
- [ ] Trigger when note completes
- [ ] Animate: fade in, move up, fade out
- [ ] Display points awarded (e.g., "+150")
- [ ] Position: above circular progress
- [ ] Use LaunchedEffect + animateFloatAsState
- [ ] Consider performance impact

**Estimated effort**: 45 minutes (optional, can skip for MVP)

**Phase 3 Effort**: 50 minutes (without point animation) or 1.5 hours (with animation)

---

## Phase 4: UI Updates - Completion Dialog

### 4.1 Update CompletionDialog
**File**: `app/src/main/java/com/hindustani/pitchdetector/ui/training/TrainingScreen.kt`

**Checklist**:
- [ ] Update `CompletionDialog` composable signature:
  - [ ] Add parameters: `earnedStars: Int, finalScore: Int, sessionBest: Int`
- [ ] Add star display:
  - [ ] Create Row with 3 star icons
  - [ ] Fill `earnedStars` number with filled star icon
  - [ ] Remainder with outlined star icon
  - [ ] Size: 48.dp per star
  - [ ] Color: Gold/yellow for filled, gray for outlined
  - [ ] Center horizontally above title
- [ ] Add score display:
  - [ ] Below congratulations message
  - [ ] Format: "Score: {finalScore}"
  - [ ] Style: titleLarge, bold
- [ ] Add session best display:
  - [ ] Only show if `finalScore > 0 && sessionBest > finalScore`
  - [ ] Format: "Session Best: {sessionBest}"
  - [ ] Style: bodyMedium, muted color
  - [ ] Position: below score
- [ ] Update dialog invocation in TrainingScreen to pass new parameters
- [ ] Test with different star counts (1, 2, 3)
- [ ] Test session best display logic

**Estimated effort**: 45 minutes

---

## Phase 5: String Resources

### 5.1 Add New Strings
**File**: `app/src/main/res/values/strings.xml`

**Checklist**:
- [ ] Add score-related strings:
  ```xml
  <string name="text_score_label">Score: %d</string>
  <string name="text_combo_indicator">×%d Combo!</string>
  <string name="text_final_score">Score: %d</string>
  <string name="text_session_best">Session Best: %d</string>
  ```
- [ ] Update completion dialog strings if needed
- [ ] Test with string resource preview

**Estimated effort**: 10 minutes

---

## Phase 6: Testing

### 6.1 Unit Tests for Scoring Logic
**File**: `app/src/test/java/com/hindustani/pitchdetector/viewmodel/TrainingViewModelTest.kt` (update existing)

**Checklist**:
- [ ] Test base point award:
  - [ ] `testBasePointsAwarded()` - complete note without perfect accuracy
- [ ] Test accuracy bonus:
  - [ ] `testAccuracyBonusAwarded()` - complete note with perfect accuracy
- [ ] Test combo logic:
  - [ ] `testComboIncrementsOnConsecutivePerfect()`
  - [ ] `testComboResetsOnImperfect()`
  - [ ] `testComboMultiplierCalculation()` - verify 1×, 2×, 3× bonuses
- [ ] Test star calculation:
  - [ ] `testOneStarAwarded()` - baseline completion
  - [ ] `testTwoStarsAwarded()` - 60-84% score
  - [ ] `testThreeStarsAwarded()` - 85%+ score
- [ ] Test session best tracking:
  - [ ] `testSessionBestUpdatesOnImprovement()`
  - [ ] `testSessionBestPersistsAcrossResets()`
- [ ] Use MockK for dependencies
- [ ] Use Google Truth for assertions

**Estimated effort**: 1.5 hours

### 6.2 UI Tests for Gamification Elements
**File**: `app/src/androidTest/java/com/hindustani/pitchdetector/ui/training/TrainingScreenTest.kt` (update existing)

**Checklist**:
- [ ] Test score display:
  - [ ] `testScoreDisplaysInTopBar()`
  - [ ] `testScoreUpdatesAfterNoteCompletion()`
- [ ] Test combo indicator:
  - [ ] `testComboIndicatorHiddenWhenComboZero()`
  - [ ] `testComboIndicatorShowsWhenComboTwo()`
- [ ] Test completion dialog:
  - [ ] `testStarsDisplayCorrectly()`
  - [ ] `testFinalScoreDisplays()`
  - [ ] `testSessionBestDisplaysWhenImproved()`
- [ ] Test replay scenario:
  - [ ] Complete session, note score
  - [ ] Repeat level, verify session best tracking

**Estimated effort**: 1 hour

**Total Phase 6 Effort**: 2.5 hours

---

## Phase 7: Polish & Documentation

### 7.1 Code Quality
**Checklist**:
- [ ] Run ktlint: `./gradlew ktlintFormat`
- [ ] Fix any lint warnings
- [ ] Review for code duplication
- [ ] Add KDoc comments to new functions
- [ ] Verify constants are appropriately scoped

**Estimated effort**: 30 minutes

### 7.2 Update Documentation
**Checklist**:
- [ ] Update CLAUDE.md:
  - [ ] Document gamification system
  - [ ] Add scoring algorithm explanation
  - [ ] Document star thresholds
- [ ] Update README.md:
  - [ ] Mention scoring and star system in training section
- [ ] Update TRAINING_FEATURE_PLAN.md:
  - [ ] Mark as completed
  - [ ] Add reference to gamification plan

**Estimated effort**: 30 minutes

**Total Phase 7 Effort**: 1 hour

---

## Implementation Timeline

| Phase | Task | Estimated Time | Dependencies |
|-------|------|---------------|--------------|
| Phase 1 | Data Layer Updates | 15 min | None |
| Phase 2 | ViewModel Scoring Logic | 1.5 hours | Phase 1 |
| Phase 3 | UI Updates - TrainingScreen | 50 min - 1.5 hours | Phase 1, 2 |
| Phase 4 | UI Updates - Completion Dialog | 45 min | Phase 1, 2 |
| Phase 5 | String Resources | 10 min | None |
| Phase 6 | Testing | 2.5 hours | All above |
| Phase 7 | Polish & Documentation | 1 hour | All above |

**Total Estimated Time**:
- **Without point animation**: ~7 hours
- **With point animation**: ~7.5 hours

---

## Git Commit Strategy

Recommended commit sequence:

1. **Commit 1**: "Add scoring fields to TrainingState data class"
2. **Commit 2**: "Implement scoring and combo logic in TrainingViewModel"
3. **Commit 3**: "Add star calculation and session best tracking"
4. **Commit 4**: "Add score display and combo indicator to TrainingScreen UI"
5. **Commit 5**: "Update completion dialog with stars and score display"
6. **Commit 6**: "Add unit tests for scoring and star calculation"
7. **Commit 7**: "Add UI tests for gamification elements"
8. **Commit 8**: "Update documentation for gamification system"

Or combine into fewer commits if preferred:
- Commit 1-3: Data + Logic
- Commit 4-5: UI Updates
- Commit 6-7: Testing
- Commit 8: Documentation

---

## Open Questions & Design Decisions

### UI/UX Decisions
- [ ] **Combo indicator styling**: Subtle text or more celebratory with animation?
  - Option A: Simple text with color accent
  - Option B: Animated scale/pulse effect when combo increases
  - **Recommendation**: Start with A, add B if time permits

- [ ] **Point animation**: Include "+X" popup animation when completing notes?
  - Pros: More satisfying, immediate feedback
  - Cons: Could be distracting, adds complexity
  - **Recommendation**: MVP without, add as enhancement later

- [ ] **Star icon style**: Filled/outlined or custom?
  - Use Material Icons: `Icons.Filled.Star` and `Icons.Outlined.Star`
  - Color: `Color(0xFFFFD700)` for gold, or use theme colors

### Threshold Tuning
- [ ] **Star thresholds**: Are 60% and 85% appropriate?
  - Current: 1⭐ baseline, 2⭐ at 60%, 3⭐ at 85%
  - Alternative: 2⭐ at 50%, 3⭐ at 80% (easier to achieve)
  - Alternative: 2⭐ at 70%, 3⭐ at 90% (harder to achieve)
  - **Decision needed**: User preference or test with real users

### Sound Effects
- [ ] **Audio feedback**: Add sound effects for achievements?
  - Combo milestone (e.g., 5× combo)
  - Session completion with 3 stars
  - **Recommendation**: Not for MVP, add later if desired

### Future Enhancements (Out of Scope)
- [ ] **Persistent statistics**: Store all-time best scores across sessions
  - Requires: DataStore or Room database
  - Would enable: Progress tracking over days/weeks

- [ ] **Achievements system**: Unlock badges for milestones
  - Examples: "First 3-Star", "Perfect Combo: Level 2", "100 Sessions Completed"
  - Requires: Persistent storage + achievement tracking

- [ ] **Difficulty modifiers**: Add challenge modes
  - Speed mode: Reduce hold time to 1.5 seconds
  - Accuracy mode: Tighter tolerance
  - Endurance mode: Extended note sequences

- [ ] **Leaderboards**: Compare with other users (requires backend)

- [ ] **Daily challenges**: Special note sequences or constraints
  - Requires: Date tracking, challenge generation logic

---

## Technical Notes

### Combo Logic Implementation
```kotlin
// In observePitch() during hold:
if (!isHoldingCorrectly && wasPerfectThroughout) {
    wasPerfectThroughout = false
}

// In advanceToNextNote():
val points = BASE_POINTS
if (wasPerfectThroughout) {
    comboCount++
    points += (ACCURACY_BONUS_BASE * comboCount)
} else {
    comboCount = 0
}
currentScore += points
wasPerfectThroughout = true  // Reset for next note
```

### Star Calculation Formula
```kotlin
private fun calculateStars(score: Int): Int {
    val maxScore = if (level == 1) LEVEL_1_MAX_SCORE else LEVEL_2_MAX_SCORE
    val percentage = score.toFloat() / maxScore

    return when {
        percentage >= THREE_STAR_THRESHOLD -> 3
        percentage >= TWO_STAR_THRESHOLD -> 2
        else -> 1
    }
}
```

### Session Best Logic
```kotlin
// On session complete:
if (currentScore > sessionBestScore) {
    sessionBestScore = currentScore
}

// In resetSession():
// sessionBestScore is NOT reset - persists across replays
currentScore = 0
comboCount = 0
earnedStars = 0
```

---

## Success Criteria

The gamification feature is successful if:

1. **Functional**:
   - [ ] Points are accurately calculated based on completion and accuracy
   - [ ] Combo multipliers work correctly for consecutive perfect notes
   - [ ] Stars are awarded based on correct thresholds
   - [ ] Session best tracking works across multiple replays

2. **User Experience**:
   - [ ] Score is clearly visible during practice
   - [ ] Combo indicator provides satisfying feedback
   - [ ] Completion dialog celebrates achievement with stars
   - [ ] UI doesn't feel cluttered or distracting

3. **Code Quality**:
   - [ ] All tests pass
   - [ ] No lint warnings
   - [ ] Well-documented code
   - [ ] No performance degradation

4. **Engagement** (measure after release):
   - Users replay levels to improve scores
   - Session completion rate increases
   - Average session duration increases slightly

---

## References

- Existing Training Feature Plan: `TRAINING_FEATURE_PLAN.md`
- TrainingState: `app/src/main/java/com/hindustani/pitchdetector/data/TrainingState.kt`
- TrainingViewModel: `app/src/main/java/com/hindustani/pitchdetector/viewmodel/TrainingViewModel.kt`
- TrainingScreen: `app/src/main/java/com/hindustani/pitchdetector/ui/training/TrainingScreen.kt`
