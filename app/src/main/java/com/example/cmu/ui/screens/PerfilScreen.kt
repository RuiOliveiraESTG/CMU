package com.example.cmu.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmu.R
import com.example.cmu.data.local.AppDatabase
import com.example.cmu.data.local.UserEntity
import com.example.cmu.data.repository.UserRepository
import com.example.cmu.viewmodel.UserViewModel
import com.example.cmu.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repo = UserRepository(db.userDao())
    val viewModel: UserViewModel = viewModel(factory = UserViewModelFactory(repo))

    val user = FirebaseAuth.getInstance().currentUser
    var userEntity by remember { mutableStateOf<UserEntity?>(null) }
    var novoNome by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        if (user != null) {
            viewModel.saveUser(user)
            userEntity = viewModel.getUser(user.uid)
            novoNome = userEntity?.name.orEmpty()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Meu Perfil") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userEntity != null) {
                AsyncImage(
                    model = userEntity!!.photoUrl,
                    contentDescription = "Foto de perfil",
                    placeholder = painterResource(R.drawable.ic_profile_placeholder),
                    error = painterResource(R.drawable.ic_profile_placeholder),
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                Text(
                    text = userEntity!!.email ?: "Sem email",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Editar nome
                OutlinedTextField(
                    value = novoNome,
                    onValueChange = { novoNome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (novoNome.isNotBlank()) {
                            scope.launch {
                                val atualizado = userEntity!!.copy(name = novoNome, synced = false)
                                viewModel.updateUser(atualizado)
                                userEntity = atualizado
                                Toast.makeText(context, "Nome atualizado!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Alterações")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logout
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            } else {
                Text("Não autenticado", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
