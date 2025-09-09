package com.example.cmu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmu.data.local.AvaliacaoEntity
import com.example.cmu.data.model.LeaderboardItem
import com.example.cmu.data.repository.AvaliacaoRepository
import kotlinx.coroutines.launch

class AvaliacaoViewModel(private val repository: AvaliacaoRepository) : ViewModel() {


    private val _avaliacoes = MutableLiveData<List<AvaliacaoEntity>>(emptyList())
    val avaliacoes: LiveData<List<AvaliacaoEntity>> = _avaliacoes

    fun adicionarAvaliacao(avaliacao: AvaliacaoEntity) {
        viewModelScope.launch {
            repository.adicionarAvaliacao(avaliacao)
            listarUltimas(avaliacao.placeId)
        }
    }

    fun listarUltimas(placeId: String) {
        viewModelScope.launch {
            _avaliacoes.value = repository.listarUltimasAvaliacoes(placeId)
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

