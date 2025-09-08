package com.example.cmu.data.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.cmu.data.local.PlaceEntity
import com.example.cmu.data.viewmodel.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    hasLocationPermission: Boolean,
    viewModel: MapViewModel? = null,
    onMarkerClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val places by (viewModel?.places ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val defaultLocation = LatLng(41.1579, -8.6291)
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    // Buscar localização do utilizador quando permitido
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            getUserLocation(context) { latLng ->
                userLocation = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocationPermission)
        ) {
            // Marcador da localização do utilizador
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Está aqui!"
                )
            }

            places.forEach { est: PlaceEntity ->
                Marker(
                    state = MarkerState(position = LatLng(est.lat, est.lon)),
                    title = est.name,
                    onClick = {
                        onMarkerClick(est.placeId)
                        false
                    }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getUserLocation(context: Context, onLocation: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocation(LatLng(location.latitude, location.longitude))
        }
    }
}
