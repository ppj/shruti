# Technical Architecture

This document details the technical architecture and key components of the Shruti app.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Audio Processing**: AudioRecord + Kotlin Coroutines
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Key Components

### Audio Processing
- **PYINDetector**: Probabilistic YIN algorithm implementation
  - Better noise robustness than standard YIN
  - Provides confidence scores for reliable detection
  - Reduces octave errors
  - Optimized for vocal input

- **AudioCaptureManager**: Real-time microphone audio capture
  - 44.1 kHz sample rate
  - Mono channel PCM 16-bit
  - Optimized buffer sizes for low latency

- **TanpuraPlayer**: Pre-recorded tanpura playback engine
  - MediaCodec-based OGG Vorbis decoder
  - AudioTrack streaming for gapless looping
  - 4-string tanpura with authentic jawari effect
  - Synthesized using additive synthesis with harmonics extracted from real recordings
  - Instant startup (~200-400ms)
  - Source: [Calcutta Standard Male Tanpura](https://www.india-instruments.com/tanpura-details/calcutta-standard-male-tanpura.html)

### Music Theory Engine
- **HindustaniNoteConverter**: Frequency to swar conversion
  - Just Intonation ratios for 12 basic notes
  - 22-shruti system support with microtonal variations
  - Accurate cents deviation calculation

- **SaParser**: Western notation to frequency conversion
  - Supports all standard Western notes (C, C#, Db, etc.)
  - Octave range support (0-9)
  - Validates input format

### User Interface
- **MainScreen**: Primary pitch detection interface
  - Large note display
  - Needle-style pitch indicator
  - Start/Stop controls
  - Real-time confidence meter

- **SettingsScreen**: Configuration interface
  - Sa (tonic) input
  - Tolerance slider
  - Tuning system selection
  - Tanpura volume control
  - Educational tooltips

## Project Structure

```
app/src/main/java/com/hindustani/pitchdetector/
├── audio/
│   ├── AudioCaptureManager.kt       # Microphone audio capture
│   ├── PYINDetector.kt              # PYIN pitch detection algorithm
│   └── TanpuraPlayer.kt             # Tanpura playback engine
├── music/
│   ├── HindustaniNoteConverter.kt   # Frequency to swar conversion
│   └── SaParser.kt                  # Western notation parser
├── ui/
│   ├── MainActivity.kt              # Main activity with navigation
│   ├── MainScreen.kt                # Primary pitch detection UI
│   ├── SettingsScreen.kt            # Settings interface
│   ├── components/
│   │   ├── PitchBar.kt              # Horizontal pitch bar
│   │   └── NoteDisplay.kt           # Note display component
│   └── theme/
│       ├── Theme.kt                 # Material 3 theme
│       └── Typography.kt            # Typography definitions
├── viewmodel/
│   └── PitchViewModel.kt            # State management & business logic
└── data/
    ├── PitchState.kt                # Pitch state data model
    ├── TrainingState.kt             # Training state data model
    ├── UserSettings.kt              # Settings data model
    └── UserSettingsRepository.kt    # Repository for user settings
```
