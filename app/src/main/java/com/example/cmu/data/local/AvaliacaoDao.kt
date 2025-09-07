package com.example.cmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AvaliacaoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirAvaliacao(avaliacao: AvaliacaoEntity)

    @Query("SELECT * FROM avaliacoes ORDER BY timestamp DESC")
    suspend fun getAvaliacoes(): List<AvaliacaoEntity>


    @Query("SELECT * FROM avaliacoes WHERE Synced = 0")
    suspend fun getAvaliacoesNaoSincronizadas(): List<AvaliacaoEntity>

    @Update
    suspend fun updateAvaliacao(avaliacao: AvaliacaoEntity)

    @Query("SELECT * FROM avaliacoes WHERE estabelecimentoId = :estabelecimentoId ORDER BY timestamp DESC LIMIT 10")
    suspend fun listarUltimasAvaliacoes(estabelecimentoId: Int): List<AvaliacaoEntity>

    @Query("SELECT * FROM avaliacoes ORDER BY timestamp DESC")
    suspend fun listarHistorico(): List<AvaliacaoEntity>

    @Query("SELECT * FROM avaliacoes WHERE synced = 0")
    suspend fun getUnsynced(): List<AvaliacaoEntity>

    @Query("UPDATE avaliacoes SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}
