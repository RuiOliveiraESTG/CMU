package com.example.cmu.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cmu.data.local.AppDatabase
import com.example.cmu.data.local.AvaliacaoEntity
import com.example.cmu.data.local.PlaceEntity
import com.example.cmu.data.repository.AvaliacaoRepository
import com.example.cmu.viewmodel.AvaliacaoViewModel
import com.example.cmu.viewmodel.AvaliacaoViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    val avaliacoes by viewModel.avaliacoes.observeAsState(emptyList())
    var place by remember { mutableStateOf<PlaceEntity?>(null) }
    var comentario by remember { mutableStateOf("") }
    var estrelas by remember { mutableStateOf(0) }
    var doce by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var media by remember { mutableStateOf<Double?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> fotoUri = uri }

    LaunchedEffect(placeId) {
        val localPlace = db.placeDao().getPlaceById(placeId)
        if (localPlace != null) {
            place = localPlace
        } else {
            val placesClient = Places.createClient(context)
            val request = FetchPlaceRequest.newInstance(
                placeId,
                listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.PHONE_NUMBER
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
                        lon = fetched.latLng?.longitude ?: 0.0,
                        phone = fetched.phoneNumber
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        db.placeDao().insertPlace(entity)
                    }
                    place = entity
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro a carregar o local", Toast.LENGTH_SHORT).show()
                }
        }

        viewModel.listarUltimas(placeId)
        media = db.avaliacaoDao().getMediaEstrelas(placeId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(place?.name ?: "Detalhes") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(text = place?.address ?: "Sem endereço", modifier = Modifier.padding(8.dp))

            place?.phone?.let { phone ->
                Text(
                    text = "Telefone: $phone",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = {
                        try {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CALL_PHONE
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
                                context.startActivity(callIntent)
                            } else {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(dialIntent)
                            }
                        } catch (e: SecurityException) {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(dialIntent)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text("Ligar")
                }
            }


            Text(
                text = "⭐ Média: ${media?.let { String.format("%.1f", it) } ?: "-"}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )

            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Button(onClick = {
                    place?.let {
                        val gmmIntentUri =
                            Uri.parse("geo:${it.lat},${it.lon}?q=${it.lat},${it.lon}(${it.name})")
                        val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        context.startActivity(intent)
                    }
                }) { Text("Abrir no Google Maps") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { pickImage.launch("image/*") }) {
                    Text(if (fotoUri != null) "Trocar foto" else "Anexar foto")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Últimas avaliações",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(avaliacoes) { avaliacao ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("Utilizador: ${avaliacao.utilizador}")
                            Text("Doçaria: ${avaliacao.doce ?: "-"}")
                            Text("Estrelas: ${avaliacao.estrelas}")
                            Text(avaliacao.comentario)
                            avaliacao.fotoPath?.let { path ->
                                AsyncImage(
                                    model = path,
                                    contentDescription = "Foto",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = doce,
                onValueChange = { doce = it },
                label = { Text("Doce") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentário") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                (1..5).forEach { star ->
                    TextButton(onClick = { estrelas = star }) {
                        Text(if (star <= estrelas) "⭐" else "☆")
                    }
                }
            }

            Button(
                onClick = {
                    val user = FirebaseAuth.getInstance().currentUser ?: return@Button
                    val p = place
                    if (user == null || p == null) {
                        Toast.makeText(
                            context,
                            "Sessão inválida ou local indisponível",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (estrelas == 0) {
                        Toast.makeText(context, "Seleciona as estrelas (1–5)", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        val ultima = repo.getUltimaAvaliacao(user.uid)
                        val agora = System.currentTimeMillis()
                        if (ultima != null && (agora - ultima.timestamp) < 30 * 60 * 1000) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    context,
                                    "Só pode avaliar novamente após 30 minutos.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return@launch
                        }

                        val fused = LocationServices.getFusedLocationProviderClient(context)
                        fused.lastLocation.addOnSuccessListener { loc ->
                            if (loc == null) {
                                Toast.makeText(
                                    context,
                                    "Ative a localização para avaliar no local.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addOnSuccessListener
                            }
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                loc.latitude, loc.longitude, p.lat, p.lon, results
                            )
                            if (results[0] > 50f) {
                                Toast.makeText(
                                    context,
                                    "Tem de estar a menos de 50 metros do local.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addOnSuccessListener
                            }

                            val avaliacao = AvaliacaoEntity(
                                id = UUID.randomUUID().toString(),
                                placeId = placeId,
                                utilizador = user.uid,
                                estrelas = estrelas,
                                comentario = comentario,
                                doce = doce,
                                fotoPath = fotoUri?.toString(),
                                timestamp = System.currentTimeMillis(),
                                synced = false
                            )

                            viewModel.adicionarAvaliacao(avaliacao)
                            comentario = ""
                            estrelas = 0
                            doce = ""
                            fotoUri = null


                            CoroutineScope(Dispatchers.Main).launch {
                                media = db.avaliacaoDao().getMediaEstrelas(placeId)
                            }
                            Toast.makeText(context, "Avaliação submetida!", Toast.LENGTH_SHORT)
                                .show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Falha ao obter localização.", Toast.LENGTH_SHORT)
                                .show()
                        }
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
