package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.domain.model.Filter
import com.orcuspay.dakpion.presentation.theme.BorderColor
import com.orcuspay.dakpion.presentation.theme.TextSecondary
import com.orcuspay.dakpion.presentation.theme.interFontFamily


@Composable
fun FilterCard(
    filter: Filter,
    onEnabledChange: (value: Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = filter.sender ?: "All Senders",
                    fontSize = 15.sp,
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontFamily = interFontFamily,
                )

                Gap(height = 4.dp)

                Text(
                    text = filter.value,
                    color = TextSecondary,
                    fontFamily = interFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Gap(height = 8.dp)

                FilterTypeBadge(isRegex = filter.isValidRegex())
            }
            XSwitch(
                value = filter.enabled,
                onValueChange = onEnabledChange,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun FilterTypeBadge(
    modifier: Modifier = Modifier,
    isRegex: Boolean,
) {
    val bgColor = if (isRegex) Color(0xFFF2EBFF) else Color(0xFFD4E2FC)
    val textColor = if (isRegex) Color(0xFF3F32A1) else Color(0xFF102C60)
    val label = if (isRegex) "REGEX" else "EXACT MATCH"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontFamily = interFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}