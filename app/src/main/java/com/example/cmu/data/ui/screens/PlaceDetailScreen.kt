package com.example.cmu.data.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
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
import com.example.cmu.data.local.AppDatabase
import com.example.cmu.data.local.AvaliacaoEntity
import com.example.cmu.data.local.PlaceEntity
import com.example.cmu.data.repository.AvaliacaoRepository
import com.example.cmu.data.viewmodel.AvaliacaoViewModel
import com.example.cmu.data.viewmodel.AvaliacaoViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    navController: NavController,
    placeId: String
) {
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
                            Text(text = "Utilizador: ${avaliacao.utilizador}")
                            Text(text = "Estrelas: ${avaliacao.estrelas}")
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
                        navController.navigate("login")
                        return@Button
                    }

                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null && place != null) {
                                // calcular distância
                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(
                                    location.latitude, location.longitude,
                                    place!!.lat, place!!.lon,
                                    results
                                )
                                val distancia = results[0]

                                if (distancia > 50) {
                                    Toast.makeText(
                                        context,
                                        "Tem de estar a menos de 50m para avaliar!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@addOnSuccessListener
                                }

                                // verificar última avaliação do utilizador
                                viewModel.verificarUltimaAvaliacao(user.uid) { ultima ->
                                    val agora = System.currentTimeMillis()
                                    if (ultima != null && agora - ultima.timestamp < 30 * 60 * 1000) {
                                        Toast.makeText(
                                            context,
                                            "Só pode avaliar de 30 em 30 minutos!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@verificarUltimaAvaliacao
                                    }

                                    // tudo OK → guardar avaliação
                                    val avaliacao = AvaliacaoEntity(
                                        id = UUID.randomUUID().toString(),
                                        placeId = placeId,
                                        utilizador = user.uid,
                                        estrelas = estrelas,
                                        comentario = comentario,
                                        fotoPath = null,
                                        timestamp = agora,
                                        synced = false
                                    )

                                    viewModel.adicionarAvaliacao(avaliacao)
                                    comentario = ""
                                    estrelas = 0

                                    // refrescar lista
                                    viewModel.listarUltimas(placeId) { avaliacoes = it }
                                }
                            }
                        }
                    }
                    else{
                        Toast.makeText(context, "Sem permissão de localização", Toast.LENGTH_LONG).show()
                    }
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
