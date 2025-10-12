# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Shruti** is a real-time pitch detection Android app for Hindustani classical music students. It uses the PYIN (Probabilistic YIN) algorithm to detect vocal pitch and displays notes in traditional Hindustani swaras (S, R, G, P, etc.) with visual feedback on accuracy.

**Tech Stack:**
- Language: Kotlin
- UI: Jetpack Compose with Material 3
- Architecture: MVVM (Model-View-ViewModel)
- Audio: AudioRecord + Kotlin Coroutines
- Min SDK: 24 (Android 7.0), Target SDK: 34 (Android 14)

## Build & Development Commands

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Testing
```bash
# Run all unit tests (local JVM, fast)
./gradlew test

# Run unit tests for a specific class
./gradlew test --tests "com.hindustani.pitchdetector.music.SaParserTest"

# Run Android instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific UI test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.hindustani.pitchdetector.ui.MainScreenTest
```

**Test Suite:** 69 unit tests + 26 UI tests = 95 total tests

### Running the App
```bash
# Run from command line
./gradlew installDebug

# Or use Android Studio: Shift+F10
```

**Important:** Virtual devices have limited microphone support. For proper audio testing, use a physical Android device with USB debugging enabled.

## Architecture

### Core Components

**Audio Processing Pipeline:**
1. `AudioCaptureManager` - Captures real-time microphone audio (44.1 kHz, mono, PCM 16-bit)
2. `PYINDetector` - Detects pitch using Probabilistic YIN algorithm (~10-15ms latency)
3. `HindustaniNoteConverter` - Converts frequency to Hindustani swara notation
4. `PitchViewModel` - Manages state and coordinates the pipeline

