package com.example.cmu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cmu.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user != null) {
            Text("Bem-vindo, ${user.email ?: "Utilizador"}")

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { navController.navigate(Screen.Map.route) }) {
                Text("Ver Mapa")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { navController.navigate(Screen.PlaceList.route) }) {
                Text("Ver Lista de Estabelecimentos")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { navController.navigate(Screen.Leaderboard.route) }) {
                Text("Leaderboard")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { navController.navigate(Screen.Historico.route) }) {
                Text("Histórico de Avaliações")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { navController.navigate(Screen.Perfil.route) }) {
                Text("Perfil")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            ) {
                Text("Logout")
            }

        } else {
            Text("Ainda não tem sessão iniciada")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { navController.navigate(Screen.Login.route) }) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigate(Screen.Register.route) }) {
                Text("Register")
            }
        }
    }
}



