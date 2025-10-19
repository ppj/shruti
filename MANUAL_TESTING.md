# Manual Testing Guide - Training Feature

## Quick Start

```bash
# Connect physical Android device with USB debugging enabled
# (Virtual devices have limited audio support)

# Build and install
./gradlew installDebug

# Or run from Android Studio: Shift+F10
```

## Testing Flow

### 1. Basic Functionality
1. Launch app, grant microphone permission if needed
2. Tap "Train" tab in bottom navigation
3. Verify training screen loads with:
   - Level 1, ±20¢ tolerance
   - Target note (e.g., "S", "R", "G")
   - Timer counting up
   - Frequency slider
   - "Check My Tuning" button (initially disabled)

### 2. Audio Testing
**CRITICAL:** Test on physical device - emulators have poor audio support

1. **Auto-play:** Detuned note should play automatically when exercise loads
2. **Replay:** Tap play button (▶) - note plays again
3. **Sound quality:** Should sound like a plucked string with decay
4. **Pitch variation:** Different target notes have different pitches

### 3. Exercise Flow
1. **Move slider** - adjust frequency
2. **Check enabled** - "Check My Tuning" button becomes active
3. **Submit attempt** - tap "Check My Tuning"
4. **See result:**
   - Animated overlay (slide up + fade in)
   - Success ✓ (green) or Fail ✗ (red/orange)
   - Deviation in cents (e.g., "+12¢")
   - Score earned this round
5. **Next exercise** - tap "Next Exercise"
6. **Auto-play** - new detuned note plays automatically

### 4. Scoring System
Test different scenarios:

**Perfect tuning (0¢ deviation, <10s):**
- Expected: ~150 points (100 base × 1.0 accuracy × 1.5 time bonus)

**Good tuning (±5¢, 15s):**
- Expected: ~100-120 points

**Acceptable tuning (±15¢, 25s):**
- Expected: ~30-50 points

**Outside tolerance (>20¢):**
- Expected: 0 points

**Very slow (>30s):**
- Expected: Reduced score but no penalty below 0.5× multiplier

### 5. Level Progression
1. Complete 10 successful exercises (within ±20¢)
2. Watch progress bar fill up (0/10 → 10/10)
3. On 10th success:
   - Level advances to Level 2
   - Tolerance becomes ±10¢
   - New swaras unlock: r, g, m, d, n (chromatic)
   - Progress resets to 0/10

4. Complete 10 more at Level 2 → Level 3 (±5¢)

### 6. State Persistence
1. **Score persistence:**
   - Note current score
   - Close app (swipe away from recents)
   - Reopen app → score should be preserved

2. **Level persistence:**
   - Advance to Level 2
   - Close app
   - Reopen → should still be Level 2 with ±10¢

3. **Tab switching:**
   - Switch to "Detect" tab
   - Switch back to "Train" → state preserved

### 7. Edge Cases

**Slider movement:**
- [ ] Move slider very slightly - button should enable
- [ ] Move slider to extremes (±100 cents range)
- [ ] Move slider while result showing - should be disabled

**Timer behavior:**
- [ ] Timer starts when exercise loads
- [ ] Timer continues while adjusting slider
- [ ] Timer stops when showing result
- [ ] Timer resets for next exercise

**Audio playback:**
- [ ] Tap play multiple times rapidly - should handle gracefully
- [ ] Play while result showing - should be disabled
- [ ] Audio continues if app is backgrounded briefly

**Result overlay:**
- [ ] Appears with smooth animation
- [ ] Blocks interaction with controls behind it
- [ ] "Next Exercise" is the only interactive element
- [ ] Different colors based on accuracy

## Known Behaviors

### Expected:
- Detuned note plays automatically on each new exercise
- No real-time cents deviation display (by design - forces ear training)
- Chromatic swaras only appear at Level 2+
- Progress resets when level advances
- Score accumulates indefinitely

### Audio Notes:
- Uses synthesized sine wave with exponential decay
- Sample rate: 44100 Hz, mono, PCM 16-bit
- Duration: ~1 second
- May sound different from real tanpura (intentional - pure tone)

## Troubleshooting

**No audio:**
- Ensure physical device (not emulator)
- Check device volume
- Check app permissions
- Try different target notes

**Crashes on start:**
- Check logcat: `adb logcat | grep Shruti`
- Verify all dependencies compiled

**Slider not responding:**
- May be disabled during result display
- Try tapping "Next Exercise" first

**Score not persisting:**
- DataStore may need time to save
- Try waiting a few seconds before closing app
- Check for storage permissions (should be automatic)

## Success Criteria

Feature is working correctly if:
- ✅ All 7 testing sections pass
- ✅ No crashes during normal use
- ✅ Audio plays clearly on physical device
- ✅ Score and level persist across app restarts
- ✅ Level progression works at 10 successful exercises
- ✅ UI is responsive and animations are smooth
- ✅ Bottom navigation works without issues

## Reporting Issues

If you find bugs, note:
1. **Steps to reproduce**
2. **Expected behavior**
3. **Actual behavior**
4. **Device info** (model, Android version)
5. **Logcat output** if crash occurs
