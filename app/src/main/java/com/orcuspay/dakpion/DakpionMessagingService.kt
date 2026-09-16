package com.orcuspay.dakpion

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orcuspay.dakpion.util.DakpionPreference
import com.orcuspay.dakpion.util.Diag
import com.orcuspay.dakpion.worker.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Wake-up path for phones whose OEM freezes background apps (Transsion,
 * Xiaomi…): a high-priority FCM push is the one event those ROMs let through
 * to a frozen app. HQ sends one when a customer is waiting on a verification
 * and no SMS has arrived; we thaw, sync the inbox, and upload.
 */
class DakpionMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        Diag.init(applicationContext)
        Diag.log("push: new token")
        DakpionPreference(applicationContext).setPushToken(token)
        // Report it to HQ right away (the verify heartbeat carries the token).
        SyncScheduler.enqueueImmediate(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Diag.init(applicationContext)
        val reason = message.data["reason"] ?: "?"
        Diag.log("push: received reason=$reason")
        SmsKeepAliveService.start(applicationContext)

        // A high-priority push grants a short execution window; do the sync
        // inside it rather than deferring to a job the OEM may hold back.
        runBlocking {
            val outcome = withTimeoutOrNull(PUSH_BUDGET_MS) {
                try {
                    DakpionApplication.syncer(applicationContext).sync(fast = true)
                } catch (e: Exception) {
                    Diag.log("push: sync threw ${e.javaClass.simpleName}: ${e.message}")
                    null
                }
            }
            Diag.log("push: sync outcome=${outcome ?: "TIMEOUT"}")
        }
    }

    companion object {
        private const val PUSH_BUDGET_MS = 9_000L
    }
}
