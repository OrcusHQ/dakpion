package com.orcuspay.dakpion.presentation.screens.logs

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.theme.epilogueFontFamily
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.orcuspay.dakpion.util.ifTrue
import com.orcuspay.dakpion.util.toLogTimeFormat
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun SMSLogScreen(
    navigator: DestinationsNavigator,
    viewModel: SMSLogViewModel = hiltViewModel()
) {

    val credentialWithSMSList by viewModel.getCredentialsWithSMS()
        .observeAsState(initial = listOf())
    val credentials = credentialWithSMSList.map { it.credential }
    var selectedCredential by remember {
        mutableStateOf(0)
    }
    val smsList = credentialWithSMSList.getOrNull(selectedCredential)?.smsList ?: listOf()
    val groupedSMS = viewModel.groupSMSByDate(smsList)

    Log.d("kraken", "Grouped: $groupedSMS")
    Scaffold(
        topBar = {
            TopBar(title = "Logs")
        }
    ) { pv ->
        Column(
            modifier = Modifier.padding(pv),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            Gap(height = 10.dp)
            LazyRow {
                items(credentials.size) { i ->
                    val credential = credentials[i]

                    Row(
                        modifier = Modifier
                            .ifTrue(selectedCredential != i) {
                                Modifier.clickable {
                                    selectedCredential = i
                                }
                            }
                            .padding(start = 16.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == selectedCredential) Color(0xFFF2EBFF)
                                else Color(0xFFF6F8FA)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (credential.icon == null) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 7.dp, bottom = 7.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colors.primary,
                                                MaterialTheme.colors.primaryVariant
                                            )
                                        )
                                    )
                            )
                        } else {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(credential.icon)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 7.dp, bottom = 7.dp)
                                    .clip(shape = CircleShape)
                                    .size(22.dp)
                            )
                        }
                        Gap(width = 8.dp)
                        Text(
                            text = credential.businessName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontFamily = interFontFamily,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
            Gap(height = 16.dp)

            if (groupedSMS.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Gap(height = 80.dp)
                    Image(
                        painter = painterResource(id = R.drawable.empty),
                        contentDescription = "",
                        modifier = Modifier
                            .size(175.dp),
                        contentScale = ContentScale.Fit
                    )
                    Gap(height = 4.dp)
                    Text(
                        text = "Everything is empty here",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )
                    Gap(height = 8.dp)
                    Text(
                        text = "Your SMS logs will be here",
                        fontFamily = interFontFamily,
                        color = Color(0xFF545969),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn {
                    groupedSMS.forEach { (date, smsGroup) ->
                        item {
                            Text(
                                text = date,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.Black,
                                fontFamily = interFontFamily,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 5.dp,
                                    bottom = 5.dp
                                ),
                            )
                        }

                        items(smsGroup.size) { i ->
                            val sms = smsGroup[i]

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 5.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFEBEEF1),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                            ) {
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.Top,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = sms.sender,
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = sms.date.toLogTimeFormat(),
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp,
                                            color = Color(0xFF545969)
                                        )
                                    }
                                    Gap(height = 8.dp)
                                    Text(
                                        text = sms.body,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Gap(height = 8.dp)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        SMSStatusBadge(status = sms.status)
                                        Gap(width = 12.dp)
                                        if (sms.status == SMSStatus.ERROR) {
                                            Box(
                                                modifier = Modifier.clickable {
                                                    viewModel.retry(
                                                        credential = credentials[selectedCredential],
                                                        sms = sms
                                                    )
                                                }
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_retry),
                                                        contentDescription = "",
                                                        tint = Color(0xFF635BFF)
                                                    )
                                                    Gap(width = 4.dp)
                                                    Text(
                                                        text = "Retry now",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontFamily = interFontFamily,
                                                        color = Color(0xFF635BFF)
                                                    )
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SMSStatusBadge(
    modifier: Modifier = Modifier,
    status: SMSStatus
) {

    val backgroundColor = when (status) {
        SMSStatus.PROCESSING -> Color(0xFFD4E2FC)
        SMSStatus.ERROR -> Color(0xFFFFE7F2)
        SMSStatus.STORED -> Color(0xFFD7F7C2)
        SMSStatus.NOT_STORED -> Color(0xFFFCEDB9)
        SMSStatus.UNAUTHORIZED -> Color(0xFFFFE7F2)
        SMSStatus.DUPLICATE -> Color(0xFFFFE7F2)
    }

    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            val text = when (status) {
                SMSStatus.PROCESSING -> "PROCESSING"
                SMSStatus.ERROR -> "ERROR"
                SMSStatus.STORED -> "STORED IN DB"
                SMSStatus.NOT_STORED -> "NOT STORED IN DB"
                SMSStatus.UNAUTHORIZED -> "UNAUTHORIZED"
                SMSStatus.DUPLICATE -> "DUPLICATE"
            }

            val color = when (status) {
                SMSStatus.PROCESSING -> Color(0xFF102C60)
                SMSStatus.ERROR -> Color(0xFF68052B)
                SMSStatus.STORED -> Color(0xFF043B15)
                SMSStatus.NOT_STORED -> Color(0xFF5F1A05)
                SMSStatus.UNAUTHORIZED -> Color(0xFF68052B)
                SMSStatus.DUPLICATE -> Color(0xFF68052B)
            }

            Text(
                text = text,
                color = color,
                fontFamily = interFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}