#!/usr/bin/env python3
"""
Generate pre-recorded OGG Vorbis files for tanpura playback.
Creates 60 files: 4 String 1 notes (P, M, S, N) × 15 Sa values (G#2 to A#3)
"""

import numpy as np
from pathlib import Path
import soundfile as sf

# Audio configuration
SAMPLE_RATE = 44100
SUSTAIN_DURATION = 8.0  # seconds per string
BEAT_INTERVAL = 0.6  # 100 BPM
CYCLE_BEATS = 6
CYCLE_DURATION = BEAT_INTERVAL * CYCLE_BEATS

# Just Intonation ratios
NOTE_RATIOS = {
    "S": 1.0,
    "r": 16.0 / 15.0,
    "R": 9.0 / 8.0,
    "g": 6.0 / 5.0,
    "G": 5.0 / 4.0,
    "m": 4.0 / 3.0,
    "M": 45.0 / 32.0,
    "P": 3.0 / 2.0,
    "d": 8.0 / 5.0,
    "D": 5.0 / 3.0,
    "n": 16.0 / 9.0,
    "N": 15.0 / 8.0
}

# Sa values from G#2 to A#3 (15 semitones)
SA_FREQUENCIES = {
    "gs2": 103.83,
    "a2": 110.00,
    "as2": 116.54,
    "b2": 123.47,
    "c3": 130.81,
    "cs3": 138.59,
    "d3": 146.83,
    "ds3": 155.56,
    "e3": 164.81,
    "f3": 174.61,
    "fs3": 185.00,
    "g3": 196.00,
    "gs3": 207.65,
    "a3": 220.00,
    "as3": 233.08
}

# String 1 notes to generate (most common)
STRING1_NOTES = ["P", "M", "S", "N"]

# Tanpura harmonic structure with jawari effect
HARMONICS = [
    (1.0, 0.55),   # Fundamental (weaker than key harmonics)
    (2.0, 0.85),   # Octave
    (3.0, 0.75),   # Fifth
    (4.0, 1.0),    # DOMINANT (jawari)
    (5.0, 0.65),   # Major third
    (6.0, 0.58),   # Fifth + octave
    (7.0, 0.95),   # DOMINANT (jawari)
    (8.0, 0.48),   # Triple octave
    (9.0, 0.42),   # Major ninth
    (10.0, 0.38),  # Major third + octave
    (11.0, 0.85),  # DOMINANT (jawari)
    (12.0, 0.32),  # Fifth + double octave
    (13.0, 0.28),  # Thirteenth
    (14.0, 0.25),  # Minor seventh + octave
    (15.0, 0.22),  # Major seventh + octave
    (16.0, 0.18),  # Quadruple octave
    (17.0, 0.75),  # DOMINANT (jawari)
    (18.0, 0.15),  # Ninth + octave
    (19.0, 0.12),  # Nineteenth
    (20.0, 0.10)   # Twentieth
]


def generate_string_pluck(frequency, duration, amplitude_variation=1.0, attack_duration=0.8, volume=0.5):
    """
    Generate a single string pluck with realistic tanpura timbre using additive synthesis.
    """
    num_samples = int(SAMPLE_RATE * duration)
    samples = np.zeros(num_samples)

    # Time array
    t = np.arange(num_samples) / SAMPLE_RATE

    # Envelope: S-curve attack then exponential decay
    envelope = np.where(
        t < attack_duration,
        1.0 / (1.0 + np.exp(-12.0 * (t / attack_duration - 0.5))),  # Sigmoid attack
        np.exp(-t * 0.18)  # Slow decay for 8s sustain
    )

    # Inharmonicity coefficient (constant for all frequencies)
    inharmonicity_coeff = 0.0002

    # Generate harmonics with additive synthesis
    for harmonic_num, amplitude in HARMONICS:
        # Add inharmonicity: harmonics are slightly sharp
        inharmonic_factor = np.sqrt(1.0 + inharmonicity_coeff * harmonic_num * harmonic_num)
        harmonic_freq = frequency * harmonic_num * inharmonic_factor

        # Phase
        phase = 2.0 * np.pi * harmonic_freq * t

        # Quadratic frequency-dependent damping
        harmonic_decay = np.exp(-t * (0.15 + harmonic_num * harmonic_num * 0.004))

        # Per-harmonic amplitude modulation (waxing/waning)
        modulation_freq = 1.5 + (harmonic_num * 0.15)
        modulation_phase = harmonic_num * 0.4
        modulation_depth = 0.12 * np.exp(-t * 0.8)
        harmonic_modulation = 1.0 + modulation_depth * np.sin(2.0 * np.pi * modulation_freq * t + modulation_phase)

        # Phase variation per harmonic
        harmonic_phase_shift = np.sin(harmonic_num * 0.7) * 0.05

        # Add this harmonic to the sample
        samples += amplitude * harmonic_decay * harmonic_modulation * np.sin(phase + harmonic_phase_shift)

    # Apply envelope, amplitude variation, and volume
    amplitude_sum = sum(amp for _, amp in HARMONICS)
    samples *= envelope * amplitude_variation * volume / amplitude_sum

    # Soft clipping for natural saturation
    samples = np.clip(samples, -1.0, 1.0)

    # Scale to fuller amplitude
    samples *= 0.8

    return samples


