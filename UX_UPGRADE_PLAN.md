# Shruti UX Upgrade Checklist

## 1. Piano Keyboard Sa Selector
- [x] Create `PianoKeyboardSelector.kt` component
  - [x] Visual piano keyboard with white and black keys (G#2 to A#3)
  - [x] Highlight selected Sa with color/elevation
  - [x] Show frequency on key press
  - [x] Compact horizontal layout
- [x] Replace dropdown in `MainScreen.kt` with keyboard component
- [x] Position below header, above note display
- [x] Allow Sa selection during recording (no stop required)

## 2. Modern Horizontal Pitch Bar
- [x] Rename `PitchIndicator.kt` → `PitchBar.kt`
- [x] Replace semi-circle with horizontal bar design (-50¢ to +50¢)
- [x] Gradient background: Blue (flat) → Green (center) → Red (sharp)
- [x] Animated indicator dot with smooth sliding
- [x] Visualize tolerance zone as green band
- [x] Larger cents text above bar
- [x] Emoji status feedback: 🟢 (perfect), ➡️ (sharpen), ⬅️ (flatten)
- [x] Enhanced smoothing with spring animations

## 3. Independent Tanpura Toggle
- [x] Remove `enabled = !isRecording` constraint in `MainScreen.kt`
- [x] Set tanpura toggle to always enabled
- [x] Keep string selection constraint: `enabled = !isTanpuraPlaying`
- [x] Remove logic that stops recording when opening Sa selector

## 4. Update MainScreen Layout
- [x] Rearrange components in correct order:
  - [x] Header (Sa label + Settings icon)
  - [x] Piano Keyboard (new)
  - [x] Note Display (large swar)
  - [x] Pitch Bar (replaces needle)
  - [x] Tanpura Card
  - [x] Recording Button
- [x] Adjust spacing for new components

## 5. Testing
- [x] Test Sa selection during recording
- [x] Test tanpura toggle during recording
- [x] Test pitch bar smoothness and accuracy
- [ ] Test keyboard interaction on various screen sizes
- [ ] Test emoji rendering on different Android versions
- [x] Run existing UI tests and update as needed

## 6. Dynamic Tanpura String 1 Selection
- [x] Remove `enabled = !isTanpuraPlaying` constraint in `MainScreen.kt:137`
- [x] Allow changing tanpura string 1 while tanpura is playing
- [x] Allow changing tanpura string 1 while listening/recording is active
- [x] Update `TanpuraPlayer` to support seamless switching between string 1 notes (already implemented)
- [x] Test smooth transitions when changing string 1 during playback

## 7. Add Contextual Help via Tooltips
- [x] Create `HelpTooltip.kt` reusable component with info icon
- [x] Add tooltips to MainScreen.kt:
  - [x] Sa label: Explain tonic concept
  - [x] Piano keyboard: Selection instructions
  - [x] Note display: Octave notation (`.S` mandra, `S` madhya, `S'` taar)
  - [x] Pitch bar: Cents deviation + emoji meanings (🟢 ➡️ ⬅️)
  - [x] Tanpura card: Traditional drone instrument explanation
  - [x] Confidence indicator: Detection quality explanation
- [x] Add tooltips to SettingsScreen.kt:
  - [x] Tuning system: 12-note vs 22-shruti explanation
  - [x] Tanpura volume: Purpose and usage guidance
- [x] Tooltips show on tap/long-press (mobile-friendly)
- [x] Concise, actionable help text (1-2 sentences)

## 8. Redesign UI Theme
- [x] Design new color scheme reflecting Hindustani classical music aesthetics
- [x] Update `Theme.kt` with new color palette
- [x] Consider adding dark theme support
- [x] Implement vibrant, culturally-appropriate colors
- [x] Add gradient accents or warm tones suitable for classical music
- [x] Update all color references throughout the app
- [x] Test theme consistency across all screens

## 9. Redesign Logo
- [x] Design new logo concept for Hindustani classical music app
- [x] Update `ic_launcher_foreground.xml` with new design
- [x] Incorporate musical elements (tanpura, swar symbols, etc.)
- [x] Update `ic_launcher_colors.xml` if needed
- [x] Follow Material Design adaptive icon guidelines
- [x] Create visually distinctive icon for app stores
- [x] Test logo visibility on different backgrounds and device launchers

## 10. Rename App
- [x] Choose new app name (shorter, memorable, culturally relevant) → **Shruti**
- [x] Update `strings.xml` app_name resource
- [x] Update `AndroidManifest.xml` theme references
- [x] Update `Theme.kt` theme name to `ShrutiTheme`
- [x] Update `MainActivity.kt` theme import and usage
- [x] Update README.md with new app name
- [x] Update CLAUDE.md with new app name
- [x] Update TESTING.md with new app name
- [x] Build and test to verify all references updated correctly

## 11. Enhance Tanpura Card with Visual Icon
- [x] Find or create tanpura icon/image asset
- [x] Add tanpura icon to drawable resources
- [x] Remove "Tanpura" text label from card
- [x] Redesign card layout with three columns:
  - [x] Left: String 1 selector dropdown
  - [x] Center: Small tanpura image/icon
  - [x] Right: On/off toggle switch (keep current position)
- [x] Ensure proper spacing and alignment
- [x] Test visual balance and accessibility
- [x] Verify icon is visible in both light and dark themes
