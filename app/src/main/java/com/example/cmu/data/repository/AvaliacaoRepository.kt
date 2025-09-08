package com.example.cmu.data.repository

import com.example.cmu.data.local.AvaliacaoDao
import com.example.cmu.data.local.AvaliacaoEntity
import com.example.cmu.data.model.LeaderboardItem
import com.example.cmu.data.remote.FirebaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AvaliacaoRepository(private val dao: AvaliacaoDao) {

    // Adicionar avaliação (Room + Firebase)
    suspend fun adicionarAvaliacao(avaliacao: AvaliacaoEntity) {
        withContext(Dispatchers.IO) {
            // Guardar local
            dao.inserirAvaliacao(avaliacao)

            // Enviar para Firebase
            val data = hashMapOf(
                "placeId" to avaliacao.placeId,
                "estrelas" to avaliacao.estrelas,
                "comentario" to avaliacao.comentario,
                "utilizador" to avaliacao.utilizador,
                "fotoPath" to avaliacao.fotoPath,
                "timestamp" to avaliacao.timestamp
            )

            FirebaseProvider.db.collection("avaliacoes")
                .add(data)
                .addOnSuccessListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.markSynced(avaliacao.id)
                    }
                }
        }
    }

    // Últimas 10 avaliações de um estabelecimento
    suspend fun listarUltimas(placeId: String): List<AvaliacaoEntity> =
        withContext(Dispatchers.IO) { dao.listarUltimasAvaliacoes(placeId) }

    // Histórico completo
    suspend fun listarHistorico(): List<AvaliacaoEntity> =
        withContext(Dispatchers.IO) { dao.listarHistorico() }

    // Sincronizar Firebase → Room
    fun syncFromFirebase() {
        FirebaseProvider.db.collection("avaliacoes")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.map { doc ->
                    AvaliacaoEntity(
                        placeId = (doc.getLong("placeId") ?: 0).toString(),
                        utilizador = doc.getString("utilizador") ?: "",
                        estrelas = doc.getLong("estrelas")?.toInt() ?: 0,
                        comentario = doc.getString("comentario") ?: "",
                        fotoPath = doc.getString("fotoPath"),
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        synced = true
                    )
                }
                CoroutineScope(Dispatchers.IO).launch {
                    lista.forEach { dao.inserirAvaliacao(it) }
                }
            }
    }

    // Sincronizar Room → Firebase (quando offline e depois volta net)
    suspend fun syncPending() {
        val unsynced = dao.getUnsynced()
        for (a in unsynced) {
            val data = hashMapOf(
                "placeId" to a.placeId,
                "estrelas" to a.estrelas,
                "comentario" to a.comentario,
                "utilizador" to a.utilizador,
                "fotoPath" to a.fotoPath,
                "timestamp" to a.timestamp
            )
            FirebaseProvider.db.collection("avaliacoes")
                .add(data)
                .addOnSuccessListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.markSynced(a.id)
                    }
                }
        }
    }


    suspend fun getLeaderboard(): List<LeaderboardItem> =
        withContext(Dispatchers.IO) { dao.leaderboard() }

}
