package com.example.cmu.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cmu.data.local.AppDatabase
import com.example.cmu.data.local.PlaceEntity
import com.example.cmu.data.model.LeaderboardItem
import com.example.cmu.data.repository.AvaliacaoRepository
import com.example.cmu.ui.navigation.Screen
import com.example.cmu.viewmodel.AvaliacaoViewModel
import com.example.cmu.viewmodel.AvaliacaoViewModelFactory
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repo = AvaliacaoRepository(db.avaliacaoDao())
    val viewModel: AvaliacaoViewModel = viewModel(factory = AvaliacaoViewModelFactory(repo))

    var leaderboard by remember { mutableStateOf<List<LeaderboardItem>>(emptyList()) }
    var nomesPlaces by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        viewModel.leaderboard { items ->
            leaderboard = items

            items.forEach { item ->
                launch {
                    val localPlace = db.placeDao().getPlaceById(item.placeId)
                    if (localPlace != null) {
                        nomesPlaces = nomesPlaces + (item.placeId to localPlace.name.orEmpty())
                    } else {
                        val placesClient = Places.createClient(context)

                        val request = FetchPlaceRequest.newInstance(
                            item.placeId,
                            listOf(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.LAT_LNG
                            )
                        )
                        placesClient.fetchPlace(request)
                            .addOnSuccessListener { response ->
                                val fetched = response.place
                                val entity = PlaceEntity(
                                    placeId = fetched.id!!,
                                    name = fetched.name,
                                    address = fetched.address,
                                    lat = fetched.latLng?.latitude ?: 0.0,
                                    lon = fetched.latLng?.longitude ?: 0.0
                                )
                                CoroutineScope(Dispatchers.IO).launch {
                                    db.placeDao().insertPlace(entity)
                                }
                                nomesPlaces = nomesPlaces + (item.placeId to (fetched.name ?: item.placeId))
                            }
                    }
                }
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Leaderboard") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(leaderboard) { item ->
                val place by db.placeDao().observePlaceById(item.placeId)
                    .collectAsState(initial = null)

                LaunchedEffect(place) {
                    if (place == null) {
                        val placesClient = Places.createClient(context)
                        val request = FetchPlaceRequest.newInstance(
                            item.placeId,
                            listOf(
                                Place.Field.ID,
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.LAT_LNG
                            )
                        )
                        placesClient.fetchPlace(request).addOnSuccessListener { response ->
                            val fetched = response.place
                            val entity = PlaceEntity(
                                placeId = fetched.id!!,
                                name = fetched.name,
                                address = fetched.address,
                                lat = fetched.latLng?.latitude ?: 0.0,
                                lon = fetched.latLng?.longitude ?: 0.0
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                db.placeDao().insertPlace(entity)
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { navController.navigate(Screen.PlaceDetail.createRoute(item.placeId)) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Estabelecimento: ${place?.name ?: " "}")
                        Text("⭐ Média: %.1f".format(item.media))
                        Text("Total avaliações: ${item.total}")
                    }
                }
            }
        }

    }
}
