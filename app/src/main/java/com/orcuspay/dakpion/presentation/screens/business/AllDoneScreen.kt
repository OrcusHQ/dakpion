package com.orcuspay.dakpion.presentation.screens.business

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
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
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.XButton
import com.orcuspay.dakpion.presentation.theme.gilroyFontFamily
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun AllDoneScreen(
    navigator: DestinationsNavigator
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Gap(height = 120.dp)
        Image(
            painter = painterResource(id = R.drawable.all_done),
            contentDescription = "",
            modifier = Modifier.size(175.dp),
            contentScale = ContentScale.FillWidth
        )
        Gap(height = 18.dp)
        Text(
            text = "ALL DONE!",
            fontSize = 50.sp,
            color = Color.Black,
            fontFamily = gilroyFontFamily,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Gap(height = 8.dp)
        Text(
            text = "We will be forwarding your transactional SMSs.",
            fontSize = 16.sp,
            color = Color(0xFF545969),
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Divider(
                    thickness = 1.dp,
                    color = Color(0xFFEBEEF1)
                )
                XButton(
                    text = "Got it",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    navigator.navigateUp()
                    navigator.navigateUp()
                }
            }
        }
    }
}