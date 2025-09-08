package com.example.cmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avaliacoes")
data class AvaliacaoEntity(
    @PrimaryKey val id: String,
    val placeId: String,
    val utilizador: String,
    val estrelas: Int,
    val comentario: String,
    val fotoPath: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
