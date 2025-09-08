package com.example.cmu.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmu.data.local.AvaliacaoEntity
import com.example.cmu.data.model.LeaderboardItem
import com.example.cmu.data.repository.AvaliacaoRepository
import kotlinx.coroutines.launch

class AvaliacaoViewModel(private val repository: AvaliacaoRepository) : ViewModel() {

    fun adicionarAvaliacao(avaliacao: AvaliacaoEntity) {
        viewModelScope.launch {
            repository.adicionarAvaliacao(avaliacao)
        }
    }

    fun syncFromFirebase() {
        repository.syncFromFirebase()
    }

    fun syncPending() {
        viewModelScope.launch {
            repository.sincronizarPendentes()
        }
    }

    fun verificarUltimaAvaliacao(userId: String, callback: (AvaliacaoEntity?) -> Unit) {
        viewModelScope.launch {
            val ultima = repository.getUltimaAvaliacao(userId)
            callback(ultima)
        }
    }


    fun listarUltimas(placeId: String, callback: (List<AvaliacaoEntity>) -> Unit) {
        viewModelScope.launch {
            val result = repository.listarUltimasAvaliacoes(placeId)
            callback(result)
        }
    }

    fun listarHistorico(callback: (List<AvaliacaoEntity>) -> Unit) {
        viewModelScope.launch {
            val result = repository.listarHistoricoUtilizador()
            callback(result)
        }
    }

    fun leaderboard(callback: (List<LeaderboardItem>) -> Unit) {
        viewModelScope.launch {
            val result = repository.getLeaderboard()
            callback(result)
        }
    }
}
