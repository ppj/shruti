# Comprehensive Cleanup Checklist

This document contains cleanup tasks identified through code analysis and Android Lint. These will be addressed in a future PR.

**Analysis Date:** 2025-10-17 (Updated with comprehensive lint analysis)
**Branch Analyzed:** `cleanup/comprehensive-cleanup` vs `main`
**Files Analyzed:** 30 Kotlin files + all resources + manifest + build files

---

## Priority 1: Critical Issues (Must Fix)

### 🔴 CRITICAL: Missing Permission Check
- [ ] **AudioCaptureManager.kt:43** - Add permission check before creating AudioRecord
  - Current: Directly instantiates AudioRecord without permission check
  - Risk: App will crash if permission denied
  - Solution: Wrap in permission check or add `@SuppressLint("MissingPermission")` if permission is guaranteed by caller

---

## Priority 2: Error Handling & Logging

### Improve Error Handling
- [ ] **AudioCaptureManager.kt:61** - Replace `e.printStackTrace()` with `Log.e(TAG, "message", e)`
- [ ] **AudioCaptureManager.kt:75** - Replace `e.printStackTrace()` with `Log.e(TAG, "message", e)`
  - Add: `private const val TAG = "AudioCaptureManager"`

### Refactor Complex Business Logic
- [ ] **FindSaViewModel.kt** - Refactor pitch analysis algorithm into separate class
  - Current: ViewModel contains complex business logic for analyzing and calculating Sa recommendation
  - Issue: Makes ViewModel large and harder to test
  - Solution: Extract pitch analysis logic (`analyzePitches`, `combineRecommendations`, etc.) into separate `FindSaAlgorithm.kt` class
  - Benefit: Improves separation of concerns and allows for more focused unit testing

### Extract Duplicated Code
- [ ] **FindSaViewModel.kt** - Extract duplicated outlier removal logic
  - Issue: Outlier removal logic is duplicated in `analyzeSpeakingOnly`, `analyzeSingingOnly`, and `analyzeBothMethods`
  - Solution: Create single private helper function `private fun removeOutliers(pitches: List<Float>, percentage: Double): List<Float>`
  - Call from all three methods to reduce code duplication

---

## Priority 3: Code Quality - Remove Redundant Comments

### Comments That Restate Code
- [ ] **MainActivity.kt:40** - Remove "Check permission status" (self-explanatory)
- [ ] **MainScreen.kt:36** - Remove "Dropdown state for tanpura string 1 selector"
- [ ] **PitchViewModel.kt:40** - Remove "Smoothing for needle movement"

### Test Comments
- [ ] **FindSaScreenTest.kt:40** - Remove "Should display title"
- [ ] **FindSaScreenTest.kt:43** - Remove redundant assertion comments
- [ ] **FindSaScreenTest.kt:46** - Remove redundant assertion comments

---

## Priority 4: Extract Magic Numbers to Constants

### AudioCaptureManager.kt
- [ ] **Line 23** - Extract `4096` to `private const val FALLBACK_BUFFER_SIZE = 4096`

### PYINDetector.kt
- [ ] **Line 12** - Extract `0.15f` to `private const val DEFAULT_YIN_THRESHOLD = 0.15f`
- [ ] **Lines 19, 26** - Extract `2048` to `private const val YIN_BUFFER_SIZE = 2048`
- [ ] **Line 118** - Extract `0.7f` to `private const val VALUE_CONFIDENCE_WEIGHT = 0.7f`
- [ ] **Line 134** - Extract `0.3f` to `private const val SEPARATION_CONFIDENCE_WEIGHT = 0.3f`

### TanpuraPlayer.kt
- [ ] **Line 120** - Extract `8192` to `private const val AUDIO_TRACK_BUFFER_SIZE = 8192`
- [ ] **Lines 200, 209** - Extract `10000` to `private const val DECODE_TIMEOUT_US = 10000L`

### PitchViewModel.kt
- [ ] **Line 41** - Move `smoothingAlpha = 0.25` to companion object as `private const val CENTS_DEVIATION_SMOOTHING_ALPHA = 0.25`
- [ ] **Line 128** - Extract `0.5f` to `private const val PITCH_CONFIDENCE_THRESHOLD = 0.5f`
  - Used to decide if detected pitch is reliable enough to process

### PianoKeyboardSelector.kt
- [ ] Extract hardcoded layout values to constants
  - Issue: Contains numerous hardcoded layout values (e.g., `90.dp`, `54.dp`, `0.5.dp`) and colors (e.g., `Color(0xFF2C2C2C)` for black keys)
  - Solution: Move these values into companion object with descriptive constant names or into `Theme.kt`
  - Benefit: Makes keyboard easier to maintain and style

---

## Priority 5: Extract Magic Strings to Constants

