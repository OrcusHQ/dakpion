package com.orcuspay.dakpion.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Single place that schedules [DakpionKamla]. Two shapes of work:
 *
 *  - [enqueueImmediate]: an expedited one-shot with no initial delay. Expedited
 *    work uses JobScheduler's priority lane (API 31+) or a short foreground
 *    service (API 26–30), so OEM battery managers defer it far less than a
 *    plain job. Used as the retry when the in-receiver upload fails, and as
 *    the "sync on app open" trigger.
 *  - [ensurePeriodic]: the catch-up scan. 15 minutes is the minimum Android
 *    allows; it heals anything the receiver missed (phone offline, OEM killed
 *    the process before the broadcast reached us, etc.).
 */
object SyncScheduler {

    private const val IMMEDIATE_WORK_NAME = "dakpion-sync-now"

    private val connected: Constraints
        get() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    fun enqueueImmediate(
        context: Context,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP,
    ) {
        val request = OneTimeWorkRequestBuilder<DakpionKamla>()
            .setConstraints(connected)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            // Short LINEAR retries (15 s, 30 s, 45 s, …). Exponential backoff
            // pushed a payment SMS out by an hour after a few failures on a
            // flaky connection — a merchant can't wait that long.
            .setBackoffCriteria(BackoffPolicy.LINEAR, 15L, TimeUnit.SECONDS)
            .addTag(DakpionKamla.TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(IMMEDIATE_WORK_NAME, policy, request)
    }

    fun ensurePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<DakpionKamla>(
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS,
        )
            .setConstraints(connected)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 2L, TimeUnit.MINUTES)
            .addTag(DakpionKamla.TAG)
            .build()

        // UPDATE keeps the existing schedule (REPLACE would reset the timer on
        // every process start, which on a frequently-killed app means the
        // periodic sync may never actually fire).
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DakpionKamla.TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
    }
}
