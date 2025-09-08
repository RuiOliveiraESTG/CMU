package com.example.cmu.data.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cmu.data.local.DatabaseProvider
import com.example.cmu.data.local.PlaceEntity
import com.example.cmu.data.local.PlaceRepository
import com.example.cmu.data.viewmodel.PlaceViewModel
import com.example.cmu.data.viewmodel.PlaceViewModelFactory
import com.example.cmu.data.ui.navigation.Screen
import com.example.cmu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreen(navController: NavController) {
    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)
    val repository = PlaceRepository(database.placeDao())
    val viewModel: PlaceViewModel = viewModel(
        factory = PlaceViewModelFactory(repository)
    )

    val places by viewModel.places.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPlaces(context.getString(R.string.google_maps_key))
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(places) { place ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        navController.navigate(Screen.PlaceDetail.createRoute(place.placeId))
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    place.name?.let { Text(text = it, style = MaterialTheme.typography.titleMedium) }
                    place.address?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}
