package com.example.bhullkad.reset

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bhullkad.BhullkadApp

class DailyResetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val manager = (applicationContext as BhullkadApp).resetManager
        return try {
            manager.runResetIfDue()
            manager.scheduleNext(fromWorker = true)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
