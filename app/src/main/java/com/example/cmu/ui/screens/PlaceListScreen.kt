package com.example.cmu.ui.screens

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
import com.example.cmu.R
import com.example.cmu.data.local.DatabaseProvider
import com.example.cmu.data.repository.PlaceRepository
import com.example.cmu.ui.navigation.Screen
import com.example.cmu.viewmodel.PlaceViewModel
import com.example.cmu.viewmodel.PlaceViewModelFactory
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreen(navController: NavController) {
    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)
    val repository = PlaceRepository(database.placeDao())
    val viewModel: PlaceViewModel = viewModel(factory = PlaceViewModelFactory(repository))

    val places by viewModel.places.collectAsState()
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location.latitude to location.longitude
                    viewModel.loadPlaces(
                        context.getString(R.string.google_maps_key),
                        location.latitude,
                        location.longitude
                    )
                } else {
                    userLocation = 41.1579 to -8.6291
                    viewModel.loadPlaces(
                        context.getString(R.string.google_maps_key),
                        41.1579, -8.6291
                    )
                }
            }
        } else {
            userLocation = 41.1579 to -8.6291
            viewModel.loadPlaces(
                context.getString(R.string.google_maps_key),
                41.1579, -8.6291
            )
        }
    }

    val ordered = remember(places, userLocation) {
        userLocation?.let { (ulat, ulon) ->
            places.sortedBy { haversine(ulat, ulon, it.lat, it.lon) }
        } ?: places
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(ordered) { place ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        navController.navigate(Screen.PlaceDetail.createRoute(place.placeId))
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    place.name?.let {
                        Text(text = it, style = MaterialTheme.typography.titleMedium)
                    }
                    place.address?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                    }
                    userLocation?.let { (ulat, ulon) ->
                        val km = haversine(ulat, ulon, place.lat, place.lon)
                        Text(
                            text = String.format("Distância: %.2f km", km),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c
}
