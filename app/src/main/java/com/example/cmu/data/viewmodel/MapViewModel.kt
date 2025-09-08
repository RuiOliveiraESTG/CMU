package com.example.cmu.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmu.data.local.PlaceEntity
import com.example.cmu.data.local.PlaceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(
    private val repo: PlaceRepository
) : ViewModel() {

    // Expor lista como StateFlow para Compose
    val places: StateFlow<List<PlaceEntity>> =
        repo.getPlaces()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


}
