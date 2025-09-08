package com.example.cmu.data.ui.screens

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
import com.example.cmu.data.model.LeaderboardItem
import com.example.cmu.data.repository.AvaliacaoRepository
import com.example.cmu.data.ui.navigation.Screen
import com.example.cmu.data.viewmodel.AvaliacaoViewModel
import com.example.cmu.data.viewmodel.AvaliacaoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repo = AvaliacaoRepository(db.avaliacaoDao())
    val viewModel: AvaliacaoViewModel = viewModel(factory = AvaliacaoViewModelFactory(repo))

    var leaderboard by remember { mutableStateOf<List<LeaderboardItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.leaderboard { leaderboard = it }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Leaderboard") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(leaderboard) { item ->
                var placeName by remember { mutableStateOf("Carregando...") }

                // Buscar nome do restaurante
                LaunchedEffect(item.placeId) {
                    val place = db.placeDao().getPlaceById(item.placeId)
                    placeName = place?.name ?: "Desconhecido"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            navController.navigate(Screen.PlaceDetail.createRoute(item.placeId))
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Estabelecimento: $placeName")
                        Text("⭐ Média: %.1f".format(item.media))
                        Text("Total avaliações: ${item.total}")
                    }
                }
            }
        }
    }
}
