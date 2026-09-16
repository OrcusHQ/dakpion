package com.orcuspay.dakpion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orcuspay.dakpion.util.BackgroundTest
import com.orcuspay.dakpion.util.DakpionPreference
import com.orcuspay.dakpion.worker.SyncScheduler

/**
 * Target of the background self-test alarm (see [BackgroundTest]). Reaching
 * this code at all is the proof: the phone let Dakpion start in the
 * background. We also use the wake-up productively — restart the keep-alive
 * service and run a sync.
 */
class BackgroundTestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        BackgroundTest.recordFired(
            preference = DakpionPreference(context.applicationContext),
            appInForeground = MainActivity.inForeground,
        )
        SmsKeepAliveService.start(context)
        SyncScheduler.enqueueImmediate(context.applicationContext)
    }

    companion object {
        const val ACTION = "com.orcuspay.dakpion.BACKGROUND_TEST"
    }
}
