package com.example.cmu.data.local

import com.example.cmu.data.remote.FirebaseProvider
import com.example.cmu.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class PlaceRepository(private val dao: PlaceDao) {

    fun getPlaces(): Flow<List<PlaceEntity>> = dao.getAllPlaces()

    suspend fun fetchAndSavePlaces(apiKey: String) {
        val response = RetrofitInstance.api.searchNearby(
            location = "41.1579,-8.6291", // Porto
            radius = 1000,
            type = "restaurant",
            key = apiKey
        )

        if (response.status == "OK") {
            val entities = response.results.map {
                PlaceEntity(
                    placeId = it.place_id,
                    name = it.name,
                    address = it.vicinity,
                    lat = it.geometry.location.lat,
                    lon = it.geometry.location.lng
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
