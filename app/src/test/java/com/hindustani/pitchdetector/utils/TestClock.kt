package com.hindustani.pitchdetector.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * Test implementation of Clock that uses the virtual time from TestCoroutineScheduler.
 * This allows tests to control time progression and run much faster than real-time.
 *
 * @param scheduler The TestCoroutineScheduler that manages virtual time in tests
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestClock(private val scheduler: TestCoroutineScheduler) : Clock {
    override fun now(): Long = scheduler.currentTime
}
