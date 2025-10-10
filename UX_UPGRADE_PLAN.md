# Kansen UX Upgrade Plan

## Current State Analysis

**1. Sa Selection (MainScreen.kt:48-114)**
- Dropdown menu with 15 notes (G#2 to A#3)
- Shows note name + frequency in Hz
- Forces recording to stop when opened

**2. Pitch Indicator (PitchIndicator.kt)**
- Semi-circular needle meter (Canvas-based)
- Color-coded: Green/Blue/Red for perfect/flat/sharp
- EMA smoothing (alpha=0.25) but still feels jumpy
- Dated mechanical appearance

**3. Tanpura Controls (MainScreen.kt:243)**
- Toggle disabled when recording (`enabled = !isRecording`)
- Prevents users from using drone during practice

---

## Proposed Changes

### 1. Piano Keyboard Sa Selector

**Create new component**: `PianoKeyboardSelector.kt`
- Visual piano keyboard with white and black keys
- Cover G#2 to A#3 (15 notes, ~1.5 octaves)
- Highlight selected Sa with color/elevation
- Show frequency on key press
- Compact horizontal layout (~12dp per white key)

**Update**: `MainScreen.kt`
- Replace dropdown (lines 48-114) with keyboard component
- Position below header, above note display
- No need to stop recording when selecting Sa

### 2. Modern Horizontal Pitch Bar

**Replace**: `PitchIndicator.kt` → `PitchBar.kt`
- **Horizontal bar design** (-50¢ to +50¢)
- Gradient background: Blue (flat) → Green (center) → Red (sharp)
- Animated indicator dot that slides smoothly
- Tolerance zone visualized as green band
- Larger cents text above bar
 **Emoji status feedback**: 🟢 (perfect), ➡️ (sharpen), ⬅️ (flatten)
- Enhanced smoothing with spring animations

### 3. Independent Tanpura Toggle

**Update**: `MainScreen.kt:243`
- Remove `enabled = !isRecording` constraint
- Change to: `enabled = true` (always enabled)
- Keep string selection constraint: `enabled = !isTanpuraPlaying`
- Users can start/stop tanpura anytime during practice

**Update**: `MainScreen.kt:59-64`
- Remove logic that stops recording when opening Sa selector
- Sa changes can happen during recording

---

## Implementation Steps

### Step 1: Create Feature Branch & Plan Document ✅
1. Create branch: `feature/ux-upgrade`
2. Create `UX_UPGRADE_PLAN.md` with this plan
3. Commit as first commit: "Add UX upgrade implementation plan"

### Step 2: Piano Keyboard Component
1. Create `app/src/main/java/com/hindustani/pitchdetector/ui/components/PianoKeyboardSelector.kt`
2. Implement:
   - White keys: Row with 7 base notes per octave
   - Black keys: Positioned with offset using Box/Stack
   - Selected state highlighting
   - Tap handlers for note selection
   - Frequency display on selection

### Step 3: Horizontal Pitch Bar
1. Rename `PitchIndicator.kt` → `PitchBar.kt`
2. Redesign UI:
   - Replace Canvas semi-circle with horizontal Box/Canvas
   - Gradient brush for background bar
   - Animated indicator using `animateFloatAsState`
   - Spring damping for smoother motion
   - **Update status emojis**: 🟢 / ➡️ / ⬅️
   - Adjust layout: wider aspect ratio (400dp × 80dp)

### Step 4: Remove Recording Dependencies
1. **MainScreen.kt:243**: Change `enabled = !isRecording` → `enabled = true`
2. **MainScreen.kt:188**: Keep `enabled = !isTanpuraPlaying`
3. **MainScreen.kt:59-64**: Remove `if (isRecording)` block that stops recording

### Step 5: Update MainScreen Layout
1. Rearrange components:
   - Header (Sa label + Settings icon)
   - **Piano Keyboard** (new)
   - Note Display (large swara)
   - **Pitch Bar** (replaces needle)
   - Tanpura Card
   - Recording Button
2. Adjust spacing for new components

### Step 6: Testing
1. Test Sa selection during recording
2. Test tanpura toggle during recording
3. Test pitch bar smoothness and accuracy
4. Test keyboard interaction on various screen sizes
5. Test emoji rendering on different Android versions
6. Run existing UI tests and update as needed

---

## Files to Create/Modify

**New files:**
- `UX_UPGRADE_PLAN.md` (this plan document)
- `app/src/main/java/com/hindustani/pitchdetector/ui/components/PianoKeyboardSelector.kt`

**Modified files:**
- `app/src/main/java/com/hindustani/pitchdetector/ui/components/PitchIndicator.kt` → `PitchBar.kt`
- `app/src/main/java/com/hindustani/pitchdetector/ui/MainScreen.kt` (major layout changes)

**Test updates needed:**
- UI tests that reference needle/PitchIndicator
- Tests for tanpura enable/disable logic

---

## Emoji Status Indicators

| Status | Emoji | Meaning |
|--------|-------|---------|
| Perfect | 🟢 | Pitch is within tolerance - love it! |
| Flat (low) | ➡️ | Pitch too low - sharpen (move right) |
| Sharp (high) | ⬅️ | Pitch too high - flatten (move left) |

---

## Benefits

✅ **Better Sa selection**: Visual, intuitive keyboard vs abstract dropdown
✅ **Smoother pitch feedback**: Animated horizontal bar, less mechanical
✅ **Instructional emoji feedback**: Directional arrows (➡️/⬅️) guide correction + perfect (🟢)
✅ **Improved workflow**: Tanpura and Sa changes without stopping practice
✅ **Modern aesthetic**: Cleaner, more polished UI components
✅ **Better UX flow**: Fewer interruptions during practice sessions
