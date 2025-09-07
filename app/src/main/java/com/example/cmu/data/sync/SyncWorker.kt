package com.example.cmu.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cmu.data.repository.AvaliacaoRepository
import com.example.cmu.data.local.AppDatabase

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // obter instância da BD e do repositório
            val dao = AppDatabase.getDatabase(applicationContext).avaliacaoDao()
            val repo = AvaliacaoRepository(dao)

            // sincronizar Room → Firebase
            repo.syncPending()

            // sincronizar Firebase → Room
            repo.syncFromFirebase()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
