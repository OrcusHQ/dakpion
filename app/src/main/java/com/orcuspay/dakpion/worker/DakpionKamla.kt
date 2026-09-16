package com.orcuspay.dakpion.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.orcuspay.dakpion.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager entry point for the SMS sync. The actual pipeline lives in
 * [SmsSyncer]; this worker just runs it and maps the outcome to retry/success.
 * Scheduled by [SyncScheduler] (expedited retry / periodic catch-up).
 */
@HiltWorker
class DakpionKamla @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val smsSyncer: SmsSyncer,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(
    appContext = context,
    params = workerParams
) {

    companion object {
        const val TAG = "dakpionkamla"
    }

    override suspend fun doWork(): Result {
        return when (smsSyncer.sync()) {
            SmsSyncer.Outcome.SUCCESS -> Result.success()
            SmsSyncer.Outcome.RETRY -> Result.retry()
        }
    }

    // Required for expedited work on API 26–30, where WorkManager runs it as a
    // short-lived foreground service. Silent, low-importance notification.
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = notificationHelper.buildSyncNotification()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NotificationHelper.SYNC_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(NotificationHelper.SYNC_NOTIFICATION_ID, notification)
        }
    }
}
