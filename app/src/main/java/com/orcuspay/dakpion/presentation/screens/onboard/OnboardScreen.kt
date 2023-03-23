package com.orcuspay.dakpion.presentation.screens.onboard

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.XButton
import com.orcuspay.dakpion.presentation.destinations.ContainerScreenDestination
import com.orcuspay.dakpion.presentation.destinations.HomeScreenDestination
import com.orcuspay.dakpion.presentation.destinations.OnboardScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Destination
fun OnboardScreen(
    navigator: DestinationsNavigator
) {

    val smsPermissionState = rememberPermissionState(
        android.Manifest.permission.READ_SMS
    ) {
        if (it) {
            navigator.navigate(ContainerScreenDestination) {
                popUpTo(OnboardScreenDestination) {
                    inclusive = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Gap(height = 60.dp)
        Image(
            painter = painterResource(id = R.drawable.onboarding),
            contentDescription = "",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Gap(height = 24.dp)
        Text(text = "You’re almost done", style = MaterialTheme.typography.h1)
        Gap(height = 8.dp)
        Text(
            text = "Let Dakpion access your transactional SMSs to integrate with OrcusPay.",
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.secondary,
            textAlign = TextAlign.Center
        )
        Gap(height = 100.dp)
        XButton(
            text = "Continue",
        ) {
            smsPermissionState.launchPermissionRequest()
        }
    }
}