### Navigation Routes
- [ ] **MainActivity.kt** - Create navigation constants object
  ```kotlin
  object AppRoutes {
      const val MAIN = "main"
      const val SETTINGS = "settings"
      const val FIND_SA = "findSa"
  }
  ```
- [ ] **MainActivity.kt:70** - Replace `"main"` with `AppRoutes.MAIN`
- [ ] **MainActivity.kt:76** - Replace `"settings"` with `AppRoutes.SETTINGS`
- [ ] **MainActivity.kt:85** - Replace `"findSa"` with `AppRoutes.FIND_SA`

---

## Priority 6: Refactoring Opportunities

### Self-Documenting Code
- [ ] **PitchViewModel.kt:140** - Extract smoothing logic to `private fun createSmoothedNote(...): HindustaniNoteConverter.HindustaniNote`
  - Replaces comment: "Recalculate isPerfect/isFlat/isSharp based on smoothed value"
  - Makes code more testable and readable

### State Machine Review
- [ ] **FindSaViewModel.kt:211** - Review state machine logic
  - Comment says "This shouldn't happen, but handle it gracefully"
  - Consider redesigning state machine to make this case impossible
  - If truly unreachable, remove defensive code and comment

---

## Priority 7: User-Facing Content

### Review Text
- [ ] **MainScreen.kt:220** - Review tanpura help text
  - Contains: "• M: Very rare (to be removed soon)"
  - Contains: "• S: Very rare (to be removed soon)"
  - Decision: Either remove these options or remove "(to be removed soon)" text

---

## Priority 8: Android Lint - Build Configuration

### Update SDK Versions
- [ ] **build.gradle.kts** - Update `compileSdk = 34` to `36`
- [ ] **build.gradle.kts** - Update `targetSdk = 34` to `36`

### Update AndroidX Dependencies
- [ ] `androidx.core:core-ktx` - Update `1.12.0` → `1.17.0`
- [ ] `androidx.lifecycle:lifecycle-runtime-ktx` - Update `2.6.2` → `2.9.4`
- [ ] `androidx.lifecycle:lifecycle-viewmodel-compose` - Update `2.6.2` → `2.9.4`
- [ ] `androidx.activity:activity-compose` - Update `1.8.1` → `1.11.0`
- [ ] `androidx.datastore:datastore-preferences` - Update `1.0.0` → `1.1.7`
- [ ] `androidx.navigation:navigation-compose` - Update `2.7.5` → `2.9.5`

### Update Compose BOM
- [ ] `androidx.compose:compose-bom` - Update `2023.10.01` → `2025.10.00` (2 instances)

### Update Test Dependencies
- [ ] `org.jetbrains.kotlinx:kotlinx-coroutines-android` - Update `1.7.3` → `1.10.2`
- [ ] `org.jetbrains.kotlinx:kotlinx-coroutines-test` - Update `1.7.3` → `1.10.2`
- [ ] `io.mockk:mockk` - Update `1.13.8` → `1.14.6`
- [ ] `io.mockk:mockk-android` - Update `1.13.8` → `1.14.6`
- [ ] `com.google.truth:truth` - Update `1.1.5` → `1.4.5`
- [ ] `org.robolectric:robolectric` - Update `4.11.1` → `4.16`
- [ ] `androidx.test:core` - Update `1.5.0` → `1.7.0`
- [ ] `androidx.test:core-ktx` - Update `1.5.0` → `1.7.0`
- [ ] `androidx.test.ext:junit` - Update `1.1.5` → `1.3.0`
- [ ] `androidx.test.espresso:espresso-core` - Update `3.5.1` → `3.7.0`

---

## Priority 9: Android Lint - Resources

### Remove Unused String Resources (18 total)
- [ ] **res/values/strings.xml:4** - Remove `start` string
- [ ] **res/values/strings.xml:5** - Remove `stop` string
- [ ] **res/values/strings.xml:6** - Remove `settings` string
- [ ] **res/values/strings.xml:7** - Remove `sa_label` string
- [ ] **res/values/strings.xml:8** - Remove `tolerance` string (or convert to plural first, see below)
- [ ] **res/values/strings.xml:9** - Remove `beginner` string
- [ ] **res/values/strings.xml:10** - Remove `expert` string
- [ ] **res/values/strings.xml:11** - Remove `tuning_system` string
- [ ] **res/values/strings.xml:12** - Remove `just_intonation` string
- [ ] **res/values/strings.xml:13** - Remove `shruti_22` string
- [ ] **res/values/strings.xml:14** - Remove `cents` string (or convert to plural first, see below)
- [ ] **res/values/strings.xml:15** - Remove `perfect` string
- [ ] **res/values/strings.xml:16** - Remove `flat` string
- [ ] **res/values/strings.xml:17** - Remove `sharp` string
- [ ] **res/values/strings.xml:18** - Remove `no_pitch` string
- [ ] **res/values/strings.xml:19** - Remove `permission_required` string
- [ ] **res/values/strings.xml:20** - Remove `grant_permission` string
  - Note: Codebase uses hardcoded literals instead of these resources
  - Decision: Either start using string resources or remove them

