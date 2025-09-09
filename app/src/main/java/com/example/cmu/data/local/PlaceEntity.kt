package com.example.cmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey val placeId: String,
    val name: String?,
    val address: String?,
    val lat: Double,
    val lon: Double,
    val phone: String? = null
)
