package com.orcuspay.dakpion

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orcuspay.dakpion.worker.SyncScheduler

/**
 * Brings Dakpion back after a reboot or an app update, without anyone having
 * to open the app: restarts the keep-alive service, re-arms the periodic
 * sync, and runs one sync right away to flush anything that arrived while
 * we were down.
 */
class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                SmsKeepAliveService.start(context)
                SyncScheduler.ensurePeriodic(context.applicationContext)
                SyncScheduler.enqueueImmediate(context.applicationContext)
            }
        }
    }
}
