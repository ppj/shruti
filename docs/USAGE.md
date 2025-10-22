# Usage Guide

This document provides a detailed guide on how to use the Shruti app.

## Initial Setup
1. **Grant Microphone Permission**: App will request microphone access on first launch
2. **Set Your Sa (Tonic)**: Go to Settings → Enter your Sa in Western notation (e.g., C4, D3, A#3) or use the "Find My Sa" feature to automatically detect it.
3. **Adjust Tolerance**: Set tolerance based on your skill level:
   - Expert: ±5-8 cents
   - Intermediate: ±10-15 cents
   - Beginner: ±20-30 cents

## Pitch Detection Mode
1. **Start Detection**: Tap the green "Start" button
2. **Sing a Note**: The app will display:
   - Current swar (S, R, G, etc.)
   - Cents deviation from perfect pitch
   - Visual needle indicator
   - Color-coded status (Perfect/Flat/Sharp)
3. **Adjust Your Pitch**: Use the visual feedback to correct your pitch
4. **Stop Detection**: Tap the red "Stop" button when done

## Voice Training Mode

The Voice Training Mode offers a structured approach to improve your pitch accuracy through a 4-level progression system.

1.  **Select a Level**: Choose from 4 difficulty levels, each with a specific set of swars and progression rules.
    *   **Level 1 (7 Shuddha Swars - Sequential)**: Focuses on the 7 pure (shuddha) swars, presented in a sequential order.
    *   **Level 2 (7 Shuddha Swars - Random)**: Practices the 7 shuddha swars in a random order.
    *   **Level 3 (12 Swars - Sequential)**: Introduces all 12 swars (including komal and teevra variations) in a sequential order.
    *   **Level 4 (12 Swars - Random)**: Practices all 12 swars in a random order.
2.  **Follow the Target Swar**: The app will display a target swar. Your goal is to sing and hold this swar accurately.
3.  **Real-time Feedback**:
    *   **Green**: You are singing the correct swar within the set tolerance.
    *   **Blue (Flat)**: You are singing the correct swar but are slightly flat (below the tolerance).
    *   **Red (Sharp)**: You are singing the correct swar but are slightly sharp (above the tolerance).
    *   **No Color/Incorrect Swar**: You are singing an incorrect swar or are significantly off-pitch.
4.  **Progression**: Successfully holding a swar for a set duration will advance you to the next swar in the sequence or randomly selected swar. Complete all swars in a level to progress.

## Understanding the Display

### Pitch Indicator
- **Needle Position**: Shows pitch deviation (-50 to +50 cents)
- **Green Zone**: Within tolerance (perfect pitch)
- **Needle Color**:
  - 🟢 Green: Perfect (within tolerance)
  - 🔵 Blue: Flat (below tolerance)
  - 🔴 Red: Sharp (above tolerance)

### Note Display
- **Large Swar**: Current detected note in Hindustani notation with octave
  - **Mandra Saptak** (lower octave): `.S` `.R` `.G` `.P` (dot prefix)
  - **Madhya Saptak** (middle octave): `S` `R` `G` `P` (plain)
  - **Taar Saptak** (upper octave): `S'` `R'` `G'` `P'` (apostrophe suffix)
- **Cents Deviation**: Exact deviation in cents (+/-)
- **Confidence**: Algorithm confidence level (shown during recording)

**Example**:
- Sing low Sa → Shows `.S` (mandra)
- Sing middle Sa → Shows `S` (madhya)
- Sing high Sa → Shows `S'` (taar)

## Tuning Systems

### 12-Note Just Intonation (Default)
Standard Hindustani notes with pure frequency ratios:
- S (1/1), r (16/15), R (9/8), g (6/5), G (5/4), m (4/3), M (45/32)
- P (3/2), d (8/5), D (5/3), n (16/9), N (15/8)

### 22-Shruti System (Advanced)
Microtonal variations for advanced musicians:
- Includes intermediate shrutis between main notes
- Marked with superscript numbers (e.g., r¹, r²)
- Useful for subtle raga nuances
