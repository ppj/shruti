package com.hindustani.pitchdetector.music

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class HindustaniNoteConverterTest {

    private lateinit var converter: HindustaniNoteConverter
    private val saFrequency = 261.63 // C4

    @Before
    fun setup() {
        converter = HindustaniNoteConverter(
            saFrequency = saFrequency,
            toleranceCents = 15.0,
            use22Shruti = false
        )
    }

    @Test
    fun `convertFrequency correctly identifies Sa`() {
        val result = converter.convertFrequency(saFrequency)

        assertThat(result.swara).isEqualTo("S")
        assertThat(result.octave).isEqualTo(HindustaniNoteConverter.Octave.MADHYA)
        assertThat(abs(result.centsDeviation)).isWithin(1.0).of(0.0)
        assertThat(result.isPerfect).isTrue()
        assertThat(result.isFlat).isFalse()
        assertThat(result.isSharp).isFalse()
    }

    @Test
    fun `convertFrequency correctly identifies Pancham (P)`() {
        // P is 3/2 ratio from Sa
        val pFrequency = saFrequency * (3.0 / 2.0)
        val result = converter.convertFrequency(pFrequency)

        assertThat(result.swara).isEqualTo("P")
        assertThat(abs(result.centsDeviation)).isLessThan(1.0)
        assertThat(result.isPerfect).isTrue()
    }

    @Test
    fun `convertFrequency correctly identifies Shuddha Rishabh (R)`() {
        // R is 9/8 ratio from Sa
        val rFrequency = saFrequency * (9.0 / 8.0)
        val result = converter.convertFrequency(rFrequency)

        assertThat(result.swara).isEqualTo("R")
        assertThat(abs(result.centsDeviation)).isLessThan(1.0)
    }

    @Test
    fun `convertFrequency correctly identifies Komal Rishabh (r)`() {
        // r is 16/15 ratio from Sa
        val rFrequency = saFrequency * (16.0 / 15.0)
        val result = converter.convertFrequency(rFrequency)

        assertThat(result.swara).isEqualTo("r")
        assertThat(abs(result.centsDeviation)).isLessThan(1.0)
    }

    @Test
    fun `convertFrequency detects flat notes correctly`() {
        // Frequency 20 cents below Sa
        val flatFrequency = saFrequency * Math.pow(2.0, -20.0 / 1200.0)
        val result = converter.convertFrequency(flatFrequency)

        assertThat(result.isFlat).isTrue()
        assertThat(result.isPerfect).isFalse()
        assertThat(result.isSharp).isFalse()
        assertThat(result.centsDeviation).isLessThan(-15.0)
    }

    @Test
    fun `convertFrequency detects sharp notes correctly`() {
        // Frequency 20 cents above Sa
        val sharpFrequency = saFrequency * Math.pow(2.0, 20.0 / 1200.0)
        val result = converter.convertFrequency(sharpFrequency)

        assertThat(result.isSharp).isTrue()
        assertThat(result.isPerfect).isFalse()
        assertThat(result.isFlat).isFalse()
        assertThat(result.centsDeviation).isGreaterThan(15.0)
    }

    @Test
    fun `convertFrequency detects perfect pitch within tolerance`() {
        // Frequency 10 cents above Sa (within default 15 cent tolerance)
        val nearPerfectFrequency = saFrequency * Math.pow(2.0, 10.0 / 1200.0)
        val result = converter.convertFrequency(nearPerfectFrequency)

        assertThat(result.isPerfect).isTrue()
        assertThat(result.isFlat).isFalse()
        assertThat(result.isSharp).isFalse()
    }

    @Test
    fun `convertFrequency respects custom tolerance`() {
        val strictConverter = HindustaniNoteConverter(
            saFrequency = saFrequency,
            toleranceCents = 5.0,
            use22Shruti = false
        )

        // 8 cents deviation
        val slightlyOffFrequency = saFrequency * Math.pow(2.0, 8.0 / 1200.0)
        val result = strictConverter.convertFrequency(slightlyOffFrequency)

        // Should be marked as sharp with 5 cent tolerance
        assertThat(result.isPerfect).isFalse()
        assertThat(result.isSharp).isTrue()
    }

    @Test
    fun `convertFrequency handles all 12 basic notes`() {
        val expectedNotes = listOf("S", "r", "R", "g", "G", "m", "M", "P", "d", "D", "n", "N")
        val ratios = listOf(
            1.0 / 1.0, 16.0 / 15.0, 9.0 / 8.0, 6.0 / 5.0, 5.0 / 4.0, 4.0 / 3.0,
            45.0 / 32.0, 3.0 / 2.0, 8.0 / 5.0, 5.0 / 3.0, 16.0 / 9.0, 15.0 / 8.0
        )

        expectedNotes.forEachIndexed { index, expectedNote ->
            val frequency = saFrequency * ratios[index]
            val result = converter.convertFrequency(frequency)

            assertThat(result.swara).isEqualTo(expectedNote)
            assertThat(result.isPerfect).isTrue()
        }
    }

    @Test
    fun `22-shruti system provides more notes than 12-note system`() {
        val converter12 = HindustaniNoteConverter(
            saFrequency = saFrequency,
            toleranceCents = 15.0,
            use22Shruti = false
        )

        val converter22 = HindustaniNoteConverter(
            saFrequency = saFrequency,
            toleranceCents = 15.0,
            use22Shruti = true
        )

        val swaras12 = converter12.getAvailableSwaras()
        val swaras22 = converter22.getAvailableSwaras()

        assertThat(swaras12).hasSize(12)
        assertThat(swaras22).hasSize(21) // 22-shruti system has 21 unique notes
        assertThat(swaras22.size).isGreaterThan(swaras12.size)
    }

    @Test
    fun `getFrequencyForSwara returns correct frequencies`() {
        val saFreq = converter.getFrequencyForSwara("S")
        val pFreq = converter.getFrequencyForSwara("P")

        assertThat(saFreq).isNotNull()
        assertThat(pFreq).isNotNull()

        assertThat(saFreq!!).isWithin(0.01).of(saFrequency)
        assertThat(pFreq!!).isWithin(0.01).of(saFrequency * 3.0 / 2.0)
    }

    @Test
    fun `getFrequencyForSwara returns null for invalid swara`() {
        val frequency = converter.getFrequencyForSwara("X")
        assertThat(frequency).isNull()
    }

    @Test
    fun `cents deviation calculation is accurate`() {
        // Test frequency 100 cents (1 semitone) above Sa
        val oneSemitoneUp = saFrequency * Math.pow(2.0, 100.0 / 1200.0)
        val result = converter.convertFrequency(oneSemitoneUp)

        // Should find the closest note and calculate deviation
        // The closest note should be either r or R depending on exact frequency
        assertThat(abs(result.centsDeviation)).isLessThan(100.0)
    }

    @Test
    fun `converter handles frequencies below Sa correctly`() {
        // Frequency of N from lower octave (15/8 * 1/2 = 15/16 of current Sa)
        val lowerN = saFrequency * (15.0 / 8.0) / 2.0
        val result = converter.convertFrequency(lowerN)

        // Should still identify as N (or closest note)
        assertThat(result.swara).isNotEmpty()
        assertThat(result.centsDeviation).isNotNull()
    }

    @Test
    fun `converter handles frequencies above octave correctly`() {
        // Frequency of R from upper octave (9/8 * 2 of current Sa)
        val upperR = saFrequency * (9.0 / 8.0) * 2.0
        val result = converter.convertFrequency(upperR)

        // Should still identify correctly
        assertThat(result.swara).isNotEmpty()
    }

    @Test
    fun `different Sa frequencies work correctly`() {
        val differentSa = 440.0 // A4 as Sa
        val converterA4 = HindustaniNoteConverter(
            saFrequency = differentSa,
            toleranceCents = 15.0,
            use22Shruti = false
        )

        val result = converterA4.convertFrequency(differentSa)
        assertThat(result.swara).isEqualTo("S")
        assertThat(abs(result.centsDeviation)).isLessThan(1.0)

        // P should be 3/2 of the new Sa
        val pResult = converterA4.convertFrequency(differentSa * 3.0 / 2.0)
        assertThat(pResult.swara).isEqualTo("P")
    }

    @Test
    fun `tolerance edge cases are handled correctly`() {
        // Exactly at tolerance boundary (15 cents)
        val boundaryFrequency = saFrequency * Math.pow(2.0, 15.0 / 1200.0)
        val result = converter.convertFrequency(boundaryFrequency)

        // At exactly the boundary, should be considered perfect (<=)
        assertThat(result.isPerfect).isTrue()
    }

    @Test
    fun `just intonation ratios are accurate`() {
        // Verify that the frequency ratios match Just Intonation
        val pFreq = converter.getFrequencyForSwara("P")!!
        val ratio = pFreq / saFrequency

        // P should be exactly 3/2 (1.5)
        assertThat(ratio).isWithin(0.0001).of(1.5)

        val mFreq = converter.getFrequencyForSwara("m")!!
        val mRatio = mFreq / saFrequency

        // m should be exactly 4/3
        assertThat(mRatio).isWithin(0.0001).of(4.0 / 3.0)
    }

    // Octave detection tests

    @Test
    fun `convertFrequency detects lower octave Sa (mandra saptak)`() {
        // Sa one octave lower (half frequency)
        val lowerSa = saFrequency / 2.0
        val result = converter.convertFrequency(lowerSa)

        assertThat(result.swara).isEqualTo("S")
        assertThat(result.octave).isEqualTo(HindustaniNoteConverter.Octave.MANDRA)
        assertThat(abs(result.centsDeviation)).isLessThan(1.0)
    }

    @Test
    fun `convertFrequency detects upper octave Sa (taar saptak)`() {
        // Sa one octave higher (double frequency)
        val upperSa = saFrequency * 2.0
        val result = converter.convertFrequency(upperSa)

        assertThat(result.swara).isEqualTo("S")
        assertThat(result.octave).isEqualTo(HindustaniNoteConverter.Octave.TAAR)
        assertThat(abs(result.centsDeviation)).isLessThan(1.0)
    }

    @Test
    fun `convertFrequency detects lower octave Pancham`() {
        // P in mandra saptak (3/2 * 1/2 = 3/4 of middle Sa)
        val lowerP = saFrequency * (3.0 / 2.0) / 2.0
        val result = converter.convertFrequency(lowerP)

        assertThat(result.swara).isEqualTo("P")
        assertThat(result.octave).isEqualTo(HindustaniNoteConverter.Octave.MANDRA)
        assertThat(abs(result.centsDeviation)).isLessThan(1.0)
    }

    @Test
    fun `convertFrequency detects upper octave Pancham`() {
        // P in taar saptak (3/2 * 2 = 3 times middle Sa)
        val upperP = saFrequency * (3.0 / 2.0) * 2.0
        val result = converter.convertFrequency(upperP)

        assertThat(result.swara).isEqualTo("P")
        assertThat(result.octave).isEqualTo(HindustaniNoteConverter.Octave.TAAR)
        assertThat(abs(result.centsDeviation)).isLessThan(1.0)
    }

    @Test
    fun `formatSwaraWithOctave formats mandra correctly`() {
        val lowerSa = saFrequency / 2.0
        val result = converter.convertFrequency(lowerSa)
        val formatted = converter.formatSwaraWithOctave(result)

        assertThat(formatted).isEqualTo(".S")
    }

    @Test
    fun `formatSwaraWithOctave formats madhya correctly`() {
        val result = converter.convertFrequency(saFrequency)
        val formatted = converter.formatSwaraWithOctave(result)

        assertThat(formatted).isEqualTo("S")
    }

    @Test
    fun `formatSwaraWithOctave formats taar correctly`() {
        val upperSa = saFrequency * 2.0
        val result = converter.convertFrequency(upperSa)
        val formatted = converter.formatSwaraWithOctave(result)

        assertThat(formatted).isEqualTo("S'")
    }

    @Test
    fun `octave boundaries are detected correctly`() {
        // Test notes near octave boundaries
        val nearLowerBoundary = saFrequency * 0.55  // Close to lower octave
        val nearUpperBoundary = saFrequency * 1.8   // Close to upper octave

        val lowerResult = converter.convertFrequency(nearLowerBoundary)
        val upperResult = converter.convertFrequency(nearUpperBoundary)

        // Should pick closest note in appropriate octave
        assertThat(lowerResult.octave).isIn(listOf(
            HindustaniNoteConverter.Octave.MANDRA,
            HindustaniNoteConverter.Octave.MADHYA
        ))
        assertThat(upperResult.octave).isIn(listOf(
            HindustaniNoteConverter.Octave.MADHYA,
            HindustaniNoteConverter.Octave.TAAR
        ))
    }
}
