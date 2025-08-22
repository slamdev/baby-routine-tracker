package com.github.slamdev.babyroutinetracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.slamdev.babyroutinetracker.diaper.DiaperTrackingViewModel

class DiaperTrackingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaperTrackingViewModel::class.java)) {
            return DiaperTrackingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
