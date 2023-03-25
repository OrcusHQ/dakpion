package com.orcuspay.dakpion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.orcuspay.dakpion.worker.DakpionKamla
import java.util.concurrent.TimeUnit


class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("pluton", "Receiver Start")
        if (intent.action != null && intent.action == SMS_RECEIVED) {
            val workManager = WorkManager.getInstance(context)

            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest =
                OneTimeWorkRequestBuilder<DakpionKamla>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        2L,
                        TimeUnit.MINUTES
                    )
                    .addTag(DakpionKamla.TAG)
                    .build()

            workManager.enqueue(workRequest)
        }
    }


    companion object {
        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        const val SMS_BUNDLE = "pdus"
        const val EXTRA_BUNDLE = "pdus"
    }
}
