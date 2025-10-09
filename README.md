# Hindustani Pitch Detector

A real-time pitch detection Android app for Hindustani classical music students. This app helps musicians practice vocal accuracy by providing instant visual feedback on pitch, note accuracy, and deviation in cents.

## Features

### Core Functionality
- **Real-time Pitch Detection**: Uses PYIN (Probabilistic YIN) algorithm for accurate vocal pitch detection
- **Hindustani Notation**: Displays notes in traditional Hindustani swaras (S r R g G m M P d D n N)
- **Visual Feedback**: Needle-style pitch indicator with color-coded accuracy (Green = Perfect, Blue = Flat, Red = Sharp)
- **Customizable Tolerance**: Adjustable accuracy tolerance from ±5 cents (expert) to ±30 cents (beginner)
- **Dual Tuning Systems**:
  - 12-note Just Intonation (standard)
  - 22-shruti system (microtonal variations)

### User-Friendly Features
- **Flexible Sa (Tonic) Selection**: Input Sa using Western notation (C4, A#3, Bb4, etc.)
- **Built-in Tanpura/Drone**: 4-string tanpura with gapless looping for practice accompaniment
  - Configurable String 1 (P, m, M, S, N) for different ragas
  - Instant playback using pre-recorded samples with authentic tanpura timbre
  - Harmonic structure based on spectral analysis of real Calcutta-standard male tanpura
  - 75 variations covering G#2 to A#3 tonic range (15 Sa values × 5 String 1 notes)
- **Clean Interface**: Uncluttered design focused on essential controls
- **Progressive Learning**: Adjustable difficulty levels for skill development
- **Low Latency**: Optimized for real-time feedback with minimal delay

## Technical Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Audio Processing**: AudioRecord + Kotlin Coroutines
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Key Components

#### Audio Processing
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

#### Music Theory Engine
- **HindustaniNoteConverter**: Frequency to swara conversion
  - Just Intonation ratios for 12 basic notes
  - 22-shruti system support with microtonal variations
  - Accurate cents deviation calculation

- **SaParser**: Western notation to frequency conversion
  - Supports all standard Western notes (C, C#, Db, etc.)
  - Octave range support (0-9)
  - Validates input format

#### User Interface
- **MainScreen**: Primary pitch detection interface
  - Large note display
  - Needle-style pitch indicator
  - Start/Stop controls
  - Real-time confidence meter

- **SettingsScreen**: Configuration interface
  - Sa (tonic) input
  - Tolerance slider
  - Tuning system selection
  - Educational tooltips

## Building the App

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 8 or higher
- Android SDK with minimum API 24

### Build Steps

1. **Clone/Open the project**
   ```bash
   cd /path/to/kansen
   ```

2. **Sync Gradle**
   - Open project in Android Studio
   - Wait for Gradle sync to complete
   - Resolve any dependency issues

3. **Build the app**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

   Or use Android Studio's Run button (Shift+F10)

### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

## Usage Guide

### Initial Setup
1. **Grant Microphone Permission**: App will request microphone access on first launch
2. **Set Your Sa (Tonic)**: Go to Settings → Enter your Sa in Western notation (e.g., C4, D3, A#3)
3. **Adjust Tolerance**: Set tolerance based on your skill level:
   - Expert: ±5-8 cents
   - Intermediate: ±10-15 cents
   - Beginner: ±20-30 cents

### Practicing
1. **Start Detection**: Tap the green "Start" button
2. **Sing a Note**: The app will display:
   - Current swara (S, R, G, etc.)
   - Cents deviation from perfect pitch
   - Visual needle indicator
   - Color-coded status (Perfect/Flat/Sharp)
3. **Adjust Your Pitch**: Use the visual feedback to correct your pitch
4. **Stop Detection**: Tap the red "Stop" button when done

### Understanding the Display

#### Pitch Indicator
- **Needle Position**: Shows pitch deviation (-50 to +50 cents)
- **Green Zone**: Within tolerance (perfect pitch)
- **Needle Color**:
  - 🟢 Green: Perfect (within tolerance)
  - 🔵 Blue: Flat (below tolerance)
  - 🔴 Red: Sharp (above tolerance)

#### Note Display
- **Large Swara**: Current detected note in Hindustani notation with octave
  - **Mandra Saptak** (lower octave): `.S` `.R` `.G` `.P` (dot prefix)
  - **Madhya Saptak** (middle octave): `S` `R` `G` `P` (plain)
  - **Taar Saptak** (upper octave): `S'` `R'` `G'` `P'` (apostrophe suffix)
- **Cents Deviation**: Exact deviation in cents (+/-)
- **Confidence**: Algorithm confidence level (shown during recording)

**Example**:
- Sing low Sa → Shows `.S` (mandra)
- Sing middle Sa → Shows `S` (madhya)
- Sing high Sa → Shows `S'` (taar)

### Tuning Systems

#### 12-Note Just Intonation (Default)
Standard Hindustani notes with pure frequency ratios:
- S (1/1), r (16/15), R (9/8), g (6/5), G (5/4), m (4/3), M (45/32)
- P (3/2), d (8/5), D (5/3), n (16/9), N (15/8)

#### 22-Shruti System (Advanced)
Microtonal variations for advanced musicians:
- Includes intermediate shrutis between main notes
- Marked with superscript numbers (e.g., r¹, r²)
- Useful for subtle raga nuances

## Performance Optimizations

### Low Latency Audio
- Optimized buffer sizes based on device capabilities
- Async processing with Kotlin Coroutines
- Efficient pitch detection algorithm (~10-15ms latency)

### UI Optimization
- Throttled UI updates to prevent frame drops
- Hardware-accelerated Canvas drawing
- Efficient state management with StateFlow

### Battery Efficiency
- Audio capture stops when not in use
- Efficient coroutine management
- Minimal background processing

## Troubleshooting

### Common Issues

**No pitch detected:**
- Ensure microphone permission is granted
- Sing louder or closer to the microphone
- Check if device volume is not muted
- Try adjusting tolerance to higher value

**Inaccurate detection:**
- Reduce background noise
- Sing clearer, sustained notes
- Avoid vibrato for more stable detection
- Lower the tolerance for more precision

**Octave errors:**
- PYIN algorithm minimizes this, but can occur
- Sing in your comfortable vocal range (80-1000 Hz)
- Ensure clear vocal onset

**App crashes:**
- Check microphone permission
- Ensure Android version is 7.0 or higher
- Try restarting the app

## Project Structure

```
app/src/main/java/com/hindustani/pitchdetector/
├── audio/
│   ├── AudioCaptureManager.kt       # Microphone audio capture
│   ├── PYINDetector.kt              # PYIN pitch detection algorithm
│   └── TanpuraPlayer.kt             # Tanpura playback engine
├── music/
│   ├── HindustaniNoteConverter.kt   # Frequency to swara conversion
│   └── SaParser.kt                  # Western notation parser
├── ui/
│   ├── MainActivity.kt              # Main activity with navigation
│   ├── MainScreen.kt                # Primary pitch detection UI
│   ├── SettingsScreen.kt            # Settings interface
│   ├── components/
│   │   ├── PitchIndicator.kt        # Needle-style pitch meter
│   │   └── NoteDisplay.kt           # Note display component
│   └── theme/
│       ├── Theme.kt                 # Material 3 theme
│       └── Typography.kt            # Typography definitions
├── viewmodel/
│   └── PitchViewModel.kt            # State management & business logic
└── data/
    ├── UserSettings.kt              # Settings data model
    └── PitchState.kt                # Pitch state data model
```

## Music Theory Reference

### Just Intonation Ratios
| Swara | Ratio | Cents from Sa | Western Equivalent |
|-------|-------|---------------|-------------------|
| S     | 1/1   | 0             | Do (C)            |
| r     | 16/15 | 112           | Re♭ (D♭)          |
| R     | 9/8   | 204           | Re (D)            |
| g     | 6/5   | 316           | Ga♭ (E♭)          |
| G     | 5/4   | 386           | Ga (E)            |
| m     | 4/3   | 498           | Ma (F)            |
| M     | 45/32 | 590           | Ma♯ (F♯)          |
| P     | 3/2   | 702           | Pa (G)            |
| d     | 8/5   | 814           | Dha♭ (A♭)         |
| D     | 5/3   | 884           | Dha (A)           |
| n     | 16/9  | 996           | Ni♭ (B♭)          |
| N     | 15/8  | 1088          | Ni (B)            |

### Cents Measurement
- 100 cents = 1 semitone
- 1200 cents = 1 octave
- Human perception: ~5-10 cents (trained musicians)

## Future Enhancements

Potential features for future versions:
- Recording/playback of practice sessions
- Progress tracking and analytics
- Raga-specific practice modes
- Metronome integration
- Dark mode support
- Export practice logs

## License

This project is created for educational purposes as a learning tool for Hindustani classical music students.

## Credits

**Algorithm**: PYIN (Probabilistic YIN) pitch detection
**Music Theory**: Traditional Hindustani classical music system
**Development**: Android Jetpack Compose + Kotlin

## Support

For issues, questions, or feature requests, please refer to the project documentation or contact the development team.

---

**Happy Practicing! 🎵**
