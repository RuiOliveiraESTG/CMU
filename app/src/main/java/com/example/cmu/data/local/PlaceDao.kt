package com.example.cmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<PlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Query("SELECT * FROM places")
    fun getAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE placeId = :placeId Limit 1")
    suspend fun getPlaceById(placeId: String): PlaceEntity?

    @Query("SELECT * FROM places")
    suspend fun getAllPlacesOnce(): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE placeId = :id LIMIT 1")
    fun observePlaceById(id: String): kotlinx.coroutines.flow.Flow<PlaceEntity?>
}