**Music Theory Engine:**
- `SaParser` - Parses Western notation (C4, A#3, Bb4) to frequency
- `HindustaniNoteConverter` - Handles:
  - 12-note Just Intonation (default)
  - 22-shruti microtonal system (advanced)
  - Cents deviation calculation
  - Perfect/flat/sharp determination

**Tanpura System:**
- `TanpuraPlayer` - Pre-recorded tanpura playback with gapless looping
- Uses MediaCodec (OGG Vorbis) + AudioTrack for streaming
- 75 pre-recorded variations (15 Sa values × 5 String 1 notes)
- Authentic harmonic structure from real tanpura recordings

**State Management:**
- `PitchViewModel` - Central state manager using StateFlow
- `UserSettings` - Persisted settings (Sa, tolerance, tuning system, tanpura config)
- `PitchState` - Real-time pitch detection state (current note, frequency, confidence)

**UI Layer (Jetpack Compose):**
- `MainActivity` - Single activity with Navigation Compose
- `MainScreen` - Primary pitch detection interface
- `SettingsScreen` - Configuration interface
- `PitchBar` - Horizontal pitch bar with emoji feedback (Canvas-based)
- `NoteDisplay` - Large swara display with cents deviation

### Key Design Patterns

**MVVM Architecture:**
- ViewModels expose StateFlow for reactive UI updates
- Business logic isolated in ViewModel layer
- UI is purely declarative (Compose)

**Audio Processing Flow:**
```
AudioCaptureManager (main thread)
  → captures audio buffer
  → callback to ViewModel
    → Dispatchers.Default (background)
      → PYINDetector.detectPitch()
      → HindustaniNoteConverter.convertFrequency()
      → Exponential Moving Average smoothing
    → Dispatchers.Main (UI thread)
      → StateFlow update
        → Compose recomposition
```

**Smoothing Strategy:**
- Exponential Moving Average (EMA) applied to cents deviation
- Alpha = 0.25 (responsive but smooth)
- Reduces needle jitter while maintaining responsiveness
- Smoothing resets on start/stop to prevent carry-over

### Important Implementation Details

**Pitch Detection:**
- Vocal range: 80-1000 Hz (validated in ViewModel)
- Confidence threshold: >0.5 (below this, no note displayed)
- Buffer size: Minimum 2048 samples for PYIN
- YIN buffer: Up to 2048 or half audio buffer size

**Just Intonation Ratios:**
The app uses pure frequency ratios (not equal temperament):
- S (1/1), r (16/15), R (9/8), g (6/5), G (5/4), m (4/3), M (45/32)
- P (3/2), d (8/5), D (5/3), n (16/9), N (15/8)

**Octave Notation:**
- Mandra saptak (lower): `.S` `.R` `.G` (dot prefix)
- Madhya saptak (middle): `S` `R` `G` (plain)
- Taar saptak (upper): `S'` `R'` `G'` (apostrophe suffix)

**Tanpura Asset Structure:**
- Pre-recorded OGG files in `app/src/main/assets/tanpura/`
- Naming: `tanpura_<note>_<string1>.ogg` (e.g., `tanpura_C3_P.ogg`)
- Coverage: G#2 to A#3 (Sa range), String 1 options: P, m, M, S, N
- Instant startup (~200-400ms) with gapless looping

## Development Conventions

**Kotlin Style:**
- Follow standard Kotlin coding conventions
- Use data classes for immutable state
- Prefer `val` over `var`
- Use named parameters for clarity

**Coroutines:**
- Audio processing: `Dispatchers.Default`
- UI updates: `Dispatchers.Main`
- Use `viewModelScope` for ViewModel coroutines
- Properly cancel jobs in `onCleared()`

**State Management:**
- Use `StateFlow` for observable state
- Update state with `.update { }` for atomic modifications
- Never mutate state directly

**Testing:**
- Unit tests: JVM-based, fast, no Android dependencies
- Use MockK for mocking
- Use Google Truth for assertions
- UI tests: Compose Testing with `composeTestRule`
- Test naming: `` `methodName_stateUnderTest_expectedBehavior` ``

**Compose UI:**
- Material 3 design system
- Stateless composables when possible
- Hoist state to ViewModels
- Use `remember` for computation caching
- Hardware acceleration for Canvas (PitchBar)

## Common Development Tasks

### Adding a New Swara/Note
1. Update `HindustaniNoteConverter.kt` - Add ratio to `noteRatios` or `shruti22Ratios`
2. Update tests in `HindustaniNoteConverterTest.kt`
3. Update documentation if needed

### Modifying Pitch Detection Parameters
1. `PYINDetector.kt` - Adjust threshold (default: 0.15)
2. `PitchViewModel.kt` - Adjust:
   - Vocal range validation (default: 80-1000 Hz)
   - Confidence threshold (default: 0.5)
   - Smoothing alpha (default: 0.25)

### Adding New Tanpura Variations
1. Generate/record OGG files with naming convention
2. Place in `app/src/main/assets/tanpura/`
3. Update `TanpuraPlayer.kt` if logic changes needed
4. Update `getTanpuraAvailableNotes()` if new String 1 options added

### Modifying UI Components
1. Composables in `app/src/main/java/com/hindustani/pitchdetector/ui/`
2. Theme modifications in `ui/theme/Theme.kt`
3. Typography in `ui/theme/Typography.kt`
4. Add UI tests in `app/src/androidTest/java/.../ui/`

## Project Structure

```
app/src/main/java/com/hindustani/pitchdetector/
├── audio/
│   ├── AudioCaptureManager.kt    # Microphone capture
│   ├── PYINDetector.kt           # Pitch detection algorithm
│   └── TanpuraPlayer.kt          # Tanpura playback
├── music/
│   ├── HindustaniNoteConverter.kt # Frequency → swara conversion
│   └── SaParser.kt                # Western notation parser
├── ui/
│   ├── MainActivity.kt            # Single activity + navigation
│   ├── MainScreen.kt              # Pitch detection UI
│   ├── SettingsScreen.kt          # Settings UI
│   ├── components/
│   │   ├── PitchBar.kt            # Horizontal pitch bar (Canvas)
│   │   └── NoteDisplay.kt         # Note display component
│   └── theme/
├── viewmodel/
│   └── PitchViewModel.kt          # State management
└── data/
    ├── UserSettings.kt            # Settings model
    ├── UserSettingsRepository.kt  # DataStore persistence
    └── PitchState.kt              # Pitch state model
```

## Testing Notes

**Audio Testing:**
- Emulators have poor/no microphone support
- Use physical device for real audio testing
- Enable USB debugging in Developer Options
- See `AUDIO_TESTING.md` for detailed troubleshooting

**Unit Test Strategy:**
- `PYINDetector` uses synthetic sine waves
- Music theory tests cover all ratios and edge cases
- ViewModel tests use mocked audio capture
- Fast execution (<5 seconds for all unit tests)

**UI Test Strategy:**
- Uses Compose Testing framework
- Tests user interactions and state changes
- Requires connected device/emulator
- Execution time: <2 minutes for all UI tests

## Dependencies

**Core:**
- Kotlin 1.9.20
- Compose BOM 2023.10.01
- Coroutines 1.7.3
- DataStore Preferences 1.0.0
- Navigation Compose 2.7.5

**Testing:**
- JUnit 4.13.2
- MockK 1.13.8
- Google Truth 1.1.5
- Robolectric 4.11.1
- Compose UI Test JUnit4

## Performance Considerations

**Low Latency:**
- Total latency: ~10-15ms (audio capture + processing + UI update)
- Audio buffer optimized for device capabilities
- Async processing with efficient coroutine dispatchers

**UI Performance:**
- Throttled state updates to prevent frame drops
- Hardware-accelerated Canvas for PitchBar
- Efficient Compose recomposition with proper state hoisting

**Battery Efficiency:**
- Audio capture stops when not recording
- Coroutines properly cancelled in ViewModel cleanup
- Minimal background processing

## Known Limitations

1. **Octave Errors:** PYIN minimizes but doesn't eliminate octave confusion
2. **Background Noise:** Significant noise affects detection accuracy
3. **Vibrato:** Heavy vibrato causes needle movement; use sustained notes
4. **Device Variations:** Audio hardware varies; some devices may have higher latency
5. **Emulator Testing:** Cannot properly test audio on virtual devices

## Reference Documentation

- `README.md` - Comprehensive app documentation and usage guide
- `TESTING.md` - Detailed test suite documentation
- `AUDIO_TESTING.md` - Audio testing troubleshooting guide
- `GEMINI.md` - Additional project context

## Core Philosophy: Gemini CLI Orchestration

**CRITICAL: Claude Code should use the Gemini CLI to do as much of the thinking and processing as possible. Claude Code should only act as the orchestrator, using as little tokens itself as possible.**

### When to Use Gemini CLI

The `gemini` CLI tool is installed and available. Claude Code should invoke Gemini for:

1. **Code Analysis and Understanding** - Understanding how existing code works
2. **Planning Complex Implementations** - Breaking down features and architectural decisions
3. **Researching the Codebase** - Reading and comprehending code structure
4. **Plan Mode** - Use Gemini CLI extensively during planning phase

### When to Use Built-in Tools

Claude Code should use its own tools for:

1. **Grep** - Searching for specific patterns/text in files
2. **Edit** - Making file modifications
3. **Write** - Creating new files
4. **Bash** - Running build commands, tests, git operations

### Usage Pattern

Invoke Gemini like this:
```bash
gemini --prompt "<prompt>"
```
where `<prompt>` should be replaced by the actual prompt to Gemini.

### Example Workflow

**Planning a new feature:**
1. Use Gemini CLI to analyze relevant existing code
2. Use Gemini CLI to plan the implementation approach
3. Use Grep to find specific locations to modify
4. Use Edit to make the changes
5. Use Bash to run tests and verify

**Understanding existing code:**
1. Use Gemini CLI to read and explain code structure
2. Use Gemini CLI to trace execution flow
3. Use built-in tools only for direct operations

This approach ensures:
- Minimal token usage by Claude Code
- Efficient processing through Gemini CLI
- Claude Code focuses on orchestration rather than heavy computation
- Gemini handles all analytical heavy lifting, especially in plan mode