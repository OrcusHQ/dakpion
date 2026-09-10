package com.orcuspay.dakpion.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

/** A selectable SIM in the phone. slotIndex is 0-based (0 = "SIM 1"). */
data class SimOption(
    val slotIndex: Int,
    val label: String,
)

/**
 * Reads the SIMs installed on the device and maps SMS subscription ids to SIM
 * slots, so the app can filter which SIM's SMS it processes.
 *
 * All of this needs READ_PHONE_STATE. When the permission is missing (or the
 * device can't report it), every method degrades gracefully so the caller
 * simply falls back to "read all SIMs".
 */
class SimInfoProvider(
    private val context: Context,
) {

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** SIMs currently in the phone, one per active slot. Empty if unknown. */
    fun getSims(): List<SimOption> {
        if (!hasPermission()) return emptyList()
        val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
            as? SubscriptionManager ?: return emptyList()

        return try {
            val list = sm.activeSubscriptionInfoList ?: return emptyList()
            list
                .sortedBy { it.simSlotIndex }
                .map { info ->
                    val carrier = info.carrierName?.toString()?.trim().orEmpty()
                    val number = info.number?.trim().orEmpty()
                    val parts = listOfNotNull(
                        "SIM ${info.simSlotIndex + 1}",
                        carrier.ifEmpty { null },
                        number.ifEmpty { null },
                    )
                    SimOption(
                        slotIndex = info.simSlotIndex,
                        label = parts.joinToString(" · "),
                    )
                }
        } catch (e: SecurityException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Map of subscriptionId -> simSlotIndex. Empty if unknown. */
    fun getSubscriptionIdToSlot(): Map<Int, Int> {
        if (!hasPermission()) return emptyMap()
        val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
            as? SubscriptionManager ?: return emptyMap()

        return try {
            (sm.activeSubscriptionInfoList ?: emptyList())
                .associate { it.subscriptionId to it.simSlotIndex }
        } catch (e: SecurityException) {
            emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    companion object {
        /** Sentinel for "read all SIMs" (no filtering). */
        const val SLOT_BOTH = -1
    }
}
