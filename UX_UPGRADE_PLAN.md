# Kansen UX Upgrade Checklist

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
  - [x] Note Display (large swara)
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
- [ ] Run existing UI tests and update as needed

## 6. Dynamic Tanpura String 1 Selection
- [ ] Remove `enabled = !isTanpuraPlaying` constraint in `MainScreen.kt:137`
- [ ] Allow changing tanpura string 1 while tanpura is playing
- [ ] Allow changing tanpura string 1 while listening/recording is active
- [ ] Update `TanpuraPlayer` to support seamless switching between string 1 notes
- [ ] Test smooth transitions when changing string 1 during playback

## 7. Add Help Screen
- [ ] Create `HelpScreen.kt` composable
- [ ] Add "help" navigation route in `MainActivity.kt`
- [ ] Add navigation link to Help screen (from Settings or main menu)
- [ ] Document swara notation system (S, R, G, M, P, D, N)
- [ ] Explain octave notation (`.S` mandra, `S` madhya, `S'` taar)
- [ ] Describe tuning systems (12-note Just Intonation vs 22-shruti)
- [ ] Include app usage guide and feature explanations
- [ ] Add tolerance level guidance for different skill levels

## 8. Redesign UI Theme
- [ ] Design new color scheme reflecting Hindustani classical music aesthetics
- [ ] Update `Theme.kt` with new color palette
- [ ] Consider adding dark theme support
- [ ] Implement vibrant, culturally-appropriate colors
- [ ] Add gradient accents or warm tones suitable for classical music
- [ ] Update all color references throughout the app
- [ ] Test theme consistency across all screens

## 9. Redesign Logo
- [ ] Design new logo concept for Hindustani classical music app
- [ ] Update `ic_launcher_foreground.xml` with new design
- [ ] Incorporate musical elements (tanpura, swara symbols, etc.)
- [ ] Update `ic_launcher_colors.xml` if needed
- [ ] Follow Material Design adaptive icon guidelines
- [ ] Create visually distinctive icon for app stores
- [ ] Test logo visibility on different backgrounds and device launchers

## 10. Rename App
- [ ] Choose new app name (shorter, memorable, culturally relevant)
- [ ] Update `strings.xml` app_name resource
- [ ] Update `AndroidManifest.xml` theme references if needed
- [ ] Update README.md with new app name
- [ ] Update CLAUDE.md with new app name
- [ ] Update any other documentation references
- [ ] Search codebase for hardcoded references to old name
