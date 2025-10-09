# Project Overview

This is a real-time pitch detection Android app for Hindustani classical music students. It provides visual feedback on vocal accuracy, helping musicians practice and improve their pitch.

**Main Technologies:**

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Audio Processing:** `AudioRecord` + Kotlin Coroutines
*   **Pitch Detection:** PYIN (Probabilistic YIN) algorithm

**Key Features:**

*   Real-time pitch detection and display in Hindustani swaras.
*   Customizable pitch accuracy tolerance.
*   Supports both 12-note Just Intonation and 22-shruti tuning systems.
*   Built-in tanpura/drone with configurable settings.
*   Clean and intuitive user interface.

# Building and Running

**Prerequisites:**

*   Android Studio Hedgehog (2023.1.1) or newer
*   JDK 11 or higher
*   Android SDK with minimum API 24

**Build Steps:**

1.  **Clone/Open the project.**
2.  **Sync Gradle:** Open the project in Android Studio and wait for Gradle to sync.
3.  **Build the app:**
    ```bash
    ./gradlew assembleDebug
    ```
4.  **Install on device:**
    ```bash
    ./gradlew installDebug
    ```
    Alternatively, use the "Run" button in Android Studio (Shift+F10).

**Running Tests:**

*   **Unit Tests:**
    ```bash
    ./gradlew testDebugUnitTest
    ```
*   **Android Instrumentation Tests:**
    ```bash
    ./gradlew connectedDebugAndroidTest
    ```

# Development Conventions

*   **Coding Style:** The project follows the standard Kotlin coding conventions.
*   **State Management:** The app uses `StateFlow` to manage and observe UI state.
*   **UI:** The UI is built entirely with Jetpack Compose, following Material 3 design guidelines.
*   **Asynchronous Operations:** Kotlin Coroutines are used for handling asynchronous tasks like audio processing.
*   **Branching:** GitFlow
*   **Commits:** (TODO: Add commit message conventions if available)
