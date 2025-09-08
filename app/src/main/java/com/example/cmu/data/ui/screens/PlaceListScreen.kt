package com.example.cmu.data.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cmu.data.local.DatabaseProvider
import com.example.cmu.data.local.PlaceRepository
import com.example.cmu.data.viewmodel.PlaceViewModel
import com.example.cmu.data.viewmodel.PlaceViewModelFactory
import com.example.cmu.data.ui.navigation.Screen
import com.example.cmu.R
import com.google.android.gms.location.LocationServices

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
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // obter localização
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location.latitude to location.longitude
                    viewModel.loadPlaces(
                        context.getString(R.string.google_maps_key),
                        location.latitude,
                        location.longitude
                    )
                } else {
                    // fallback (Porto)
                    userLocation = 41.1579 to -8.6291
                    viewModel.loadPlaces(
                        context.getString(R.string.google_maps_key),
                        41.1579,
                        -8.6291
                    )
                }
            }
        } else {
            // ainda não tens permissão → fallback
            userLocation = 41.1579 to -8.6291
            viewModel.loadPlaces(
                context.getString(R.string.google_maps_key),
                41.1579,
                -8.6291
            )
        }
    }

    // filtrar por distância (< 1 km)
    val filteredPlaces = remember(places, userLocation) {
        if (userLocation == null) emptyList()
        else {
            val (lat, lon) = userLocation!!
            places.filter { place ->
                val distance = haversine(lat, lon, place.lat, place.lon)
                distance < 1.0 // só lugares a menos de 1 km
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(filteredPlaces) { place ->
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

// Função Haversine para calcular distância entre 2 pontos em km
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371 // raio da Terra em km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}
