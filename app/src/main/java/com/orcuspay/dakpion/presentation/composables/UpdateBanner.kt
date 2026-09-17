package com.orcuspay.dakpion.presentation.composables

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.orcuspay.dakpion.presentation.theme.PrimaryColor
import com.orcuspay.dakpion.presentation.theme.interFontFamily

/**
 * A soft "update available" banner. It asks Google Play whether a newer version
 * of Dakpion has been published and, if so, prompts the merchant to update from
 * the Play Store. Play only reports an update for builds INSTALLED FROM PLAY, so
 * side-loaded builds simply never show it (the banner stays hidden). Dismissible
 * for the session. Render it above the tab content so it shows on any screen.
 */
@Composable
fun UpdateBanner(forceVisibleForPreview: Boolean = false) {
    val context = LocalContext.current
    var updateAvailable by remember { mutableStateOf(forceVisibleForPreview) }
    var dismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val manager = AppUpdateManagerFactory.create(context)
            manager.appUpdateInfo
                .addOnSuccessListener { info ->
                    if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        updateAvailable = true
                    }
                }
                .addOnFailureListener { }
        } catch (_: Exception) {
        }
    }

    if (!updateAvailable || dismissed) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Update available",
                color = Color.White,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                text = "A newer version of Dakpion is on the Play Store.",
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = interFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        Gap(width = 10.dp)
        Text(
            text = "Update",
            color = PrimaryColor,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .clickable { openPlayStore(context) }
                .padding(horizontal = 14.dp, vertical = 7.dp),
        )
        Gap(width = 2.dp)
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable { dismissed = true }
                .padding(6.dp),
        )
    }
}

private fun openPlayStore(context: Context) {
    val pkg = context.packageName
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(market)
    } catch (_: Exception) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$pkg"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