def generate_tanpura_cycle(sa_frequency, string1_note):
    """
    Generate one complete 6-beat tanpura cycle as stereo audio.
    """
    # Calculate string frequencies
    string1_freq = sa_frequency * NOTE_RATIOS[string1_note] / 2.0  # Lower octave
    string2_freq = sa_frequency  # Tonic Sa
    string3_freq = sa_frequency  # Tonic Sa
    string4_freq = sa_frequency / 2.0  # Lower octave Sa

    # Generate individual strings with different attack durations
    print(f"  Generating String 1 ({string1_note})...")
    string1_samples = generate_string_pluck(string1_freq, SUSTAIN_DURATION, amplitude_variation=0.98, attack_duration=0.4)

    print(f"  Generating String 2 (Sa)...")
    string2_samples = generate_string_pluck(string2_freq, SUSTAIN_DURATION, amplitude_variation=1.0, attack_duration=0.8)

    print(f"  Generating String 3 (Sa)...")
    string3_samples = generate_string_pluck(string3_freq, SUSTAIN_DURATION, amplitude_variation=1.0, attack_duration=0.8)

    print(f"  Generating String 4 (lower Sa)...")
    string4_samples = generate_string_pluck(string4_freq, SUSTAIN_DURATION, amplitude_variation=0.96, attack_duration=0.6)

    # Plucking pattern offsets (in beats)
    pluck_offsets = [0, 2, 3, 4]  # String1, String2, String3, String4
    all_strings = [string1_samples, string2_samples, string3_samples, string4_samples]

    # Create mono buffer for one complete cycle
    beat_interval_samples = int(SAMPLE_RATE * BEAT_INTERVAL)
    cycle_size = int(SAMPLE_RATE * CYCLE_DURATION)
    mono_buffer = np.zeros(cycle_size)

    # Mix all strings with their timing offsets
    for string_index, string_samples in enumerate(all_strings):
        offset = pluck_offsets[string_index] * beat_interval_samples

        for i in range(len(string_samples)):
            # Wrap around if string extends beyond cycle boundary
            buffer_index = (offset + i) % cycle_size
            mono_buffer[buffer_index] += string_samples[i]

    # Normalize mono buffer (leave headroom)
    max_amp = np.max(np.abs(mono_buffer))
    if max_amp > 0.85:
        mono_buffer *= 0.85 / max_amp

    # Convert mono to stereo with timing offset and panning
    stereo_timing_offset = int(SAMPLE_RATE * 0.012)  # 12ms delay for R channel
    panning_l = 0.75  # 75% left
    panning_r = 0.75  # 75% right

    stereo_buffer = np.zeros((cycle_size, 2))

    for i in range(cycle_size):
        # Left channel
        stereo_buffer[i, 0] = mono_buffer[i] * panning_l

        # Right channel with timing offset
        right_index = (i + stereo_timing_offset) % cycle_size
        stereo_buffer[i, 1] = mono_buffer[right_index] * panning_r

    # No fade-in or fade-out needed - the modulo wrapping in mono_buffer creation
    # already creates seamless loops. String 4's 8s sustain naturally wraps around
    # and overlaps with String 1 at the loop point.

    return stereo_buffer


def main():
    """Generate all tanpura OGG files."""
    # Create output directory
    output_dir = Path(__file__).parent.parent / "app" / "src" / "main" / "assets" / "tanpura"
    output_dir.mkdir(parents=True, exist_ok=True)

    print(f"Generating tanpura files in: {output_dir}")
    print(f"Total files to generate: {len(SA_FREQUENCIES)} Sa values × {len(STRING1_NOTES)} String 1 notes = {len(SA_FREQUENCIES) * len(STRING1_NOTES)}")
    print()

    file_count = 0
    total_files = len(SA_FREQUENCIES) * len(STRING1_NOTES)

    # Generate all combinations
    for sa_name, sa_freq in SA_FREQUENCIES.items():
        for string1_note in STRING1_NOTES:
            file_count += 1
            filename = f"{sa_name}_{string1_note}.ogg"
            filepath = output_dir / filename

            print(f"[{file_count}/{total_files}] Generating {filename}...")
            print(f"  Sa = {sa_freq:.2f} Hz, String 1 = {string1_note}")

            # Generate the audio
            audio_data = generate_tanpura_cycle(sa_freq, string1_note)

            # Save as OGG Vorbis
            print(f"  Writing to {filename}...")
            sf.write(filepath, audio_data, SAMPLE_RATE, format='OGG', subtype='VORBIS')

            print(f"  ✓ Complete\n")

    print(f"\n{'='*60}")
    print(f"Generation complete! {total_files} files created.")
    print(f"Output directory: {output_dir}")
    print(f"{'='*60}")


if __name__ == "__main__":
    main()
