package com.example.cmu.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmu.R
import com.example.cmu.data.local.DatabaseProvider
import com.example.cmu.data.repository.PlaceRepository
import com.example.cmu.viewmodel.PlaceViewModel
import com.example.cmu.viewmodel.PlaceViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    hasLocationPermission: Boolean,
    onMarkerClick: (String) -> Unit
) {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val repo = PlaceRepository(db.placeDao())
    val viewModel: PlaceViewModel = viewModel(factory = PlaceViewModelFactory(repo))

    val places by viewModel.places.collectAsState()
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            val fine = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fine) {
                fused.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLocation = loc.latitude to loc.longitude
                        viewModel.loadPlaces(
                            context.getString(R.string.google_maps_key),
                            loc.latitude,
                            loc.longitude
                        )
                    } else {
                        userLocation = 41.1579 to -8.6291
                        viewModel.loadPlaces(
                            context.getString(R.string.google_maps_key),
                            41.1579, -8.6291
                        )
                    }
                }
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        userLocation?.let { (lat, lon) ->
            position = CameraPosition.fromLatLngZoom(LatLng(lat, lon), 12f)
        }
    }

    LaunchedEffect(places, userLocation) {
        if ((places.isNotEmpty() || userLocation != null) && cameraPositionState.isMoving.not()) {
            val builder = LatLngBounds.Builder()

            userLocation?.let { (lat, lon) ->
                builder.include(LatLng(lat, lon))
            }
            places.forEach { place ->
                builder.include(LatLng(place.lat, place.lon))
            }

            val bounds = builder.build()
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = true),
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
    ) {
        userLocation?.let { (lat, lon) ->
            Marker(
                state = MarkerState(position = LatLng(lat, lon)),
                title = "Você está aqui",
                snippet = "Localização atual"
            )
        }

        places.forEach { place ->
            Marker(
                state = MarkerState(position = LatLng(place.lat, place.lon)),
                title = place.name ?: "Estabelecimento",
                snippet = place.address ?: "",
                onClick = {
                    onMarkerClick(place.placeId)
                    true
                }
            )
        }
    }
}
