package com.example.bhullkad.reset

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bhullkad.data.UserPreferences
import com.example.bhullkad.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class DailyResetManager(
    context: Context,
    private val taskRepository: TaskRepository,
    private val preferences: UserPreferences,
    private val now: () -> ZonedDateTime = { ZonedDateTime.now(ZoneId.systemDefault()) },
) {
    private val appContext = context.applicationContext
    private val mutex = Mutex()

    suspend fun runResetIfDue(): Int = mutex.withLock {
        val lastResetAt = preferences.lastResetAt.first()
        val hour = preferences.resetHour.first()
        val minute = preferences.resetMinute.first()
        val current = now()

        if (!ResetTimeCalculator.isResetDue(lastResetAt, current, hour, minute)) {
            return@withLock 0
        }

        val cutoff = ResetTimeCalculator.mostRecentBoundary(current, hour, minute)
            .toInstant().toEpochMilli()
        val archived = taskRepository.archiveCompletedBefore(cutoff)
        preferences.setLastResetAt(current.toInstant().toEpochMilli())
        archived
    }

    suspend fun scheduleNext(fromWorker: Boolean = false) {
        val hour = preferences.resetHour.first()
        val minute = preferences.resetMinute.first()
        val delayMillis = ResetTimeCalculator.delayUntilNext(now(), hour, minute).toMillis() + 1000L

        val request = OneTimeWorkRequestBuilder<DailyResetWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            WORK_NAME,
            if (fromWorker) ExistingWorkPolicy.APPEND_OR_REPLACE else ExistingWorkPolicy.REPLACE,
            request
        )
    }

    suspend fun syncNow() {
        runResetIfDue()
        scheduleNext()
    }

    companion object {
        const val WORK_NAME = "bhullkad_daily_reset"
    }
}
