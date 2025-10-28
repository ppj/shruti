# Audio Testing Guide

## Issue: Virtual Devices Don't Detect Audio

Android emulators have **very limited microphone support**. This is a known limitation.

## Solutions

### ✅ **Option 1: Use a Physical Android Device (Recommended)**

**This is the BEST way to test the app!**

1. **Enable Developer Mode on your phone:**
   - Settings → About Phone
   - Tap "Build Number" 7 times
   - You'll see "You are now a developer!"

2. **Enable USB Debugging:**
   - Settings → System → Developer Options
   - Enable "USB Debugging"

3. **Connect via USB:**
   - Plug phone into computer
   - Accept "Allow USB Debugging" prompt on phone

4. **Run the App:**
   - In Android Studio, click ▶️ Run
   - Select your physical device
   - Grant microphone permission when prompted
   - Start singing/humming and watch it detect!

### ⚙️ **Option 2: Configure Emulator Microphone (Limited)**

The emulator can *technically* use your computer's microphone, but results vary:

1. **Check Emulator Settings:**
   - Tools → Device Manager
   - Click ⋮ (three dots) on your emulator
   - Edit → Show Advanced Settings
   - Under "Camera", set Front camera to "Webcam0" (this often enables mic)

2. **Grant System Permissions:**
   - macOS: System Preferences → Security & Privacy → Microphone
   - Allow "Android Emulator" or "qemu-system"

3. **Restart Emulator:**
   - Close and restart the virtual device
   - Run the app again

**Note:** Even with this setup, emulator audio quality is poor and detection may be unreliable.

### 🎵 **Option 3: Test with Audio File Playback (For Development)**

If you need to test without hardware, you could modify the app temporarily to:

1. **Load a test audio file** instead of live microphone
2. **Play synthetic tones** at specific frequencies
3. **Use pre-recorded vocal samples**

Example modification (for testing only):

```kotlin
// In PitchViewModel.kt - TEST ONLY
private fun generateTestTone(frequency: Float = 440f): FloatArray {
    val sampleRate = 44100
    val duration = 0.1f
    val numSamples = (sampleRate * duration).toInt()

    return FloatArray(numSamples) { i ->
        (0.5f * sin(2.0 * PI * frequency * i / sampleRate)).toFloat()
    }
}

// Replace real audio capture with test tone
fun startTestMode() {
    viewModelScope.launch {
        while (true) {
            delay(100)
            val testAudio = generateTestTone(440f) // A4
            processAudioData(testAudio)
        }
    }
}
```

## Testing Checklist (Physical Device)

When testing on a real device, verify:

- [ ] **Microphone Permission**: App requests and receives permission
- [ ] **Pitch Detection**: Detects your singing/humming
- [ ] **Note Display**: Shows correct swar (S, R, G, P, etc.)
- [ ] **Pitch Indicator**: Needle moves based on pitch
- [ ] **Color Feedback**: Green (perfect), Blue (flat), Red (sharp)
- [ ] **Tolerance Settings**: Adjusting tolerance affects perfect/flat/sharp
- [ ] **Sa Changes**: Changing Sa recalculates all notes correctly
- [ ] **Tuning Systems**: Switching between 12-note and 22-shruti works

## Expected Behavior on Real Device

### **Test Sequence:**

1. **Set your Sa:**
   - Go to Settings (gear icon)
   - Enter your comfortable singing note (e.g., "D4" for males, "A4" for females)
   - See green ✓ with frequency

2. **Start Detection:**
   - Tap green "Start" button
   - Button turns red and shows "Stop"

3. **Sing Sa (your tonic):**
   - Should show "S" in large display
   - Needle should be centered
   - Status: "Perfect!" (if within tolerance)

4. **Sing other notes:**
   - Try singing higher: Should show R, G, M, P, etc.
   - Try singing lower: Should show n, D, d, etc.
   - Needle moves left (flat) or right (sharp)

5. **Test Tolerance:**
   - Settings → Adjust tolerance slider
   - Expert (±5¢): Very strict, harder to get "Perfect!"
   - Beginner (±30¢): More forgiving

## Troubleshooting

### "No pitch detected" (shows "—")

**Possible causes:**
- Not singing loud enough
- Background noise interfering
- Frequency out of range (below 80 Hz or above 1000 Hz)
- Microphone blocked or faulty

**Solutions:**
- Sing louder and clearer
- Move to quiet room
- Hold phone closer to mouth
- Check phone's microphone with voice recorder app

### "Pitch jumps around erratically"

**Possible causes:**
- Too much background noise
- Voice has vibrato
- Singing multiple notes quickly

**Solutions:**
- Reduce background noise
- Sing sustained, steady notes
- Increase tolerance in settings

### "Always shows flat/sharp, never perfect"

**Possible causes:**
- Sa is set incorrectly
- Your natural pitch is different from Sa
- Phone microphone calibration

**Solutions:**
- Settings → Set Sa to a note you can comfortably sing
- Try different Sa values (C3, C4, D4, etc.)
- Increase tolerance temporarily to test

## Performance Tips

- **Battery Usage**: Audio processing uses CPU. Stop when not practicing.
- **Low Latency**: Physical devices have ~10-15ms latency (imperceptible)
- **Best Results**: Quiet room, clear sustained notes, proper Sa selection

---

**Recommendation**: For the best experience testing this app, **use a real Android phone**. The pitch detection is accurate and responsive on hardware!
