package com.orcuspay.dakpion.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun DashboardScreen(
    navigator: DestinationsNavigator,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val totalCount by viewModel.totalPayments.observeAsState(0)
    val senderStats by viewModel.statsBySender.observeAsState(emptyList())
    val statusStats by viewModel.statsByStatus.observeAsState(emptyList())
    val allSms by viewModel.allStoredSMS.observeAsState(emptyList())
    val paymentsByDay = viewModel.getPaymentsByDay(allSms)

    Scaffold(
        topBar = {
            TopBar(title = "Analytics")
        }
    ) { pv ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatCard(title = "Total Payments Detected", value = totalCount.toString())
            }

            item {
                Text(
                    text = "Payments by Day",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = interFontFamily
                )
            }

            items(paymentsByDay.toList()) { (date, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = date, fontFamily = interFontFamily)
                    Text(text = "$count payments", fontWeight = FontWeight.SemiBold, fontFamily = interFontFamily)
                }
            }

            item {
                Text(
                    text = "Payments by Sender",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = interFontFamily
                )
            }

            items(senderStats) { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stat.sender, fontFamily = interFontFamily)
                    Text(text = "${stat.count}", fontWeight = FontWeight.SemiBold, fontFamily = interFontFamily)
                }
            }

            item {
                Text(
                    text = "Status Overview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = interFontFamily
                )
            }

            items(statusStats) { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stat.status.name, fontFamily = interFontFamily)
                    Text(text = "${stat.count}", fontWeight = FontWeight.SemiBold, fontFamily = interFontFamily)
                }
            }
            
            item { Gap(height = 20.dp) }
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F4F8), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD1D9E0), RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color(0xFF545969),
                fontSize = 14.sp,
                fontFamily = interFontFamily
            )
            Gap(height = 8.dp)
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF043B15),
                fontFamily = interFontFamily
            )
        }
    }
}
