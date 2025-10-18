# Comprehensive Cleanup Checklist

This document contains cleanup tasks identified through code analysis and Android Lint. These will be addressed in a future PR.

**Analysis Date:** 2025-10-17 (Updated with comprehensive lint + DRY analysis)
**Branch Analyzed:** `cleanup/comprehensive-cleanup` vs `main`
**Files Analyzed:** 30 Kotlin files + all resources + manifest + build files
**Analysis Types:** Android Lint, Code Quality Review, DRY Violations, Single Source of Truth

---

## Priority 1: Critical Issues (Must Fix)

### 🔴 CRITICAL: Missing Permission Check
- [x] **AudioCaptureManager.kt:43** - Add permission check before creating AudioRecord
  - Current: Directly instantiates AudioRecord without permission check
  - Risk: App will crash if permission denied
  - Solution: Wrap in permission check or add `@SuppressLint("MissingPermission")` if permission is guaranteed by caller

---

## Priority 1.5: Single Source of Truth - DRY Violations (CRITICAL)

### 🔴 CRITICAL: Consolidate Pitch Analysis Constants
- [x] **FindSaViewModel.kt & FindSaScreen.kt** - Unify duplicated algorithm constants
  - Duplicated locations:
    - `FindSaViewModel.kt:40-59` - Defines algorithm constants (MIN_SPEECH_SAMPLES, MIN_SINGING_SAMPLES, etc.)
    - `FindSaScreen.kt:21-22` - Redefines MIN_SPEECH_SAMPLES and MIN_SINGING_SAMPLES
  - Risk: Algorithm parameters can drift out of sync causing bugs and making tuning difficult
  - Solution: Consolidate all algorithm constants in FindSaViewModel companion object, remove duplicates from FindSaScreen
  - Priority: **CRITICAL** (affects algorithm correctness)

### 🟠 HIGH: Unify Default User Settings
- [x] **UserSettings.kt & UserSettingsRepository.kt** - Create single source of truth for defaults
  - Duplicated locations:
    - `UserSettings.kt:6-12` - Data class constructor default arguments
    - `UserSettingsRepository.kt:31-37` - Null-coalescing operators with duplicate default values
  - Risk: Defaults can diverge, causing inconsistent app behavior between fresh installs and migrations
  - Solution: Create `companion object` in UserSettings with named constants for all defaults, reference these in repository
  - Example:
    ```kotlin
    companion object {
        const val DEFAULT_SA = "C4"
        const val DEFAULT_TOLERANCE = 20
        // ... etc
    }
    ```

### 🟠 HIGH: Centralize Sa Note/Frequency Mapping
- [x] **Create SaNotes.kt** - Single source of truth for Sa notes and frequencies
  - Currently duplicated in 3 files:
    - `TanpuraPlayer.kt:23-43` - SA_FREQUENCY_MAP
    - `SaParser.kt:47-66` - getSaOptionsInRange logic
    - `FindSaViewModel.kt:98-117` - standardSaNotes list
  - Risk: Adding/modifying Sa range requires changes in 3 separate files, error-prone
  - Solution: Create new file `com.hindustani.pitchdetector.music.SaNotes.kt` with canonical mapping
  - All three classes should reference this single source
  - Refactoring complexity: Medium

### 🟡 MEDIUM: Eliminate Test Boilerplate Duplication
- [x] **UI Test Files** - Create shared test utilities for ViewModel creation
  - Duplicated in:
    - `MainScreenTest.kt:21-24`
    - `SettingsScreenTest.kt:19-22`
    - `FindSaScreenTest.kt:21-24`
  - Issue: Identical ViewModel creation boilerplate copied 3 times
  - Solution: Create `TestViewModelFactory.kt` in androidTest or base test class with helper methods
  - Refactoring complexity: Low

### 🟢 LOW: Create Reusable UI Components
- [ ] **SettingsScreen.kt** - Extract repeated UI patterns into reusable Composables
  - Pattern 1: Settings section headers (duplicated 3 times)
    - Lines 49-59 (Tolerance), 90-103 (Tuning System), 134-142 (Tanpura Volume)
    - Solution: Create `SettingsSectionHeader(title: String, tooltipText: String)` composable
  - Pattern 2: Labeled sliders (duplicated 2 times)
    - Lines 61-79 (Tolerance slider), 144-162 (Volume slider)
    - Solution: Create `LabelledSlider(value, range, labels, onValueChange)` composable
  - Refactoring complexity: Medium

---

## Priority 2: Error Handling & Logging

### Improve Error Handling
- [x] **AudioCaptureManager.kt:61** - Replace `e.printStackTrace()` with `Log.e(TAG, "message", e)`
- [x] **AudioCaptureManager.kt:75** - Replace `e.printStackTrace()` with `Log.e(TAG, "message", e)`
  - Add: `private const val TAG = "AudioCaptureManager"`

