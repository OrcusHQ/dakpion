package com.orcuspay.dakpion.util

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * On-device event trail for the SMS pipeline. Many OEM ROMs (Transsion,
 * Xiaomi…) suppress app logcat output entirely, which makes a silent
 * failure in the background indistinguishable from "never ran". So the
 * receiver, the inbox observer, the scheduler and the syncer each record
 * what they did here; the trail survives process death (SharedPreferences)
 * and is printed by `adb shell dumpsys activity service
 * com.dakpion.app/.SmsKeepAliveService` and on the Device tab.
 */
object Diag {

    private const val PREFS = "dakpion_diag"
    private const val KEY = "events"
    private const val MAX_EVENTS = 80
    private const val SEP = ""

    private val fmt = SimpleDateFormat("dd/MM HH:mm:ss", Locale.US)

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun log(message: String) {
        Log.d("kraken", message)
        val ctx = appContext ?: return
        val entry = "${fmt.format(Date())} $message"
        try {
            val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val existing = prefs.getString(KEY, "") ?: ""
            val all = if (existing.isEmpty()) listOf(entry) else existing.split(SEP) + entry
            prefs.edit().putString(KEY, all.takeLast(MAX_EVENTS).joinToString(SEP)).apply()
        } catch (_: Exception) {
            // Diagnostics must never break the pipeline.
        }
        // Mirror to the app's external files dir so it can be pulled with adb
        // even on ROMs that hide app logs and time out service dumps.
        try {
            val dir = ctx.getExternalFilesDir(null) ?: return
            val file = java.io.File(dir, "diag.txt")
            if (file.length() > 200_000L) file.writeText("")
            file.appendText(entry + "\n")
        } catch (_: Exception) {
        }
    }

    fun events(context: Context): List<String> {
        return try {
            val raw = context.applicationContext
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "") ?: ""
            if (raw.isEmpty()) emptyList() else raw.split(SEP)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
