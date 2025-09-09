package com.example.cmu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cmu.data.repository.PlaceRepository

class PlaceViewModelFactory(private val repo: PlaceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaceViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}