### Refactor Complex Business Logic
- [ ] **FindSaViewModel.kt** - Refactor pitch analysis algorithm into separate class
  - Current: ViewModel contains complex business logic for analyzing and calculating Sa recommendation
  - Issue: Makes ViewModel large and harder to test
  - Solution: Extract pitch analysis logic (`analyzePitches`, `combineRecommendations`, etc.) into separate `FindSaAlgorithm.kt` class
  - Benefit: Improves separation of concerns and allows for more focused unit testing

### Extract Duplicated Code
- [x] **FindSaViewModel.kt** - Extract duplicated outlier removal logic
  - Issue: Outlier removal logic is duplicated in `analyzeSpeakingOnly`, `analyzeSingingOnly`, and `analyzeBothMethods`
  - Solution: Create single private helper function `private fun removeOutliers(pitches: List<Float>, percentage: Double): List<Float>`
  - Call from all three methods to reduce code duplication

---

## Priority 3: Code Quality - Remove Redundant Comments

### Comments That Restate Code
- [x] **MainActivity.kt:40** - Remove "Check permission status" (self-explanatory)
- [x] **MainScreen.kt:36** - Remove "Dropdown state for tanpura string 1 selector"
- [x] **PitchViewModel.kt:40** - Remove "Smoothing for needle movement"

### Test Comments
- [x] **FindSaScreenTest.kt:40** - Remove "Should display title"
- [x] **FindSaScreenTest.kt:43** - Remove redundant assertion comments
- [x] **FindSaScreenTest.kt:46** - Remove redundant assertion comments

---

## Priority 4: Extract Magic Numbers to Constants

### AudioCaptureManager.kt
- [x] **Line 23** - Extract `4096` to `private const val FALLBACK_BUFFER_SIZE = 4096`

### PYINDetector.kt
- [x] **Line 12** - Extract `0.15f` to `private const val DEFAULT_YIN_THRESHOLD = 0.15f`
- [x] **Lines 19, 26** - Extract `2048` to `private const val YIN_BUFFER_SIZE = 2048`
- [x] **Line 118** - Extract `0.7f` to `private const val VALUE_CONFIDENCE_WEIGHT = 0.7f`
- [x] **Line 134** - Extract `0.3f` to `private const val SEPARATION_CONFIDENCE_WEIGHT = 0.3f`

### TanpuraPlayer.kt
- [x] **Line 120** - Extract `8192` to `private const val AUDIO_TRACK_BUFFER_SIZE = 8192`
- [x] **Lines 200, 209** - Extract `10000` to `private const val DECODE_TIMEOUT_US = 10000L`

### PitchViewModel.kt
- [x] **Line 41** - Move `smoothingAlpha = 0.25` to companion object as `private const val CENTS_DEVIATION_SMOOTHING_ALPHA = 0.25`
- [x] **Line 128** - Extract `0.5f` to `private const val PITCH_CONFIDENCE_THRESHOLD = 0.5f`
  - Used to decide if detected pitch is reliable enough to process

### PianoKeyboardSelector.kt
- [x] Extract hardcoded layout values to constants
  - Issue: Contains numerous hardcoded layout values (e.g., `90.dp`, `54.dp`, `0.5.dp`) and colors (e.g., `Color(0xFF2C2C2C)` for black keys)
  - Solution: Move these values into companion object with descriptive constant names or into `Theme.kt`
  - Benefit: Makes keyboard easier to maintain and style

---

## Priority 5: Extract Magic Strings to Constants

### Navigation Routes
- [x] **MainActivity.kt** - Create navigation constants object
  ```kotlin
  object AppRoutes {
      const val MAIN = "main"
      const val SETTINGS = "settings"
      const val FIND_SA = "findSa"
  }
  ```
- [x] **MainActivity.kt:70** - Replace `"main"` with `AppRoutes.MAIN`
- [x] **MainActivity.kt:76** - Replace `"settings"` with `AppRoutes.SETTINGS`
- [x] **MainActivity.kt:85** - Replace `"findSa"` with `AppRoutes.FIND_SA`

---

## Priority 6: Refactoring Opportunities

### Self-Documenting Code
- [x] **PitchViewModel.kt:140** - Extract smoothing logic to `private fun createSmoothedNote(...): HindustaniNoteConverter.HindustaniNote`
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
- [x] **build.gradle.kts** - Update `compileSdk = 34` to `36`
- [x] **build.gradle.kts** - Update `targetSdk = 34` to `36`

### Update Kotlin
- [x] **build.gradle.kts** - Update Kotlin `1.9.20` → `2.1.0` (required for dependency compatibility)
- [x] **build.gradle.kts** - Add `org.jetbrains.kotlin.plugin.compose` plugin (required for Kotlin 2.0+)

### Update AndroidX Dependencies
- [x] `androidx.core:core-ktx` - Update `1.12.0` → `1.17.0`
- [x] `androidx.lifecycle:lifecycle-runtime-ktx` - Update `2.6.2` → `2.9.4`
- [x] `androidx.lifecycle:lifecycle-viewmodel-compose` - Update `2.6.2` → `2.9.4`
- [x] `androidx.activity:activity-compose` - Update `1.8.1` → `1.11.0`
- [x] `androidx.datastore:datastore-preferences` - Update `1.0.0` → `1.1.7`
- [x] `androidx.navigation:navigation-compose` - Update `2.7.5` → `2.9.5`