### Remove Unused Drawable/Color Resources
- [ ] **res/values/ic_launcher_colors.xml:3** - Remove or use `ic_launcher_background` color
- [ ] **res/drawable/ic_launcher_foreground.xml** - Remove or use `ic_launcher_foreground` drawable
  - Note: These may be needed for adaptive icons (see Icon Issues below)

### Fix String Pluralization
- [ ] **res/values/strings.xml:8** - Convert "Tolerance: ±%d cents" to plural resource
  ```xml
  <plurals name="tolerance">
      <item quantity="one">Tolerance: ±%d cent</item>
      <item quantity="other">Tolerance: ±%d cents</item>
  </plurals>
  ```
- [ ] **res/values/strings.xml:14** - Convert "%d cents" to plural resource
  ```xml
  <plurals name="cents">
      <item quantity="one">%d cent</item>
      <item quantity="other">%d cents</item>
  </plurals>
  ```

### Fix Icon Issues
- [ ] **App launcher icons** - Fix icon shape and duplicate issues
  - Issue: Lint reports icon shape violations and duplicate icons
  - Problems:
    1. Icons fill every pixel of their square region (against design guidelines)
    2. Round icons not actually circular
    3. `ic_launcher.png` and `ic_launcher_round.png` are identical in all density folders
  - Solution: Implement proper adaptive icons
    1. Create `res/mipmap-anydpi-v26/ic_launcher.xml` with foreground/background layers
    2. Create `res/mipmap-anydpi-v26/ic_launcher_round.xml` with foreground/background layers
    3. Reference `@drawable/ic_launcher_foreground` and `@color/ic_launcher_background`
    4. Remove duplicate `ic_launcher.png` and `ic_launcher_round.png` from individual density folders after verification
  - Benefit: Proper adaptive icon support for Android 8.0+ devices

---

## Priority 10: Android Lint - Manifest

### Screen Orientation
- [ ] **AndroidManifest.xml:17** - Review `android:screenOrientation="portrait"` setting
  - Lint warnings:
    1. Chrome OS compatibility - users can't rotate to landscape
    2. Android 16+ will ignore fixed orientations
  - Decision: Keep portrait-only or support multiple orientations?
  - If keeping portrait: Add suppression with justification
  - If supporting rotation: Change to `"unspecified"` or `"fullSensor"`

---

## Priority 11: Test Coverage

### Add Test TODOs
- [ ] **FindSaScreenTest.kt:160** - Add TODO comment for audio mocking
  ```kotlin
  // TODO: Inject mock audio data to test results view with actual pitch detection data
  ```

---

## Priority 12: Native Library Alignment

### 16KB Alignment Issue
- [ ] Investigate mockk-agent-android native library alignment issue
  - Current: `libmockkjvmtiagent.so` is 4KB aligned
  - Required: 16KB alignment for future Android devices
  - Options:
    1. Wait for mockk library update to 16KB-aligned version
    2. Contact mockk maintainers about timeline
    3. Consider alternative mocking library if critical
  - Note: This is a test-only dependency, lower priority

---

## Verification Steps

After completing cleanup tasks:

1. **Run ktlint**
   ```bash
   ./gradlew ktlintCheck
   ```

2. **Run all tests**
   ```bash
   ./gradlew test connectedAndroidTest
   ```

3. **Run Android Lint**
   ```bash
   ./gradlew lint
   ```

4. **Build release variant**
   ```bash
   ./gradlew assembleRelease
   ```

5. **Manual testing on device**
   - Test all features still work
   - Test on different Android versions if possible
   - Test rotation behavior if orientation changes made

---

## Notes

- **Estimated effort**: 4-6 hours for comprehensive cleanup
- **Breaking changes**: Dependency updates may introduce API changes
- **Testing**: Thorough testing required after dependency updates
- **Priority order**: Can tackle in stages across multiple PRs if needed

---

## Recommended PR Strategy

### Option A: Single Comprehensive PR
- All 90+ items in one PR
- Pros: Complete cleanup in one go
- Cons: Very large PR, harder to review

### Option B: Multiple Focused PRs
1. **PR 1: Critical fixes** (Priority 1-2)
2. **PR 2: Code quality** (Priority 3-7)
3. **PR 3: Dependency updates** (Priority 8)
4. **PR 4: Resources & manifest** (Priority 9-10)

### Option C: As Time Permits
- Tackle items opportunistically while working on related code
- Add to cleanup backlog
