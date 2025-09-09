package com.example.cmu.data.remote

import com.example.cmu.data.remote.models.PlacesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchNearby(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String
    ): PlacesResponse
}
