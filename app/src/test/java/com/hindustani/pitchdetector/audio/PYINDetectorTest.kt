package com.hindustani.pitchdetector.audio

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class PYINDetectorTest {

    private lateinit var detector: PYINDetector
    private val sampleRate = 44100

    @Before
    fun setup() {
        detector = PYINDetector(sampleRate = sampleRate)
    }

    /**
     * Generate a synthetic sine wave at a given frequency
     */
    private fun generateSineWave(
        frequency: Float,
        durationSeconds: Float = 0.1f,
        amplitude: Float = 0.5f
    ): FloatArray {
        val numSamples = (sampleRate * durationSeconds).toInt()
        return FloatArray(numSamples) { i ->
            (amplitude * sin(2.0 * PI * frequency * i / sampleRate)).toFloat()
        }
    }

    /**
     * Generate a sine wave with noise
     */
    private fun generateNoisySineWave(
        frequency: Float,
        durationSeconds: Float = 0.1f,
        signalAmplitude: Float = 0.5f,
        noiseAmplitude: Float = 0.1f
    ): FloatArray {
        val numSamples = (sampleRate * durationSeconds).toInt()
        return FloatArray(numSamples) { i ->
            val signal = signalAmplitude * sin(2.0 * PI * frequency * i / sampleRate)
            val noise = (Math.random() - 0.5) * 2 * noiseAmplitude
            (signal + noise).toFloat()
        }
    }

    @Test
    fun `detectPitch identifies frequency for pure sine wave at 440Hz (A4)`() {
        val audioBuffer = generateSineWave(frequency = 440f)
        val result = detector.detectPitch(audioBuffer)

        assertThat(result.frequency).isNotNull()
        assertThat(result.frequency!!).isWithin(5f).of(440f)
        assertThat(result.confidence).isGreaterThan(0.5f)
    }

    @Test
    fun `detectPitch identifies frequency for pure sine wave at 261Hz (C4)`() {
        val audioBuffer = generateSineWave(frequency = 261.63f)
        val result = detector.detectPitch(audioBuffer)

        assertThat(result.frequency).isNotNull()
        assertThat(result.frequency!!).isWithin(5f).of(261.63f)
        assertThat(result.confidence).isGreaterThan(0.5f)
    }

    @Test
    fun `detectPitch identifies frequency for pure sine wave at 196Hz (G3)`() {
        val audioBuffer = generateSineWave(frequency = 196f)
        val result = detector.detectPitch(audioBuffer)

        assertThat(result.frequency).isNotNull()
        assertThat(result.frequency!!).isWithin(5f).of(196f)
    }

    @Test
    fun `detectPitch handles noisy signal reasonably`() {
        val audioBuffer = generateNoisySineWave(
            frequency = 440f,
            signalAmplitude = 0.7f,
            noiseAmplitude = 0.2f
        )
        val result = detector.detectPitch(audioBuffer)

        // With noise, we may not get perfect detection, but it should still work
        // or return low confidence
        if (result.frequency != null) {
            // If it detected something, it should be reasonably close
            assertThat(result.frequency!!).isWithin(20f).of(440f)
        }
    }

    @Test
    fun `detectPitch returns null for insufficient buffer size`() {
        val smallBuffer = FloatArray(100) { 0f }
        val result = detector.detectPitch(smallBuffer)

        assertThat(result.frequency).isNull()
        assertThat(result.confidence).isEqualTo(0f)
    }

    @Test
    fun `detectPitch returns low confidence for silence`() {
        val silentBuffer = FloatArray(4096) { 0f }
        val result = detector.detectPitch(silentBuffer)

        // May return null or very low confidence
        if (result.frequency != null) {
            assertThat(result.confidence).isLessThan(0.3f)
        }
    }

    @Test
    fun `detectPitch returns low confidence for random noise`() {
        val noiseBuffer = FloatArray(4096) {
            ((Math.random() - 0.5) * 2).toFloat()
        }
        val result = detector.detectPitch(noiseBuffer)

        // Pure noise should have low confidence
        assertThat(result.confidence).isLessThan(0.5f)
    }

    @Test
    fun `detectPitch works for different frequencies in vocal range`() {
        val vocalFrequencies = listOf(
            130f,  // C3 (male low)
            196f,  // G3
            262f,  // C4 (middle C)
            392f,  // G4
            523f   // C5 (female high)
        )

        vocalFrequencies.forEach { frequency ->
            val audioBuffer = generateSineWave(frequency = frequency)
            val result = detector.detectPitch(audioBuffer)

            assertThat(result.frequency).isNotNull()
            assertThat(result.frequency!!).isWithin(10f).of(frequency)
        }
    }

    @Test
    fun `detectPitch has higher confidence for louder signals`() {
        val quietBuffer = generateSineWave(frequency = 440f, amplitude = 0.1f)
        val loudBuffer = generateSineWave(frequency = 440f, amplitude = 0.8f)

        val quietResult = detector.detectPitch(quietBuffer)
        val loudResult = detector.detectPitch(loudBuffer)

        // Louder signal should generally have higher confidence
        if (quietResult.frequency != null && loudResult.frequency != null) {
            assertThat(loudResult.confidence).isAtLeast(quietResult.confidence)
        }
    }

    @Test
    fun `detectPitch is consistent across multiple calls with same input`() {
        val audioBuffer = generateSineWave(frequency = 330f)

        val result1 = detector.detectPitch(audioBuffer)
        val result2 = detector.detectPitch(audioBuffer)
        val result3 = detector.detectPitch(audioBuffer)

        // Results should be consistent
        assertThat(result1.frequency).isNotNull()
        assertThat(result2.frequency).isNotNull()
        assertThat(result3.frequency).isNotNull()

        assertThat(result2.frequency!!).isWithin(1f).of(result1.frequency!!)
        assertThat(result3.frequency!!).isWithin(1f).of(result1.frequency!!)
    }

    @Test
    fun `detectPitch accuracy improves with longer buffer`() {
        val shortBuffer = generateSineWave(frequency = 440f, durationSeconds = 0.05f)
        val longBuffer = generateSineWave(frequency = 440f, durationSeconds = 0.2f)

        val shortResult = detector.detectPitch(shortBuffer)
        val longResult = detector.detectPitch(longBuffer)

        assertThat(shortResult.frequency).isNotNull()
        assertThat(longResult.frequency).isNotNull()

        // Longer buffer should generally have better accuracy
        val shortError = abs(shortResult.frequency!! - 440f)
        val longError = abs(longResult.frequency!! - 440f)

        // Long buffer should be at least as accurate
        assertThat(longError).isAtMost(shortError + 2f)
    }

    @Test
    fun `detectPitch confidence is between 0 and 1`() {
        val audioBuffer = generateSineWave(frequency = 440f)
        val result = detector.detectPitch(audioBuffer)

        assertThat(result.confidence).isAtLeast(0f)
        assertThat(result.confidence).isAtMost(1f)
    }

    @Test
    fun `detectPitch handles extreme low frequency`() {
        // Test at 80 Hz (very low vocal range)
        val audioBuffer = generateSineWave(frequency = 80f, durationSeconds = 0.2f)
        val result = detector.detectPitch(audioBuffer)

        // Should either detect it or return null with low confidence
        if (result.frequency != null) {
            assertThat(result.frequency!!).isWithin(10f).of(80f)
        }
    }

    @Test
    fun `detectPitch handles extreme high frequency`() {
        // Test at 1000 Hz (high vocal range)
        val audioBuffer = generateSineWave(frequency = 1000f)
        val result = detector.detectPitch(audioBuffer)

        if (result.frequency != null) {
            assertThat(result.frequency!!).isWithin(20f).of(1000f)
        }
    }

    @Test
    fun `PYIN provides confidence scores unlike basic YIN`() {
        val audioBuffer = generateSineWave(frequency = 440f)
        val result = detector.detectPitch(audioBuffer)

        // PYIN should provide a meaningful confidence score
        assertThat(result.confidence).isGreaterThan(0f)

        // For a clean sine wave, confidence should be high
        if (result.frequency != null) {
            assertThat(result.confidence).isGreaterThan(0.6f)
        }
    }

    @Test
    fun `detectPitch handles buffer with varying amplitude (vibrato-like)`() {
        // Simulate simple amplitude modulation (like vibrato)
        val numSamples = (sampleRate * 0.15f).toInt()
        val frequency = 440f
        val vibratoRate = 5f // 5 Hz vibrato

        val audioBuffer = FloatArray(numSamples) { i ->
            val amplitude = 0.5f * (1 + 0.3f * sin(2.0 * PI * vibratoRate * i / sampleRate).toFloat())
            (amplitude * sin(2.0 * PI * frequency * i / sampleRate)).toFloat()
        }

        val result = detector.detectPitch(audioBuffer)

        // Should still detect the fundamental frequency
        assertThat(result.frequency).isNotNull()
        assertThat(result.frequency!!).isWithin(15f).of(frequency)
    }

    @Test
    fun `detectPitch distinguishes between similar frequencies`() {
        val freq1 = 440f
        val freq2 = 450f // About 38 cents higher

        val buffer1 = generateSineWave(frequency = freq1)
        val buffer2 = generateSineWave(frequency = freq2)

        val result1 = detector.detectPitch(buffer1)
        val result2 = detector.detectPitch(buffer2)

        assertThat(result1.frequency).isNotNull()
        assertThat(result2.frequency).isNotNull()

        // Should be able to distinguish the two frequencies
        assertThat(abs(result1.frequency!! - result2.frequency!!)).isGreaterThan(5f)
    }
}
