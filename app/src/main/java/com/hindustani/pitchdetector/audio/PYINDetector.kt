package com.hindustani.pitchdetector.audio

import kotlin.math.abs
import kotlin.math.min

/**
 * PYIN (Probabilistic YIN) pitch detection algorithm
 * Provides better accuracy and confidence scores compared to standard YIN
 */
class PYINDetector(
    private val sampleRate: Int = 44100,
    private val threshold: Float = 0.15f,
) {
    data class PitchResult(
        val frequency: Float?,
        val confidence: Float,
    )

    fun detectPitch(audioBuffer: FloatArray): PitchResult {
        if (audioBuffer.size < 2048) {
            return PitchResult(null, 0f)
        }

        // YIN buffer size should cover the range of periods we want to detect
        // For 80-1000 Hz at 44100 Hz sample rate:
        // Min period (1000 Hz) = 44.1 samples
        // Max period (80 Hz) = 551 samples
        // Use half the audio buffer size or 2048, whichever is smaller
        val yinBuffer = FloatArray(minOf(audioBuffer.size / 2, 2048))

        // Step 1: Calculate difference function
        calculateDifferenceFunction(audioBuffer, yinBuffer)

        // Step 2: Calculate cumulative mean normalized difference function
        calculateCumulativeMeanNormalizedDifference(yinBuffer)

        // Step 3: Get multiple pitch candidates with their probabilities
        val candidates = getPitchCandidates(yinBuffer)

        if (candidates.isEmpty()) {
            return PitchResult(null, 0f)
        }

        // Step 4: Select best candidate based on probability
        val bestCandidate = candidates.maxByOrNull { it.probability }!!

        // Step 5: Calculate confidence based on probability and clarity
        val confidence = calculateConfidence(bestCandidate, candidates)

        // Step 6: Parabolic interpolation for sub-sample accuracy
        val refinedTau = parabolicInterpolation(yinBuffer, bestCandidate.tau)
        val frequency = sampleRate / refinedTau

        return PitchResult(frequency, confidence)
    }

    private fun calculateDifferenceFunction(
        audioBuffer: FloatArray,
        yinBuffer: FloatArray,
    ) {
        for (tau in yinBuffer.indices) {
            var sum = 0f
            val maxI = audioBuffer.size - tau - 1
            for (i in 0 until maxI) {
                val delta = audioBuffer[i] - audioBuffer[i + tau]
                sum += delta * delta
            }
            yinBuffer[tau] = sum
        }
    }

    private fun calculateCumulativeMeanNormalizedDifference(yinBuffer: FloatArray) {
        yinBuffer[0] = 1f
        var runningSum = 0f

        for (tau in 1 until yinBuffer.size) {
            runningSum += yinBuffer[tau]
            if (runningSum > 0) {
                yinBuffer[tau] = yinBuffer[tau] * tau / runningSum
            } else {
                yinBuffer[tau] = 1f
            }
        }
    }

    private data class PitchCandidate(
        val tau: Int,
        val value: Float,
        val probability: Float,
    )

    private fun getPitchCandidates(yinBuffer: FloatArray): List<PitchCandidate> {
        val candidates = mutableListOf<PitchCandidate>()

        // Find local minima below threshold
        var tau = 2
        while (tau < yinBuffer.size - 1) {
            if (yinBuffer[tau] < threshold) {
                // Check if it's a local minimum
                if (yinBuffer[tau] < yinBuffer[tau - 1] && yinBuffer[tau] < yinBuffer[tau + 1]) {
                    // Calculate probability (inverse of the dip value)
                    val probability = 1f - yinBuffer[tau]
                    candidates.add(PitchCandidate(tau, yinBuffer[tau], probability))
                }
            }
            tau++
        }

        // If no candidates found with threshold, take the absolute minimum
        if (candidates.isEmpty()) {
            val minTau = yinBuffer.indices.minByOrNull { yinBuffer[it] } ?: 0
            if (minTau >= 2) {
                val probability = 1f - yinBuffer[minTau]
                candidates.add(PitchCandidate(minTau, yinBuffer[minTau], probability))
            }
        }

        return candidates
    }

    private fun calculateConfidence(
        bestCandidate: PitchCandidate,
        allCandidates: List<PitchCandidate>,
    ): Float {
        // Confidence based on:
        // 1. How low the YIN value is (lower is better)
        // 2. How much better it is than other candidates

        val valueConfidence = 1f - bestCandidate.value

        // Calculate separation from second best candidate
        val secondBest =
            allCandidates
                .filter { it.tau != bestCandidate.tau }
                .maxByOrNull { it.probability }

        val separationConfidence =
            if (secondBest != null) {
                val separation = abs(bestCandidate.probability - secondBest.probability)
                min(separation * 2f, 1f) // Scale separation
            } else {
                1f
            }

        // Combine both factors
        return (valueConfidence * 0.7f + separationConfidence * 0.3f).coerceIn(0f, 1f)
    }

    private fun parabolicInterpolation(
        array: FloatArray,
        x: Int,
    ): Float {
        if (x < 1 || x >= array.size - 1) return x.toFloat()

        val s0 = array[x - 1]
        val s1 = array[x]
        val s2 = array[x + 1]

        // Parabolic interpolation formula
        val adjustment = (s2 - s0) / (2 * (2 * s1 - s2 - s0))
        return x + adjustment
    }
}
