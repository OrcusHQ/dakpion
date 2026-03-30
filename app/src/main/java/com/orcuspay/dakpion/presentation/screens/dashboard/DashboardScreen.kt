package com.orcuspay.dakpion.presentation.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orcuspay.dakpion.domain.model.SMSStatus
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.screens.logs.SMSStatusBadge
import com.orcuspay.dakpion.presentation.theme.*
import com.orcuspay.dakpion.util.toLogTimeFormat
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

    val todayCount = viewModel.getTodayCount(allSms)
    val weekCount = viewModel.getThisWeekCount(allSms)
    val totalAmount = viewModel.getTotalAmount(allSms)
    val recentByDay = viewModel.getRecentPaymentsByDay(allSms)

    val statusMap = statusStats.associateBy { it.status }
    val suspiciousCount = statusMap[SMSStatus.SUSPICIOUS]?.count ?: 0
    val errorCount = statusMap[SMSStatus.ERROR]?.count ?: 0
    val totalAllSms = statusStats.sumOf { it.count }
    val successRate = if (totalAllSms > 0) totalCount * 100 / totalAllSms else 0

    Scaffold(
        topBar = { TopBar(title = "Analytics") }
    ) { pv ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Gap(height = 4.dp) }

            // Hero stat card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp,
                    backgroundColor = PrimaryColor
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "TOTAL STORED",
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 0.8.sp
                        )
                        Gap(height = 6.dp)
                        Text(
                            text = totalCount.toString(),
                            fontFamily = interFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                            color = Color.White
                        )
                        Gap(height = 6.dp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "SUCCESS $successRate%",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "TODAY $todayCount",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2-column mini stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        modifier = Modifier.weight(1f),
                        label = "This Week",
                        value = weekCount.toString(),
                        valueColor = TextPrimary
                    )
                    MiniStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Suspicious",
                        value = suspiciousCount.toString(),
                        valueColor = if (suspiciousCount > 0) ErrorText else TextPrimary,
                        bgColor = if (suspiciousCount > 0) ErrorBg else MaterialTheme.colors.surface
                    )
                    MiniStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Errors",
                        value = errorCount.toString(),
                        valueColor = if (errorCount > 0) ErrorText else TextPrimary,
                        bgColor = if (errorCount > 0) ErrorBg else MaterialTheme.colors.surface
                    )
                }
            }

            // Total amount card — only if amount data exists
            if (totalAmount > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = 0.dp,
                        backgroundColor = MaterialTheme.colors.surface,
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Amount Synced",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Gap(height = 2.dp)
                                Text(
                                    text = "from $totalCount payments",
                                    fontFamily = interFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "৳${String.format("%,.2f", totalAmount)}",
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = PrimaryColor
                            )
                        }
                    }
                }
            }

            // Recent Activity — Last 7 Days
            if (recentByDay.isNotEmpty()) {
                item {
                    SectionCard(title = "Last 7 Days") {
                        recentByDay.forEachIndexed { index, (date, count) ->
                            if (index > 0) Divider(color = BorderColor)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = date,
                                    fontFamily = interFontFamily,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SuccessBg)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SuccessText,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Payments
            if (allSms.isNotEmpty()) {
                item {
                    SectionCard(title = "Recent Payments") {
                        allSms.take(5).forEachIndexed { index, sms ->
                            if (index > 0) Divider(color = BorderColor)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sms.sender,
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = sms.date.toLogTimeFormat(),
                                        fontFamily = interFontFamily,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                if (sms.amount != null) {
                                    Text(
                                        text = "৳${String.format("%,.2f", sms.amount)}",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = PrimaryColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top Senders
            if (senderStats.isNotEmpty()) {
                item {
                    SectionCard(title = "Top Senders") {
                        senderStats.forEachIndexed { index, stat ->
                            if (index > 0) Divider(color = BorderColor)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stat.sender,
                                    fontFamily = interFontFamily,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (totalCount > 0) {
                                        val pct = stat.count * 100 / totalCount
                                        Text(
                                            text = "$pct%",
                                            fontFamily = interFontFamily,
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = "${stat.count}",
                                        fontFamily = interFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = PrimaryColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Status Breakdown
            if (statusStats.isNotEmpty()) {
                item {
                    SectionCard(title = "Status Breakdown") {
                        statusStats.forEachIndexed { index, stat ->
                            if (index > 0) Divider(color = BorderColor)
                            val fraction =
                                if (totalAllSms > 0) stat.count.toFloat() / totalAllSms else 0f
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SMSStatusBadge(
                                    status = stat.status,
                                    modifier = Modifier.width(110.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BorderColor)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction)
                                            .background(statusBarColor(stat.status))
                                    )
                                }
                                Text(
                                    text = "${(fraction * 100).toInt()}%",
                                    fontFamily = interFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(34.dp)
                                )
                                Text(
                                    text = "${stat.count}",
                                    fontFamily = interFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Gap(height = 20.dp) }
        }
    }
}

private fun statusBarColor(status: SMSStatus): Color = when (status) {
    SMSStatus.STORED -> Color(0xFF21A038)
    SMSStatus.PROCESSING -> Color(0xFF3B82F6)
    SMSStatus.ERROR -> Color(0xFFE11D48)
    SMSStatus.SUSPICIOUS -> Color(0xFFE11D48)
    SMSStatus.UNAUTHORIZED -> Color(0xFFE11D48)
    SMSStatus.DUPLICATE -> Color(0xFFE11D48)
    SMSStatus.FILTERED -> Color(0xFFF59E0B)
    SMSStatus.NOT_STORED -> Color(0xFFF59E0B)
}

@Composable
fun MiniStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    bgColor: Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        backgroundColor = bgColor ?: MaterialTheme.colors.surface,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontFamily = interFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Gap(height = 4.dp)
            Text(
                text = value,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = valueColor
            )
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column {
            Text(
                text = title,
                fontFamily = interFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
            Divider(color = BorderColor)
            content()
        }
    }
}

@Composable
fun SectionRow(left: String, right: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = left,
            fontFamily = interFontFamily,
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSurface
        )
        Text(
            text = right,
            fontFamily = interFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = PrimaryColor
        )
    }
}
