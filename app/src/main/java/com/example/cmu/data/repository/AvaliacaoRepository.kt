package com.example.cmu.data.repository

import com.example.cmu.data.local.AvaliacaoDao
import com.example.cmu.data.local.AvaliacaoEntity
import com.example.cmu.data.model.LeaderboardItem
import com.example.cmu.data.remote.FirebaseProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


class AvaliacaoRepository(private val dao: AvaliacaoDao) {

    suspend fun adicionarAvaliacao(avaliacao: AvaliacaoEntity) {
        withContext(Dispatchers.IO) {
            val avaliacaoFinal = if (avaliacao.id.isBlank()) {
                avaliacao.copy(id = UUID.randomUUID().toString())
            } else avaliacao

            dao.inserirAvaliacao(avaliacaoFinal)

            val data = hashMapOf(
                "placeId" to avaliacao.placeId,
                "estrelas" to avaliacao.estrelas,
                "comentario" to avaliacao.comentario,
                "utilizador" to avaliacao.utilizador,
                "doce" to avaliacao.doce,
                "fotoPath" to avaliacao.fotoPath,
                "timestamp" to avaliacao.timestamp
            )


            FirebaseProvider.db.collection("avaliacoes")
                .document(avaliacaoFinal.id)
                .set(data)
                .addOnSuccessListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.markSynced(avaliacaoFinal.id)
                    }
                }
        }
    }

    suspend fun listarUltimasAvaliacoes(placeId: String): List<AvaliacaoEntity> =
        withContext(Dispatchers.IO) { dao.listarUltimasAvaliacoes(placeId) }

    suspend fun listarHistoricoUtilizador(): List<AvaliacaoEntity> =
        withContext(Dispatchers.IO) { dao.listarHistoricoUser(FirebaseAuth.getInstance().currentUser!!.uid) }

    fun syncFromFirebase() {
        FirebaseProvider.db.collection("avaliacoes")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.map { doc ->
                    AvaliacaoEntity(
                        id = doc.id,
                        placeId = doc.getString("placeId") ?: "",
                        utilizador = doc.getString("utilizador") ?: "",
                        estrelas = doc.getLong("estrelas")?.toInt() ?: 0,
                        comentario = doc.getString("comentario") ?: "",
                        doce = doc.getString("doce"),
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

    suspend fun sincronizarPendentes() {
        val unsynced = withContext(Dispatchers.IO) { dao.getUnsynced() }
        for (a in unsynced) {
            val data = hashMapOf(
                "placeId" to a.placeId,
                "estrelas" to a.estrelas,
                "comentario" to a.comentario,
                "utilizador" to a.utilizador,
                "doce" to a.doce,
                "fotoPath" to a.fotoPath,
                "timestamp" to a.timestamp
            )

            FirebaseProvider.db.collection("avaliacoes")
                .document(a.id) // mantém o mesmo ID
                .set(data)
                .addOnSuccessListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.markSynced(a.id)
                    }
                }
        }
    }


    suspend fun getLeaderboard(): List<LeaderboardItem> =
        withContext(Dispatchers.IO) { dao.leaderboard() }

    suspend fun getUltimaAvaliacao(userId: String): AvaliacaoEntity? =
        withContext(Dispatchers.IO) { dao.getUltimaAvaliacao(userId) }

    suspend fun listarUltimas10(placeId: String): List<AvaliacaoEntity> =
        withContext(Dispatchers.IO) { dao.listarUltimas10(placeId) }

}
