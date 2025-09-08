package com.example.cmu.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmu.data.local.PlaceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaceViewModel(private val repository: PlaceRepository) : ViewModel() {

    val places = repository.getPlaces()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun loadPlaces(apiKey: String) {
        viewModelScope.launch {
            repository.fetchAndSavePlaces(apiKey)
        }
    }
}
