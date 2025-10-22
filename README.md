# Shruti

A real-time pitch detection Android app for Hindustani classical music students. Shruti helps musicians practice vocal accuracy by providing instant visual feedback on pitch, note accuracy, and deviation in cents.

## Key Features

*   **Real-time Pitch Detection**: Accurate vocal pitch detection using the PYIN algorithm.
*   **Hindustani Notation**: Displays notes as traditional Hindustani swars.
*   **Visual Feedback**: Needle-style pitch indicator with color-coded accuracy.
*   **Customizable Tolerance**: Adjustable accuracy tolerance.
*   **Dual Tuning Systems**: Supports 12-note Just Intonation and 22-shruti systems.
*   **Flexible Sa (Tonic) Selection**: Input Sa using Western notation or use "Find Your Sa" for automatic detection.
*   **Built-in Tanpura/Drone**: 4-string tanpura with gapless looping and configurable String 1 (P, m, N) and volume.
*   **Voice Training Mode**: Structured practice with a 4-level progression system.

## Technologies Used

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Audio Processing**: AudioRecord + Kotlin Coroutines
*   **Pitch Detection**: PYIN (Probabilistic YIN) algorithm

## Building the App

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11 or higher
- Android SDK with minimum API 24

### Build Steps

1. **Clone/Open the project**
   ```bash
   cd /path/to/shruti
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

### Running Tests

*   **Unit Tests:**
    ```bash
    ./gradlew testDebugUnitTest
    ```
*   **Android Instrumentation Tests:**
    ```bash
    ./gradlew connectedDebugAndroidTest
    ```

### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

## Development Setup

After cloning the repository, run the setup script to configure the git hooks:

```bash
./setup.sh
```

## Development Conventions

*   **Coding Style:** The project follows the standard Kotlin coding conventions.
*   **State Management:** The app uses `StateFlow` to manage and observe UI state.
*   **UI:** The UI is built entirely with Jetpack Compose, following Material 3 design guidelines.
*   **Asynchronous Operations:** Kotlin Coroutines are used for handling asynchronous tasks like audio processing.
*   **Branching:** GitFlow

## Further Documentation

For more detailed information, please refer to the documentation in the `docs/` directory:

*   [**Detailed Features**](docs/FEATURES.md)
*   [**Technical Architecture**](docs/ARCHITECTURE.md)
*   [**Usage Guide**](docs/USAGE.md)
*   [**Music Theory Reference**](docs/MUSIC_THEORY.md)
*   [**Performance Optimizations**](docs/PERFORMANCE.md)
*   [**Troubleshooting**](docs/TROUBLESHOOTING.md)

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