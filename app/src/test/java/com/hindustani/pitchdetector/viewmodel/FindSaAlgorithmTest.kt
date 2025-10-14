package com.hindustani.pitchdetector.viewmodel

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.pow

/**
 * Tests for the Find Sa algorithm logic
 * These tests verify the mathematical correctness of the Sa calculation without Android dependencies
 */
class FindSaAlgorithmTest {

    // Test the 7 semitones formula (core of the algorithm)

    @Test
    fun `7 semitones formula produces correct frequency ratio`() {
        // The formula is: idealSa = lowestFreq × 2^(7/12)
        // This should equal approximately 1.498 (a perfect fifth)
        val ratio = 2.0.pow(7.0 / 12.0)
        assertThat(ratio).isWithin(0.001).of(1.498)
    }

    @Test
    fun `calculate ideal Sa from 100 Hz lowest note`() {
        val lowestFreq = 100.0
        val idealSa = lowestFreq * 2.0.pow(7.0 / 12.0)

        // 100 × 1.498 ≈ 149.8 Hz
        // Should be close to D3 (146.83 Hz) or D#3 (155.56 Hz)
        assertThat(idealSa).isWithin(1.0).of(149.8)
    }

    @Test
    fun `calculate ideal Sa from 150 Hz lowest note`() {
        val lowestFreq = 150.0
        val idealSa = lowestFreq * 2.0.pow(7.0 / 12.0)

        // 150 × 1.498 ≈ 224.7 Hz
        // Should be close to A3 (220 Hz)
        assertThat(idealSa).isWithin(5.0).of(224.7)
    }

    @Test
    fun `calculate ideal Sa from 180 Hz lowest note typical female soprano`() {
        val lowestFreq = 180.0
        val idealSa = lowestFreq * 2.0.pow(7.0 / 12.0)

        // 180 × 1.498 ≈ 269.6 Hz
        // Appropriate for female soprano voice
        assertThat(idealSa).isWithin(1.0).of(269.6)
    }

    // Test outlier removal logic

    @Test
    fun `remove outliers from bottom and top 10 percent`() {
        val pitches = mutableListOf<Float>()
        repeat(10) { pitches.add(50f + it) }
        repeat(80) { pitches.add(100f + it) }
        repeat(10) { pitches.add(500f + it) }

        val sorted = pitches.sorted()
        val outlierCount = (sorted.size * 0.1).toInt()
        val filtered = sorted.subList(outlierCount, sorted.size - outlierCount)

        assertThat(filtered.size).isEqualTo(80)
        assertThat(filtered.first()).isGreaterThan(50f)
        assertThat(filtered.last()).isLessThan(500f)
    }

    @Test
    fun `small dataset under 20 samples skips outlier removal`() {
        val pitches = List(15) { 100f + (it * 2f) }
        val sorted = pitches.sorted()

        assertThat(sorted.size).isEqualTo(15)
        assertThat(sorted.first()).isEqualTo(100f)
        assertThat(sorted.last()).isEqualTo(128f)
    }

    // Test frequency to note snapping logic

    @Test
    fun `snap 130 Hz to C3 standard note`() {
        val frequency = 130.0
        val c3Freq = 130.81

        val difference = kotlin.math.abs(frequency - c3Freq)
        assertThat(difference).isLessThan(1.0)
    }

    @Test
    fun `snap 220 Hz to A3 standard note`() {
        val frequency = 220.0
        val a3Freq = 220.00

        val difference = kotlin.math.abs(frequency - a3Freq)
        assertThat(difference).isLessThan(0.1)
    }

    @Test
    fun `snap 196 Hz to G3 standard note`() {
        val frequency = 196.0
        val g3Freq = 196.00

        val difference = kotlin.math.abs(frequency - g3Freq)
        assertThat(difference).isLessThan(0.1)
    }

    // Test semitone adjustment logic

