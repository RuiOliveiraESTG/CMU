package com.example.cmu.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cmu.R
import com.example.cmu.data.local.AppDatabase
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class ProximidadeWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour !in 16..18) return Result.success()

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // ⚠️ check explícito
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val location = try {
            fusedLocationClient.lastLocation.await()
        } catch (e: SecurityException) {
            return Result.success()
        } ?: return Result.success()

        val db = AppDatabase.getDatabase(context)
        val places = db.placeDao().getAllPlacesOnce()

        for (place in places) {
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude, location.longitude,
                place.lat, place.lon,
                results
            )
            val distancia = results[0]

            if (distancia <= 50) {
                enviarNotificacao(
                    "Estas perto de ${place.name}",
                    "Deixa uma avaliação"
                )
                break
            }
        }

        return Result.success()
    }


    private fun enviarNotificacao(titulo: String, texto: String) {
        val channelId = "proximidade_channel"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificações de proximidade",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }

}
