package com.example.cmu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.cmu.data.sync.scheduleSync
import com.example.cmu.data.ui.navigation.AppNavGraph
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //sync do room com o firestore
        scheduleSync(this)

        // Inicializar o Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()

                    var hasLocationPermission by remember { mutableStateOf(false) }

                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        hasLocationPermission = granted
                    }

                    LaunchedEffect(Unit) {
                        val granted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (granted) {
                            hasLocationPermission = true
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }

                    // 🔑 Passamos a flag aqui
                    AppNavGraph(
                        navController = navController,
                        hasLocationPermission = hasLocationPermission
                    )
                }
            }
        }
    }
}
