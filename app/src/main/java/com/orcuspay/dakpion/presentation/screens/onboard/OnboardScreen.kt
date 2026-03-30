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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.OnboardingAgreementFooter
import com.orcuspay.dakpion.presentation.composables.XButton
import com.orcuspay.dakpion.presentation.screens.destinations.ContainerScreenDestination
import com.orcuspay.dakpion.presentation.screens.destinations.DisclosureScreenDestination
import com.orcuspay.dakpion.presentation.screens.destinations.OnboardScreenDestination
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
            modifier = Modifier.size(width = 220.dp, height = 215.dp),
            contentScale = ContentScale.FillWidth
        )
        Gap(height = 32.dp)
        Text(
            text = "Get Started",
            fontSize = 36.sp,
            color = Color.Black,
            fontFamily = gilroyFontFamily,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Gap(height = 12.dp)
        Text(
            text = "Configure Dakpion to start automating your payments with Orcus.",
            fontSize = 15.sp,
            color = Color(0xFF64748B),
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                XButton(
                    text = "Continue",
                ) {
                    viewModel.addDefaultFilters()
                    navigator.navigate(DisclosureScreenDestination)
                }

                Gap(height = 16.dp)

                OnboardingAgreementFooter()
            }
        }
    }
}