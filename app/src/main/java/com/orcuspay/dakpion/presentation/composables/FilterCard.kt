package com.orcuspay.dakpion.presentation.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.orcuspay.dakpion.util.ifTrue


@Composable
fun FilterCard(
    filter: Filter,
    onEnabledChange: (value: Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 24.dp, top = 18.dp, bottom = 16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                text = filter.sender ?: "All Sender",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontFamily = interFontFamily,
            )

            Gap(height = 4.dp)

            Text(
                text = filter.value,
                color = Color(0xFF545969),
                fontFamily = interFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.75f)
            )

            Gap(height = 10.dp)

            FilterTypeBadge(
                isRegex = filter.isValidRegex()
            )
        }
        XSwitch(
            value = filter.enabled,
            onValueChange = onEnabledChange,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun FilterTypeBadge(
    modifier: Modifier = Modifier,
    isRegex: Boolean,
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .ifTrue(
                    isRegex,
                    ifFalse = {
                        Modifier.background(Color(0xFFD4E2FC))
                    }
                ) {
                    Modifier.background(Color(0xFFF2EBFF))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRegex) "REGEX" else "EXACT MATCH",
                color = if (isRegex) Color(0xFF3F32A1) else Color(0xFF102C60),
                fontFamily = interFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}