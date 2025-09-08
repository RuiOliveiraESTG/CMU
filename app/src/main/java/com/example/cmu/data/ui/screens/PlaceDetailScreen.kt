package com.example.cmu.data.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmu.data.local.AppDatabase
import com.example.cmu.data.local.AvaliacaoEntity
import com.example.cmu.data.local.PlaceEntity
import com.example.cmu.data.repository.AvaliacaoRepository
import com.example.cmu.viewmodel.AvaliacaoViewModel
import com.example.cmu.viewmodel.AvaliacaoViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    navController: NavController,
    placeId: String
) {
    // Criar repo e factory para ViewModel
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repo = AvaliacaoRepository(db.avaliacaoDao())
    val factory = AvaliacaoViewModelFactory(repo)
    val viewModel: AvaliacaoViewModel = viewModel(factory = factory)

    var place by remember { mutableStateOf<PlaceEntity?>(null) }
    var avaliacoes by remember { mutableStateOf<List<AvaliacaoEntity>>(emptyList()) }
    var comentario by remember { mutableStateOf("") }
    var estrelas by remember { mutableStateOf(0) }

    // Carregar últimas 10 avaliações
    LaunchedEffect(placeId) {
        place = db.placeDao().getPlaceById(placeId)
        viewModel.listarUltimas(placeId) { avaliacoes = it }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(place?.name ?: "Detalhes") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(text = place?.address ?: "Sem endereço")
            Spacer(Modifier.height(12.dp))

            Text(
                "Últimas Avaliações:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(avaliacoes) { avaliacao ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "Utilizador: " + avaliacao.utilizador)
                            Text(text = "Estrelas: " + avaliacao.estrelas.toString())
                            Text(text = avaliacao.comentario)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentário") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Row(modifier = Modifier.padding(8.dp)) {
                (1..5).forEach { star ->
                    TextButton(onClick = { estrelas = star }) {
                        Text(if (star <= estrelas) "⭐" else "☆")
                    }
                }
            }

            Button(
                onClick = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user == null) {
                        // não autenticado -> ir para ecrã de login
                        navController.navigate("login")
                        return@Button
                    }

                    val avaliacao = AvaliacaoEntity(
                        placeId = placeId,
                        utilizador = user.uid,
                        estrelas = estrelas,
                        comentario = comentario,
                        fotoPath = null,
                        timestamp = System.currentTimeMillis(),
                        synced = false
                    )

                    viewModel.adicionarAvaliacao(avaliacao)
                    comentario = ""
                    estrelas = 0

                    // refrescar lista
                    viewModel.listarUltimas(placeId) { avaliacoes = it }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Enviar Avaliação")
            }
        }
    }
}
