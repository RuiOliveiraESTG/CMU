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
import androidx.work.*
import com.example.cmu.ui.navigation.AppScaffold
import com.example.cmu.work.ProximidadeWorker
import com.example.cmu.work.scheduleSync
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_key))
        }

        val workRequest = PeriodicWorkRequestBuilder<ProximidadeWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "proximidade_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        scheduleSync(this)

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()

                    var hasLocationPermission by remember { mutableStateOf(false) }
                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted -> hasLocationPermission = granted }

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

                    AppScaffold(
                        navController = navController,
                        hasLocationPermission = hasLocationPermission
                    )
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().signOut()
    }
}
