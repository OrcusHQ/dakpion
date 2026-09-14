package com.orcuspay.dakpion.presentation.screens.senders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.theme.PrimaryColor
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination

// Sender key (as sent by HQ / matched in the whitelist) -> friendly label.
private val KNOWN_SENDERS = listOf(
    "bkash" to "bKash",
    "nagad" to "Nagad",
    "16216" to "Rocket",
    "upay" to "Upay",
    "ibbl" to "Cellfin (IBBL)",
    "16259" to "mCash",
    "pathaopay" to "PathaoPay",
    "telecash" to "Telecash",
    "ipay" to "iPay",
    "tap" to "Tap",
    "01847-348685" to "OkWallet",
)

@Destination
@Composable
fun MutedSendersScreen(
    viewModel: MutedSendersViewModel = hiltViewModel(),
) {
    val disabled = viewModel.disabled

    Scaffold(
        topBar = { TopBar(title = "SMS Senders") },
        backgroundColor = MaterialTheme.colors.background,
    ) { pv ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = "Turn a sender off to stop forwarding its SMS from this phone. This only affects this device and never enables a sender your account didn't allow.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    fontFamily = interFontFamily,
                    lineHeight = 18.sp,
                )
                Gap(height = 4.dp)
            }

            items(KNOWN_SENDERS) { pair ->
                val key = pair.first
                val label = pair.second
                val active = !disabled.contains(key)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colors.surface)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B),
                    )
                    Switch(
                        checked = active,
                        onCheckedChange = { checked -> viewModel.setMuted(key, !checked) },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor),
                    )
                }
            }
        }
    }
}
