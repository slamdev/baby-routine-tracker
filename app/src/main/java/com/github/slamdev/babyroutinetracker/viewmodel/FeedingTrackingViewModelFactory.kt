package com.github.slamdev.babyroutinetracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.slamdev.babyroutinetracker.feeding.FeedingTrackingViewModel

class FeedingTrackingViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedingTrackingViewModel::class.java)) {
            return FeedingTrackingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
