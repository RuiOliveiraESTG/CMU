package com.example.cmu.data.repository

import com.example.cmu.data.local.PlaceDao
import com.example.cmu.data.local.PlaceEntity
import com.example.cmu.data.remote.FirebaseProvider
import com.example.cmu.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class PlaceRepository(private val dao: PlaceDao) {

    fun getPlaces(): Flow<List<PlaceEntity>> = dao.getAllPlaces()

    suspend fun fetchAndSavePlaces(apiKey: String, lat: Double, lon: Double) {
        val response = RetrofitInstance.api.searchNearby(
            location = "$lat,$lon",
            radius = 1000,
            type = "cafe",

            key = apiKey
        )

        if (response.status == "OK") {
            val entities = response.results.map {
                PlaceEntity(
                    placeId = it.place_id,
                    name = it.name,
                    address = it.vicinity,
                    lat = it.geometry.location.lat,
                    lon = it.geometry.location.lng,
                    phone = null
                )
            }
            dao.insertPlaces(entities)

            entities.forEach { place ->
                FirebaseProvider.db.collection("places")
                    .document(place.placeId)
                    .set(place)
                    .await()
            }
        }
    }

}
