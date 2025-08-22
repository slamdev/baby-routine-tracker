package com.github.slamdev.babyroutinetracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.slamdev.babyroutinetracker.sleep.SleepTrackingViewModel

class SleepTrackingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepTrackingViewModel::class.java)) {
            return SleepTrackingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
