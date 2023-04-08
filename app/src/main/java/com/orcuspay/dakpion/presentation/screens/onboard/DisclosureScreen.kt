package com.orcuspay.dakpion.presentation.screens.onboard

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.OnboardingAgreementFooter
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.composables.XButton
import com.orcuspay.dakpion.presentation.destinations.ContainerScreenDestination
import com.orcuspay.dakpion.presentation.destinations.OnboardScreenDestination
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo


@OptIn(ExperimentalPermissionsApi::class)
@Destination
@Composable
fun DisclosureScreen(
    navigator: DestinationsNavigator,
) {
    val smsPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
        )
    ) {
        if (it.all { p -> p.value }) {
            navigator.navigate(ContainerScreenDestination) {
                popUpTo(OnboardScreenDestination) {
                    inclusive = true
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopBar(showBackButton = true) {
                navigator.navigateUp()
            }
        }
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Text(
                    text = """
                    Our payment automation app requires access to your SMS messages to accurately track your payment transactions.

                    We understand that privacy is of utmost importance to you, and we take every measure to protect your data. To ensure this, we process only transactional messages from specific, Orcus-supported senders.

                    All data is transmitted securely to our servers, and we do not collect any personal information from your SMS messages. You can view which messages are being sent and stored on our server through the app log, giving you full transparency.

                    Rest assured that your payment transactions are safe and secure with our payment automation app.
                """.trimIndent(),
                    fontSize = 16.sp,
                    color = Color(0xFF545969),
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Left,
                    lineHeight = 22.sp,
                )
            }
            Gap(height = 20.dp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                XButton(
                    text = "Give SMS permission",
                ) {
                    smsPermissionState.launchMultiplePermissionRequest()
                }

                Gap(height = 16.dp)

                OnboardingAgreementFooter()
            }
        }
    }
}