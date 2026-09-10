package com.orcuspay.dakpion.util

import android.content.Context
import com.orcuspay.dakpion.DakpionApplication
import java.util.*

class DakpionPreference(
    context: Context
) {

    private val pref = context.getSharedPreferences(
        DakpionApplication::class.java.name,
        Context.MODE_PRIVATE
    )

    private val FIRST_LAUNCH = "FIRST_LAUNCH"
    private val LAST_SYNC_TIME = "LAST_SYNC_TIME"
    private val SIM_SLOT_FILTER = "SIM_SLOT_FILTER"

    fun isFirstLaunch(): Boolean {
        return pref.getBoolean(FIRST_LAUNCH, true)
    }

    fun setFirstLaunch() {
        pref.edit().putBoolean(FIRST_LAUNCH, false).apply()
    }

    fun setLastSyncTime(date: Date) {
        pref.edit().putLong(LAST_SYNC_TIME, date.time).apply()
    }

    fun getLastSyncTime(): Date? {
        val time = pref.getLong(LAST_SYNC_TIME, -1)
        if (time == -1L) {
            return null
        }
        return Date(pref.getLong(LAST_SYNC_TIME, time))
    }

    /**
     * Which SIM slot to read SMS from. -1 (default) = both SIMs, i.e. no
     * filtering, which matches the app's original behavior.
     */
    fun getSimSlotFilter(): Int {
        return pref.getInt(SIM_SLOT_FILTER, -1)
    }

    fun setSimSlotFilter(slot: Int) {
        pref.edit().putInt(SIM_SLOT_FILTER, slot).apply()
    }
}