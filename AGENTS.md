# Agent Guidelines for Shruti

## Commands

**Build:** `./gradlew assembleDebug` | **Lint:** `./gradlew ktlintCheck` | **Format:** `./gradlew ktlintFormat`  
**Test (all):** `./gradlew test` | **Test (single):** `./gradlew test --tests "com.hindustani.pitchdetector.music.SaParserTest"`  
**UI Tests:** `./gradlew connectedAndroidTest` (requires physical device for audio testing)

## Code Style

**Imports:** Wildcard imports allowed (Compose, tests). Order: stdlib → Android → 3rd party → project  
**Formatting:** 140 char line length, 4-space indent, ktlint enforced  
**Types:** Explicit types for public APIs, nullable types with safe calls (`?.`) or `!!` only when guaranteed non-null  
**Naming:** camelCase vars/functions, PascalCase classes/Composables, SCREAMING_SNAKE_CASE constants in companion objects  
**Nullability:** Prefer `?.let {}` over `!!`, use `?:` for defaults  
**Error Handling:** Return null for invalid input (parsing), throw exceptions for programmer errors  

## Architecture Patterns

**State:** Use `StateFlow` with `.update {}`, never mutate directly. Hoist state to ViewModels  
**Coroutines:** `Dispatchers.Default` for audio/CPU work, `Dispatchers.Main` for UI updates, `viewModelScope` for lifecycle  
**Compose:** Stateless composables, use `remember` for computation caching, Material 3 design system  
**Testing:** Google Truth assertions (`assertThat`), MockK for mocks, backtick test names, Robolectric for Android unit tests