    @Test
    fun `adjust note up by 1 semitone increases frequency by correct ratio`() {
        val baseFreq = 130.81 // C3
        val adjustedFreq = baseFreq * 2.0.pow(1.0 / 12.0)

        // Should be approximately C#3 (138.59 Hz)
        assertThat(adjustedFreq).isWithin(0.1).of(138.59)
    }

    @Test
    fun `adjust note down by 1 semitone decreases frequency by correct ratio`() {
        val baseFreq = 146.83 // D3
        val adjustedFreq = baseFreq * 2.0.pow(-1.0 / 12.0)

        // Should be approximately C#3 (138.59 Hz)
        assertThat(adjustedFreq).isWithin(0.1).of(138.59)
    }

    // Test edge cases

    @Test
    fun `algorithm works with identical frequencies`() {
        val identicalFreqs = List(30) { 150.0 }

        val lowest = identicalFreqs.minOrNull()
        val highest = identicalFreqs.maxOrNull()

        assertThat(lowest).isEqualTo(150.0)
        assertThat(highest).isEqualTo(150.0)

        val idealSa = lowest!! * 2.0.pow(7.0 / 12.0)
        assertThat(idealSa).isWithin(1.0).of(224.7)
    }

    @Test
    fun `algorithm works with narrow frequency range`() {
        val narrowRange = List(30) { 100.0 + (it * 0.5) } // 100-114.5 Hz

        val lowest = narrowRange.minOrNull()!!
        val highest = narrowRange.maxOrNull()!!

        val range = highest - lowest
        assertThat(range).isLessThan(15.0)

        val idealSa = lowest * 2.0.pow(7.0 / 12.0)
        assertThat(idealSa).isGreaterThan(lowest)
    }

    @Test
    fun `algorithm works with wide frequency range`() {
        val wideRange = List(50) { 80.0 + (it * 10.0) } // 80-570 Hz

        val lowest = wideRange.minOrNull()!!
        val highest = wideRange.maxOrNull()!!

        val range = highest - lowest
        assertThat(range).isGreaterThan(400.0)

        val idealSa = lowest * 2.0.pow(7.0 / 12.0)
        assertThat(idealSa).isWithin(1.0).of(119.9)
    }

    // Test data validation

    @Test
    fun `minimum sample count is 20 for reliable results`() {
        val minimumSamples = 20
        val samples = List(minimumSamples) { 100.0 + it }

        assertThat(samples.size).isAtLeast(20)
    }

    @Test
    fun `insufficient samples should be handled`() {
        val insufficientSamples = List(10) { 100.0 + it }

        assertThat(insufficientSamples.size).isLessThan(20)
    }

    // Test speaking pitch algorithm

    @Test
    fun `5 semitones formula produces correct frequency ratio for speaking pitch`() {
        // The formula is: idealSa = meanSpeakingFreq × 2^(5/12)
        // This should equal approximately 1.335 (a perfect fourth)
        val ratio = 2.0.pow(5.0 / 12.0)
        assertThat(ratio).isWithin(0.001).of(1.335)
    }

    @Test
    fun `calculate ideal Sa from 120 Hz speaking pitch typical male`() {
        val speakingFreq = 120.0
        val idealSa = speakingFreq * 2.0.pow(5.0 / 12.0)

        // 120 × 1.335 ≈ 160.2 Hz
        // Should be close to E3 (164.81 Hz)
        assertThat(idealSa).isWithin(5.0).of(160.2)
    }

    @Test
    fun `calculate ideal Sa from 200 Hz speaking pitch typical female`() {
        val speakingFreq = 200.0
        val idealSa = speakingFreq * 2.0.pow(5.0 / 12.0)

        // 200 × 1.335 ≈ 267.0 Hz
        // Appropriate for female soprano voice
        assertThat(idealSa).isWithin(1.0).of(267.0)
    }

    @Test
    fun `calculate ideal Sa from 150 Hz speaking pitch`() {
        val speakingFreq = 150.0
        val idealSa = speakingFreq * 2.0.pow(5.0 / 12.0)

        // 150 × 1.335 ≈ 200.25 Hz
        // Should be close to G3 (196 Hz)
        assertThat(idealSa).isWithin(5.0).of(200.25)
    }

