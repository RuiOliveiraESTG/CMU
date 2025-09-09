package com.example.cmu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cmu.ui.screens.HistoricoScreen
import com.example.cmu.ui.screens.HomeScreen
import com.example.cmu.ui.screens.LeaderboardScreen
import com.example.cmu.ui.screens.LoginScreen
import com.example.cmu.ui.screens.MapScreen
import com.example.cmu.ui.screens.PerfilScreen
import com.example.cmu.ui.screens.PlaceDetailScreen
import com.example.cmu.ui.screens.PlaceListScreen
import com.example.cmu.ui.screens.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Início")
    object PlaceList : Screen("place_list", "Lista de Locais")
    object PlaceDetail : Screen("place_detail/{placeId}", "Detalhes") {
        fun createRoute(placeId: String) = "place_detail/$placeId"
    }
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Criar Conta")
    object Map : Screen("map", "Mapa")
    object Leaderboard : Screen("leaderboard", "Ranking")
    object Historico : Screen("historico", "Histórico")
    object Perfil : Screen("perfil", "Perfil")
}


@Composable
fun AppNavGraph(
    navController: NavHostController,
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }

        composable(Screen.Map.route) {
            MapScreen(
                hasLocationPermission = hasLocationPermission,
                onMarkerClick = { placeId ->
                    navController.navigate("place_detail/$placeId")
                }
            )
        }

        composable(Screen.PlaceList.route) {
            PlaceListScreen(navController)
        }

        composable(Screen.PlaceDetail.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")
            placeId?.let { PlaceDetailScreen(navController, it) }
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(navController)
        }

        composable(Screen.Historico.route) {
            HistoricoScreen(navController)
        }

        composable(Screen.Perfil.route) {
            PerfilScreen(navController)
        }
    }
}
