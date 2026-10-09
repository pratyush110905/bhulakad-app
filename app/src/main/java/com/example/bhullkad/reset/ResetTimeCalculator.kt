package com.example.bhullkad.reset

import java.time.Duration
import java.time.ZonedDateTime

object ResetTimeCalculator {
    private fun resetOn(now: ZonedDateTime, dayOffset: Long, hour: Int, minute: Int): ZonedDateTime =
        now.toLocalDate().plusDays(dayOffset).atTime(hour, minute).atZone(now.zone)

    fun mostRecentBoundary(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
        val today = resetOn(now, 0, hour, minute)
        return if (!now.isBefore(today)) today else resetOn(now, -1, hour, minute)
    }

    private fun nextBoundary(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
        val today = resetOn(now, 0, hour, minute)
        return if (now.isBefore(today)) today else resetOn(now, 1, hour, minute)
    }

    fun delayUntilNext(now: ZonedDateTime, hour: Int, minute: Int): Duration =
        Duration.between(now, nextBoundary(now, hour, minute))

    fun isResetDue(lastResetAtMillis: Long, now: ZonedDateTime, hour: Int, minute: Int): Boolean {
        if (lastResetAtMillis == 0L) return true
        val boundaryMillis = mostRecentBoundary(now, hour, minute).toInstant().toEpochMilli()
        return lastResetAtMillis < boundaryMillis
    }
}
