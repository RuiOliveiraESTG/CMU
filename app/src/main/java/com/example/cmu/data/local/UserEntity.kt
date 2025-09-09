package com.example.cmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val name: String?,
    val photoUrl: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
