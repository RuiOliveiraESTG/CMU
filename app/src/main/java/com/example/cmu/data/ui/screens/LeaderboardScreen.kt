package com.example.cmu.data.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cmu.data.local.AppDatabase
import com.example.cmu.data.model.LeaderboardItem
import com.example.cmu.data.repository.AvaliacaoRepository
import com.example.cmu.viewmodel.AvaliacaoViewModel
import com.example.cmu.viewmodel.AvaliacaoViewModelFactory

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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Place ID: ${item.placeId}")
                        Text("⭐ Média: %.1f".format(item.media))
                        Text("Total avaliações: ${item.total}")
                    }
                }
            }
        }
    }
}
