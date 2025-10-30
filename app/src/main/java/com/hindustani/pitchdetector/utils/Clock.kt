package com.hindustani.pitchdetector.utils

/**
 * Interface for providing the current time in milliseconds.
 * This abstraction allows us to:
 * - Use real system time in production (SystemClock)
 * - Use controllable virtual time in tests (TestClock)
 * - Maintain timing accuracy in production while having fast tests
 */
fun interface Clock {
    /**
     * Returns the current time in milliseconds.
     * In production: returns System.currentTimeMillis()
     * In tests: returns controllable virtual time
     */
    fun now(): Long
}

/**
 * Production implementation that uses the system's real-time clock.
 * This ensures accurate timing in the actual app.
 */
class SystemClock : Clock {
    override fun now(): Long = System.currentTimeMillis()
}
