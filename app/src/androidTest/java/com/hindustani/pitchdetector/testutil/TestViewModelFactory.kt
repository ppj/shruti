package com.hindustani.pitchdetector.testutil

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.hindustani.pitchdetector.viewmodel.FindSaViewModel
import com.hindustani.pitchdetector.viewmodel.PitchViewModel

object TestViewModelFactory {
    private val application: Application by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    }

    fun createPitchViewModel(): PitchViewModel {
        return PitchViewModel(application)
    }

    fun createFindSaViewModel(): FindSaViewModel {
        return FindSaViewModel(application)
    }
}