### Update Compose BOM
- [x] `androidx.compose:compose-bom` - Update `2023.10.01` → `2025.10.00` (2 instances)

### Update Test Dependencies
- [x] `org.jetbrains.kotlinx:kotlinx-coroutines-android` - Update `1.7.3` → `1.10.2`
- [x] `org.jetbrains.kotlinx:kotlinx-coroutines-test` - Update `1.7.3` → `1.10.2`
- [x] `io.mockk:mockk` - Update `1.13.8` → `1.14.6`
- [x] `io.mockk:mockk-android` - Update `1.13.8` → `1.14.6`
- [x] `com.google.truth:truth` - Update `1.1.5` → `1.4.5`
- [x] `org.robolectric:robolectric` - Update `4.11.1` → `4.16`
- [x] `androidx.test:core` - Update `1.5.0` → `1.7.0`
- [x] `androidx.test:core-ktx` - Update `1.5.0` → `1.7.0`
- [x] `androidx.test.ext:junit` - Update `1.1.5` → `1.3.0`
- [x] `androidx.test.espresso:espresso-core` - Update `3.5.1` → `3.7.0`

### Fix Deprecation Warnings
- [x] **SettingsScreen.kt & FindSaScreen.kt** - Replace `Icons.Filled.ArrowBack` with `Icons.AutoMirrored.Filled.ArrowBack`
- [x] **FindSaScreen.kt** - Replace `LinearProgressIndicator(progress: Float)` with lambda overload

---

## Priority 9: Android Lint - Resources

### ~~Remove Unused String Resources~~ → Properly Implement String Resources
- [x] **String resource migration completed**
  - **Decision**: Instead of removing unused strings, properly implemented string resources throughout the app for better i18n and maintainability
  - **Implementation**: Complete migration in 9 incremental commits
    - Commit 1: Created comprehensive strings.xml with 72+ string resources covering all UI text
    - Commits 2-8: Systematically replaced all hardcoded strings in MainScreen, SettingsScreen, and FindSaScreen
    - All strings now use proper format strings with placeholders (%s, %d, %%)
    - Organized by screen and purpose with clear comments
  - **Files modified**:
    - `app/src/main/res/values/strings.xml` - Comprehensive resource file created
    - `app/src/main/java/com/hindustani/pitchdetector/ui/MainScreen.kt` - All strings migrated
    - `app/src/main/java/com/hindustani/pitchdetector/ui/SettingsScreen.kt` - All strings migrated
    - `app/src/main/java/com/hindustani/pitchdetector/ui/findsa/FindSaScreen.kt` - All strings migrated
  - **Benefits**:
    - Single source of truth for all user-facing text
    - Easy to add localization support in the future
    - Better maintainability and consistency
    - Follows Android best practices
    - Properly escaped XML entities and format strings

### Remove Unused Drawable/Color Resources
- [ ] **res/values/ic_launcher_colors.xml:3** - Remove or use `ic_launcher_background` color
- [ ] **res/drawable/ic_launcher_foreground.xml** - Remove or use `ic_launcher_foreground` drawable
  - Note: These may be needed for adaptive icons (see Icon Issues below)

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
- [x] **FindSaScreenTest.kt:183** - Add TODO comment for audio mocking
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

- **Total items**: 95+ cleanup tasks identified
- **Estimated effort**: 6-8 hours for comprehensive cleanup
- **Critical items**: 4 (Permission check + 3 DRY violations affecting algorithm correctness)
- **Breaking changes**: Dependency updates may introduce API changes
- **Testing**: Thorough testing required after dependency updates and refactoring
- **Priority order**: Can tackle in stages across multiple PRs if needed
- **DRY focus**: Priority 1.5 addresses single source of truth violations that could cause bugs

---

## Recommended PR Strategy

### Option A: Single Comprehensive PR
- All 95+ items in one PR
- Pros: Complete cleanup in one go
- Cons: Very large PR, harder to review, high risk

### Option B: Multiple Focused PRs (RECOMMENDED)
1. **PR 1: Critical fixes** (Priority 1 + Priority 1.5 critical/high items)
   - Missing permission check
   - Critical DRY violations (pitch analysis constants, user settings defaults, Sa note mapping)
2. **PR 2: Code quality & refactoring** (Priority 1.5 medium/low + Priority 2-7)
   - Test utilities, UI components
   - Error handling, business logic refactoring
   - Remove redundant comments, extract magic numbers
3. **PR 3: Dependency updates** (Priority 8)
   - SDK versions
   - AndroidX dependencies
   - Test dependencies
4. **PR 4: Resources & manifest** (Priority 9-10)
   - Remove unused resources
   - Fix string pluralization
   - Icon improvements
   - Screen orientation review

### Option C: As Time Permits
- Tackle items opportunistically while working on related code
- Add to cleanup backlog
