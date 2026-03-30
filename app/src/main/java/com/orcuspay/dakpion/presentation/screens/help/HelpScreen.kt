package com.orcuspay.dakpion.presentation.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun HelpScreen(
    navigator: DestinationsNavigator
) {
    Scaffold(
        topBar = {
            TopBar(title = "Help & Support")
        },
        backgroundColor = Color(0xFFF8FAFC)
    ) { pv ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Common Troubleshooting",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1E293B)
                )
                Gap(height = 8.dp)
            }

            item {
                HelpItem(
                    question = "Why are my SMS not syncing?",
                    answer = "Ensure you have granted SMS permissions. Check if the sender is supported (e.g., bKash, Nagad) and matches your filters. Also, ensure your business credentials are valid and 'Enabled' in the Home tab."
                )
            }

            item {
                HelpItem(
                    question = "What is 'Suspicious' status?",
                    answer = "This means the balance in the SMS doesn't match our calculations based on previous transactions. This could indicate a fake SMS or a manual transaction we missed."
                )
            }

            item {
                HelpItem(
                    question = "How to setup filters?",
                    answer = "Go to the Filters tab. Add a sender name (optional) and a value to match (e.g., 'received'). You can also use Regex by wrapping the value in slashes, like /payment/."
                )
            }

            item {
                HelpItem(
                    question = "Battery Optimization",
                    answer = "Some devices kill background apps. Please disable battery optimization for Dakpion in your phone settings to ensure 24/7 sync."
                )
            }

            item {
                HelpItem(
                    question = "Supported Senders",
                    answer = "We currently support bKash, Nagad, Upay, Cellfin (IBBL), Pathao Pay, iPay, Tap, and TeleCash. More are being added!"
                )
            }

            item {
                Gap(height = 24.dp)
                Text(
                    text = "Need more help?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFontFamily,
                    color = Color(0xFF1E293B)
                )
                Gap(height = 8.dp)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 0.dp,
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Contact Support",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = interFontFamily
                        )
                        Text(
                            text = "Email us at support@orcuspay.com for any technical issues.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = interFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HelpItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                fontFamily = interFontFamily,
                color = Color(0xFF1E293B)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF64748B)
            )
        }
        if (expanded) {
            Gap(height = 8.dp)
            Text(
                text = answer,
                fontSize = 14.sp,
                color = Color(0xFF475569),
                fontFamily = interFontFamily,
                lineHeight = 20.sp
            )
        }
    }
}
