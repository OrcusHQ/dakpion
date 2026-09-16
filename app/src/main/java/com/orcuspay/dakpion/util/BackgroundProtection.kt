package com.orcuspay.dakpion.util

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.orcuspay.dakpion.SmsKeepAliveService

/**
 * Detects whether this phone is likely to stop Dakpion running in the
 * background, and deep-links the merchant to the OEM screens that fix it.
 *
 * Battery "Unrestricted" alone is not enough on Infinix/Tecno, Xiaomi, Oppo,
 * Vivo and Huawei: each ships its own "Autostart" / app-launch manager that
 * blocks background apps regardless of the Android battery setting. Those
 * screens have no public intent, so we try the known component names and fall
 * back to the app's settings page.
 */
object BackgroundProtection {

    enum class Oem(val label: String) {
        TRANSSION("Infinix / Tecno / itel"),
        XIAOMI("Xiaomi / Redmi / POCO"),
        OPPO("OPPO / Realme / OnePlus"),
        VIVO("vivo / iQOO"),
        HUAWEI("Huawei / Honor"),
        SAMSUNG("Samsung"),
        OTHER(""),
    }

    data class Status(
        val oem: Oem,
        val batteryOptimized: Boolean,
        val backgroundRestricted: Boolean,
        val keepAliveRunning: Boolean,
        val test: BackgroundTest.Result,
        val testAt: Long?,
    ) {
        /** True when the OEM has its own app-killer the merchant must configure. */
        val hasAutostartManager: Boolean get() = oem != Oem.OTHER && oem != Oem.SAMSUNG

        /**
         * Green only when everything we can measure is fine AND the background
         * self-test has passed — that's the proof the OEM lets us wake up.
         */
        val needsAttention: Boolean
            get() = batteryOptimized || backgroundRestricted || !keepAliveRunning ||
                test != BackgroundTest.Result.PASSED
    }

    fun detectOem(): Oem {
        val s = "${Build.MANUFACTURER.orEmpty()} ${Build.BRAND.orEmpty()}".lowercase()
        fun any(vararg keys: String) = keys.any { s.contains(it) }
        return when {
            any("infinix", "tecno", "itel", "transsion") -> Oem.TRANSSION
            any("xiaomi", "redmi", "poco") -> Oem.XIAOMI
            any("oppo", "realme", "oneplus") -> Oem.OPPO
            any("vivo", "iqoo") -> Oem.VIVO
            any("huawei", "honor") -> Oem.HUAWEI
            any("samsung") -> Oem.SAMSUNG
            else -> Oem.OTHER
        }
    }

    fun status(context: Context): Status {
        val (test, testAt) = BackgroundTest.status(DakpionPreference(context.applicationContext))
        return Status(
            oem = detectOem(),
            batteryOptimized = isBatteryOptimized(context),
            backgroundRestricted = isBackgroundRestricted(context),
            keepAliveRunning = SmsKeepAliveService.isRunning,
            test = test,
            testAt = testAt,
        )
    }

    fun startBackgroundTest(context: Context) {
        BackgroundTest.start(context, DakpionPreference(context.applicationContext))
    }

    fun steps(oem: Oem): List<String> = when (oem) {
        Oem.TRANSSION -> listOf(
            "Phone Master → App management → Auto-start: turn Dakpion ON.",
            "Recent apps: pull Dakpion's card down and tap the lock icon.",
            "Don't run Phone Master's Clean / Boost — it force-stops Dakpion.",
            "Settings → Apps → Dakpion → Battery: Unrestricted.",
        )
        Oem.XIAOMI -> listOf(
            "Security → Manage apps → Dakpion → Autostart: ON.",
            "Settings → Battery → App battery saver → Dakpion: No restrictions.",
            "Recent apps: hold Dakpion's card and tap the lock icon.",
        )
        Oem.OPPO -> listOf(
            "Settings → Battery → App battery management → Dakpion: allow background activity and auto-launch.",
            "Settings → Apps → Auto-launch: turn Dakpion ON.",
            "Recent apps: lock Dakpion's card.",
        )
        Oem.VIVO -> listOf(
            "i Manager → App manager → Autostart: turn Dakpion ON.",
            "Settings → Battery → Background power consumption: allow Dakpion.",
            "Recent apps: lock Dakpion's card.",
        )
        Oem.HUAWEI -> listOf(
            "Settings → Apps → App launch → Dakpion → Manage manually: enable Auto-launch, Secondary launch and Run in background.",
            "Recent apps: lock Dakpion's card.",
        )
        Oem.SAMSUNG -> listOf(
            "Settings → Battery → Background usage limits: make sure Dakpion is not in Sleeping or Deep sleeping apps.",
            "Settings → Apps → Dakpion → Battery: Unrestricted.",
        )
        Oem.OTHER -> listOf(
            "Settings → Apps → Dakpion → Battery: Unrestricted.",
            "Keep Dakpion out of any battery saver or sleeping-apps list.",
            "Recent apps: lock Dakpion's card so it isn't swiped away.",
        )
    }

    /**
     * Try to open the OEM's Autostart / app-launch manager. Returns true if an
     * OEM screen opened, false if we had to fall back to the app settings page.
     */
    fun openAutostartSettings(context: Context): Boolean {
        val candidates: List<ComponentName> = when (detectOem()) {
            Oem.TRANSSION -> listOf(
                ComponentName("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
                ComponentName("com.transsion.phonemanager", "com.cyin.himgr.autostart.AutoStartActivity"),
                ComponentName("com.transsion.phonemaster", "com.cyin.himgr.applicationmanager.view.activity.AppManagerActivity"),
            )
            Oem.XIAOMI -> listOf(
                ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            )
            Oem.OPPO -> listOf(
                ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
                ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
                ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
                ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"),
            )
            Oem.VIVO -> listOf(
                ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
                ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
            )
            Oem.HUAWEI -> listOf(
                ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
            )
            Oem.SAMSUNG -> listOf(
                ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity"),
                ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.battery.ui.BatteryActivity"),
            )
            Oem.OTHER -> emptyList()
        }

        for (component in candidates) {
            try {
                context.startActivity(
                    Intent().apply {
                        this.component = component
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                return true
            } catch (_: Exception) {
                // Not present on this ROM version; try the next one.
            }
        }
        openAppSettings(context)
        return false
    }

    /**
     * Ask Android directly to exempt Dakpion from battery optimization (one
     * tap "Allow" dialog — what PipraPay/OwnPay's apps do). Falls back to the
     * optimization list, then to the app details page.
     */
    fun openBatterySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                return
            } catch (_: Exception) {
                // Some ROMs don't ship the dialog; try the list screen.
            }
            try {
                context.startActivity(
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                return
            } catch (_: Exception) {
                // Fall through to the app details page.
            }
        }
        openAppSettings(context)
    }

    fun openAppSettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (_: Exception) {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun isBatteryOptimized(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return false
        return !powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun isBackgroundRestricted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return false
        return activityManager.isBackgroundRestricted
    }
}
