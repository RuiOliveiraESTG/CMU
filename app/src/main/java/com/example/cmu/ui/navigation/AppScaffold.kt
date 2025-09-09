package com.example.cmu.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavHostController,
    hasLocationPermission: Boolean
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items = listOf(
        Screen.Home,
        Screen.Map,
        Screen.PlaceList,
        Screen.Leaderboard,
        Screen.Historico,
        Screen.Perfil
    )

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val noSidebarScreens = listOf(Screen.Login.route, Screen.Register.route)

    val currentScreen = listOf(
        Screen.Home,
        Screen.Map,
        Screen.PlaceList,
        Screen.Leaderboard,
        Screen.Historico,
        Screen.Perfil,
        Screen.Login,
        Screen.Register,
        Screen.PlaceDetail
    ).find { screen ->
        currentRoute?.startsWith(screen.route.substringBefore("/{")) == true
    }

    if (currentRoute in noSidebarScreens) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(currentScreen?.title ?: "CMU") })
            }
        ) { innerPadding ->
            AppNavGraph(
                navController = navController,
                hasLocationPermission = hasLocationPermission,
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    items.forEach { screen ->
                        val isSelected =
                            currentRoute?.startsWith(screen.route.substringBefore("/{")) == true

                        NavigationDrawerItem(
                            label = { Text(screen.title) },
                            selected = isSelected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentScreen?.title ?: "CMU") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                AppNavGraph(
                    navController = navController,
                    hasLocationPermission = hasLocationPermission,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