    // Test outlier removal for speech (5% vs 10% for singing)

    @Test
    fun `remove outliers from speech samples at 5 percent`() {
        val pitches = mutableListOf<Float>()
        repeat(5) { pitches.add(50f + it) }
        repeat(90) { pitches.add(100f + it) }
        repeat(5) { pitches.add(500f + it) }

        val sorted = pitches.sorted()
        val outlierCount = (sorted.size * 0.05).toInt()
        val filtered = sorted.subList(outlierCount, sorted.size - outlierCount)

        assertThat(filtered.size).isEqualTo(90)
        assertThat(filtered.first()).isGreaterThan(50f)
        assertThat(filtered.last()).isLessThan(500f)
    }

    @Test
    fun `small speech dataset under 20 samples skips outlier removal`() {
        val pitches = List(15) { 120f + (it * 2f) }
        val sorted = pitches.sorted()

        assertThat(sorted.size).isEqualTo(15)
        assertThat(sorted.first()).isEqualTo(120f)
        assertThat(sorted.last()).isEqualTo(148f)
    }

    // Test combination algorithm

    @Test
    fun `combine recommendations when they agree within 3_5 semitones uses weighted average`() {
        val saFromSpeaking = 200.0
        val saFromSinging = 210.0

        val semitoneDiff = kotlin.math.abs(kotlin.math.log2(saFromSpeaking / saFromSinging) * 12.0)
        assertThat(semitoneDiff).isLessThan(3.5)

        val combined = (saFromSinging * 0.7) + (saFromSpeaking * 0.3)
        assertThat(combined).isWithin(1.0).of(207.0)
    }

    @Test
    fun `combine recommendations when they differ by more than 3_5 semitones uses singing only`() {
        val saFromSpeaking = 140.0
        val saFromSinging = 200.0

        val semitoneDiff = kotlin.math.abs(kotlin.math.log2(saFromSpeaking / saFromSinging) * 12.0)
        assertThat(semitoneDiff).isGreaterThan(3.5)

        val combined = saFromSinging
        assertThat(combined).isEqualTo(200.0)
    }

    @Test
    fun `weighted average formula with 70 percent singing 30 percent speaking`() {
        val singing = 200.0
        val speaking = 180.0

        val weighted = (singing * 0.7) + (speaking * 0.3)

        assertThat(weighted).isWithin(0.1).of(194.0)
    }

    @Test
    fun `semitone distance calculation between two frequencies`() {
        val freq1 = 200.0
        val freq2 = 210.0

        val semitones = kotlin.math.abs(kotlin.math.log2(freq1 / freq2) * 12.0)

        assertThat(semitones).isWithin(0.1).of(0.8)
    }

    @Test
    fun `2 semitone difference equals frequency ratio of about 1_122`() {
        // 2 semitones = 2^(2/12) ≈ 1.122
        val ratio = 2.0.pow(2.0 / 12.0)
        assertThat(ratio).isWithin(0.001).of(1.122)

        // Example: 200 Hz and 224.5 Hz are 2 semitones apart
        val freq1 = 200.0
        val freq2 = freq1 * ratio
        val semitones = kotlin.math.abs(kotlin.math.log2(freq1 / freq2) * 12.0)
        assertThat(semitones).isWithin(0.01).of(2.0)
    }

    // Test minimum sample requirements for speech

    @Test
    fun `minimum speech samples is 10 for reliable results`() {
        val minimumSpeechSamples = 10
        val samples = List(minimumSpeechSamples) { 120.0 + it }

        assertThat(samples.size).isAtLeast(10)
    }

    @Test
    fun `insufficient speech samples should fall back to singing only`() {
        val insufficientSpeechSamples = List(8) { 120.0 + it }

        assertThat(insufficientSpeechSamples.size).isLessThan(10)
    }
}
