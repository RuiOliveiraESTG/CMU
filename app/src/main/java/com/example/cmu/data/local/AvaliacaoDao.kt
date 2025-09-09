package com.example.cmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cmu.data.model.LeaderboardItem

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

    @Query("SELECT * FROM avaliacoes WHERE placeId = :placeId ORDER BY timestamp DESC LIMIT 10")
    suspend fun listarUltimasAvaliacoes(placeId: String): List<AvaliacaoEntity>

    @Query("SELECT * FROM avaliacoes WHERE synced = 0")
    suspend fun getUnsynced(): List<AvaliacaoEntity>

    @Query("UPDATE avaliacoes SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)


    @Query("""
    SELECT placeId, AVG(estrelas) as media, COUNT(*) as total
    FROM avaliacoes
    GROUP BY placeId
    ORDER BY media DESC
""")
    suspend fun leaderboard(): List<LeaderboardItem>

    @Query("SELECT * FROM avaliacoes WHERE utilizador = :uid ORDER BY timestamp DESC")
    suspend fun listarHistoricoUser(uid: String): List<AvaliacaoEntity>

    @Query("SELECT * FROM avaliacoes WHERE utilizador = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getUltimaAvaliacao(userId: String): AvaliacaoEntity?

    @Query("SELECT AVG(estrelas) FROM avaliacoes WHERE placeId = :placeId")
    suspend fun getMediaEstrelas(placeId: String): Double?

    @Query("SELECT * FROM avaliacoes WHERE placeId = :placeId ORDER BY timestamp DESC LIMIT 10")
    suspend fun listarUltimas10(placeId: String): List<AvaliacaoEntity>

}
