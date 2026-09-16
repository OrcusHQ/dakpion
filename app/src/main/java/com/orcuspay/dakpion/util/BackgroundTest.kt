package com.orcuspay.dakpion.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.orcuspay.dakpion.BackgroundTestReceiver

/**
 * Proves, rather than guesses, whether this phone lets Dakpion wake up in the
 * background — the thing OEM "Autostart" switches control and no API exposes.
 *
 * Flow: [start] schedules an alarm ~90 s out. The merchant closes Dakpion from
 * Recent apps. If the phone allows it, the alarm broadcast starts our process
 * and [BackgroundTestReceiver] records PASSED. If the app was force-stopped
 * and not allowed to autostart, the broadcast is never delivered and, after a
 * grace period, the Device tab shows FAILED with the fix. An alarm broadcast
 * and an SMS broadcast reach a killed app by the same mechanism, so passing
 * this means payment SMS will wake Dakpion too.
 */
object BackgroundTest {

    enum class Result { NOT_RUN, PENDING, PASSED, FAILED, FOREGROUND }

    private const val KEY_STARTED_AT = "bg_test_started_at"
    private const val KEY_RESULT_AT = "bg_test_result_at"
    private const val KEY_RESULT = "bg_test_result"

    private const val ALARM_DELAY_MS = 90_000L
    // Alarms are inexact; allow the OS a generous window before calling it a fail.
    private const val TIMEOUT_MS = 5 * 60_000L
    private const val REQUEST_CODE = 7101

    fun start(context: Context, preference: DakpionPreference) {
        val now = System.currentTimeMillis()
        preference.putString(KEY_STARTED_AT, now.toString())
        preference.putString(KEY_RESULT, "")
        preference.putString(KEY_RESULT_AT, "")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context)
        alarmManager.cancel(pending)
        // Inexact + allow-while-idle needs no special permission and still
        // fires during Doze (within a window). Exact alarms would need a
        // permission prompt that some OEMs hide.
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            now + ALARM_DELAY_MS,
            pending,
        )
    }

    /** Called by [BackgroundTestReceiver] when the alarm reaches us. */
    fun recordFired(preference: DakpionPreference, appInForeground: Boolean) {
        val startedAt = preference.getString(KEY_STARTED_AT)?.toLongOrNull() ?: return
        if (System.currentTimeMillis() - startedAt > TIMEOUT_MS * 2) return
        preference.putString(KEY_RESULT, if (appInForeground) "foreground" else "passed")
        preference.putString(KEY_RESULT_AT, System.currentTimeMillis().toString())
    }

    fun status(preference: DakpionPreference): Pair<Result, Long?> {
        val startedAt = preference.getString(KEY_STARTED_AT)?.toLongOrNull()
            ?: return Result.NOT_RUN to null
        val resultAt = preference.getString(KEY_RESULT_AT)?.toLongOrNull()
        return when (preference.getString(KEY_RESULT)) {
            "passed" -> Result.PASSED to resultAt
            "foreground" -> Result.FOREGROUND to resultAt
            else -> {
                if (System.currentTimeMillis() - startedAt < TIMEOUT_MS) {
                    Result.PENDING to startedAt
                } else {
                    Result.FAILED to startedAt
                }
            }
        }
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context.applicationContext, BackgroundTestReceiver::class.java)
            .setAction(BackgroundTestReceiver.ACTION)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
