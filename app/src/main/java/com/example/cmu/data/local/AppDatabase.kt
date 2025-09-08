package com.example.cmu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

@Database(
    entities = [AvaliacaoEntity::class, PlaceEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun avaliacaoDao(): AvaliacaoDao
    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cmu_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
