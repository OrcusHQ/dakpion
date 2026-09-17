package com.orcuspay.dakpion.presentation.screens.device

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.orcuspay.dakpion.domain.model.DeviceInfo
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.composables.XButton
import com.orcuspay.dakpion.presentation.theme.*
import com.orcuspay.dakpion.util.BackgroundProtection
import com.orcuspay.dakpion.util.Diag
import com.orcuspay.dakpion.util.SimInfoProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DeviceStatusScreen(
    viewModel: DeviceStatusViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-read battery/background status whenever the merchant comes back from
    // a settings screen, so the card reflects what they just changed.
    var protection by remember { mutableStateOf(BackgroundProtection.status(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                protection = BackgroundProtection.status(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val phonePermission = rememberPermissionState(Manifest.permission.READ_PHONE_STATE)

    LaunchedEffect(Unit) {
        viewModel.refreshDeviceInfo()
    }

    // Reload SIM info as soon as the phone-state permission is granted.
    LaunchedEffect(phonePermission.status.isGranted) {
        if (phonePermission.status.isGranted) {
            viewModel.refreshDeviceInfo()
        }
    }

    Scaffold(
        topBar = { TopBar(title = "Device") },
        backgroundColor = MaterialTheme.colors.background,
    ) { pv ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { ConnectionCard(state = state) }

            item {
                BackgroundProtectionCard(
                    status = protection,
                    onOpenAutostart = { BackgroundProtection.openAutostartSettings(context) },
                    onOpenBattery = { BackgroundProtection.openBatterySettings(context) },
                )
            }

            item {
                SimSelectionCard(
                    state = state,
                    hasPermission = phonePermission.status.isGranted,
                    onRequestPermission = { phonePermission.launchPermissionRequest() },
                    onSelect = { slot -> viewModel.setSimSlotFilter(slot) },
                )
            }

            item {
                DeviceInfoCard(
                    deviceInfo = state.deviceInfo,
                    lastSyncTime = state.lastSyncTime,
                )
            }

            item { DiagnosticsCard(events = Diag.events(context).takeLast(14).reversed()) }

            item {
                XButton(
                    text = "Sync now",
                    loading = state.syncing,
                    enabled = !state.syncing,
                ) {
                    viewModel.syncNow()
                }
            }

            state.message?.let { message ->
                item {
                    StatusMessage(
                        message = message,
                        background = SuccessBg,
                        color = SuccessText,
                    )
                }
            }

            state.error?.let { error ->
                item {
                    StatusMessage(
                        message = error,
                        background = WarningBg,
                        color = WarningText,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionCard(state: DeviceStatusState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = PrimaryColor,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Connected to OrcusPay",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
            )
            Gap(height = 6.dp)
            Text(
                text = "Heartbeat and SMS sync are active for this Android device.",
                fontFamily = interFontFamily,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.82f),
                lineHeight = 20.sp,
            )
            Gap(height = 14.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("v${state.deviceInfo.appVersion}")
                Pill(state.deviceInfo.network ?: "network unknown")
            }
        }
    }
}

@Composable
private fun Pill(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontFamily = interFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
private fun BackgroundProtectionCard(
    status: BackgroundProtection.Status,
    onOpenAutostart: () -> Unit,
    onOpenBattery: () -> Unit,
) {
    val attention = status.needsAttention
    val fixCount = (if (status.batteryOptimized) 1 else 0) +
        (if (status.backgroundRestricted) 1 else 0) +
        (if (!status.keepAliveRunning) 1 else 0)
    val accent = if (attention) WarningText else SuccessText
    val accentBg = if (attention) WarningBg else SuccessBg

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = Color.White,
        border = BorderStroke(1.dp, BorderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: one shield + a plain-language status line.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentBg, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (attention) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Gap(width = 12.dp)
                Column {
                    Text(
                        text = "Phone protection",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                    )
                    Text(
                        text = when {
                            !attention -> "Active — payments sync even when closed"
                            fixCount == 1 -> "1 thing to fix"
                            else -> "$fixCount things to fix"
                        },
                        fontFamily = interFontFamily,
                        fontSize = 12.sp,
                        color = accent,
                    )
                }
            }

            Gap(height = 12.dp)
            Divider(color = BorderColor)
            Gap(height = 4.dp)

            ProtectionCheck(
                label = "Running in the background",
                ok = status.keepAliveRunning,
                problemHint = "Reopen Dakpion to restart it",
            )
            ProtectionCheck(
                label = "Battery won't stop it",
                ok = !status.batteryOptimized,
                problemHint = "Battery saver is limiting Dakpion",
                fixLabel = "Fix",
                onFix = onOpenBattery,
            )
            ProtectionCheck(
                label = "Allowed to run in the background",
                ok = !status.backgroundRestricted,
                problemHint = "Background activity is restricted",
                fixLabel = "Fix",
                onFix = onOpenBattery,
            )
            if (status.hasAutostartManager) {
                ProtectionCheck(
                    label = "Auto-start after restart",
                    ok = true,
                    neutral = true,
                    fixLabel = "Open",
                    onFix = onOpenAutostart,
                )
            }
        }
    }
}

/**
 * One protection check row: a status pill (green tick / amber warning, or a
 * neutral chevron for actions whose state we can't detect like OEM auto-start),
 * a plain-language label, and — only when it needs attention — an inline action
 * that deep-links to the exact settings screen.
 */
@Composable
private fun ProtectionCheck(
    label: String,
    ok: Boolean,
    problemHint: String? = null,
    fixLabel: String? = null,
    onFix: (() -> Unit)? = null,
    neutral: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = when {
            neutral -> Icons.Default.KeyboardArrowRight
            ok -> Icons.Default.Check
            else -> Icons.Default.Warning
        }
        val iconTint = when {
            neutral -> TextSecondary
            ok -> SuccessText
            else -> WarningText
        }
        val iconBg = when {
            neutral -> BorderColor
            ok -> SuccessBg
            else -> WarningBg
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp),
            )
        }
        Gap(width = 10.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = interFontFamily,
                fontSize = 14.sp,
                color = TextPrimary,
            )
            if (!ok && !neutral && problemHint != null) {
                Text(
                    text = problemHint,
                    fontFamily = interFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp,
                )
            }
        }
        if (onFix != null && (neutral || !ok)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .then(
                        if (neutral) Modifier.border(
                            1.dp,
                            BorderColor,
                            RoundedCornerShape(999.dp),
                        ) else Modifier.background(PrimaryColor)
                    )
                    .clickable { onFix() }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = fixLabel ?: "Fix",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (neutral) TextSecondary else Color.White,
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(
    deviceInfo: DeviceInfo,
    lastSyncTime: Date?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface,
        border = BorderStroke(1.dp, BorderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Status",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
            )
            Gap(height = 12.dp)
            InfoRow("Device", deviceInfo.deviceName)
            InfoRow("Device ID", deviceInfo.deviceId)
            InfoRow("Manufacturer", deviceInfo.manufacturer)
            InfoRow("Android SDK", deviceInfo.sdk)
            InfoRow("Battery", deviceInfo.batteryLevel?.let { "$it%" } ?: "Unknown")
            InfoRow("Network", deviceInfo.network ?: "Unknown")
            InfoRow("App version", deviceInfo.appVersion)
            InfoRow("Last sync", lastSyncTime?.formatStatusDate() ?: "Not yet")
        }
    }
}

@Composable
private fun DiagnosticsCard(events: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface,
        border = BorderStroke(1.dp, BorderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent activity",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
            )
            Gap(height = 8.dp)
            if (events.isEmpty()) {
                Text(
                    text = "Nothing recorded yet.",
                    fontFamily = interFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
            events.forEach { e ->
                Text(
                    text = e,
                    fontFamily = interFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(vertical = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun SimSelectionCard(
    state: DeviceStatusState,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface,
        border = BorderStroke(1.dp, BorderColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Read SMS from",
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
            )
            Gap(height = 6.dp)
            Text(
                text = "Choose which SIM's messages to sync. Use this if the phone has a second SIM whose SMS should be ignored.",
                fontFamily = interFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 19.sp,
            )
            Gap(height = 12.dp)

            if (!hasPermission) {
                Text(
                    text = "Allow phone access so Dakpion can tell which SIM each message arrived on.",
                    fontFamily = interFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 19.sp,
                )
                Gap(height = 10.dp)
                OutlinedButton(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent,
                        contentColor = PrimaryColor,
                    ),
                    border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = "Allow phone access",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                val options: List<Pair<Int, String>> = buildList {
                    if (state.sims.isNotEmpty()) {
                        state.sims.forEach { add(it.slotIndex to it.label) }
                    } else {
                        add(0 to "SIM 1")
                        add(1 to "SIM 2")
                    }
                    add(SimInfoProvider.SLOT_BOTH to "Both SIMs")
                }
                options.forEach { (slot, label) ->
                    SimOptionRow(
                        label = label,
                        selected = state.simSlotFilter == slot,
                        onClick = { onSelect(slot) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SimOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor),
        )
        Gap(width = 4.dp)
        Text(
            text = label,
            fontFamily = interFontFamily,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = TextPrimary,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            fontFamily = interFontFamily,
            fontSize = 13.sp,
            color = TextSecondary,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            fontFamily = interFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextPrimary,
        )
    }
}

@Composable
private fun StatusMessage(
    message: String,
    background: Color,
    color: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = message,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = color,
        )
    }
}

private fun Date.formatStatusDate(): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(this)
}
