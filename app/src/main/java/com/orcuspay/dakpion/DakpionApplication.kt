package com.orcuspay.dakpion

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.orcuspay.dakpion.worker.DakpionKamla
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class DakpionApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        val workManager = WorkManager.getInstance(this)

        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest =
            PeriodicWorkRequestBuilder<DakpionKamla>(
                1, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    2L,
                    TimeUnit.MINUTES
                )
                .addTag(DakpionKamla.TAG)
                .build()

        workManager
            .enqueueUniquePeriodicWork(
                DakpionKamla.TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }
}