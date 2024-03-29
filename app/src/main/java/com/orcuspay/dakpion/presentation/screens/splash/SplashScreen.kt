package com.orcuspay.dakpion.presentation.screens.splash

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.presentation.destinations.ContainerScreenDestination
import com.orcuspay.dakpion.presentation.destinations.OnboardScreenDestination
import com.orcuspay.dakpion.presentation.destinations.SplashScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Destination
@Composable
@RootNavGraph(start = true)
fun SplashScreen(
    navigator: DestinationsNavigator,
    viewModel: SplashViewModel = hiltViewModel()
) {

    val smsPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
        )
    )
    LaunchedEffect(Unit) {
        delay(1000L)
        if (smsPermissionState.allPermissionsGranted) {
            navigator.navigate(ContainerScreenDestination) {
                popUpTo(SplashScreenDestination) {
                    inclusive = true
                }
            }
        } else {
            navigator.navigate(OnboardScreenDestination) {
                popUpTo(SplashScreenDestination) {
                    inclusive = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painterResource(id = R.drawable.onboarding),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
    }
}