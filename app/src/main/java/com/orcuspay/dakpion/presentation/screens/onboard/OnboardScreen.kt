package com.orcuspay.dakpion.presentation.screens.onboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.XButton
import com.orcuspay.dakpion.presentation.destinations.ContainerScreenDestination
import com.orcuspay.dakpion.presentation.destinations.OnboardScreenDestination
import com.orcuspay.dakpion.presentation.theme.gilroyFontFamily
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Destination
fun OnboardScreen(
    navigator: DestinationsNavigator,
    viewModel: OnboardViewModel = hiltViewModel()
) {

    val smsPermissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.RECEIVE_SMS,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Gap(height = 120.dp)
        Image(
            painter = painterResource(id = R.drawable.onboarding),
            contentDescription = "",
            modifier = Modifier.size(175.dp),
            contentScale = ContentScale.FillWidth
        )
        Gap(height = 24.dp)
        Text(
            text = "GET STARTED!",
            fontSize = 50.sp,
            color = Color.Black,
            fontFamily = gilroyFontFamily,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Gap(height = 8.dp)
        Text(
            text = "Let Dakpion access your transactional SMSs to integrate with OrcusPay.",
            fontSize = 16.sp,
            color = Color(0xFF545969),
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            XButton(
                text = "Continue",
            ) {
                smsPermissionState.launchMultiplePermissionRequest()
                viewModel.addDefaultFilters()
            }
        }
    }
}