package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.domain.model.Mode
import com.orcuspay.dakpion.presentation.theme.BorderColor
import com.orcuspay.dakpion.presentation.theme.TextSecondary
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.orcuspay.dakpion.util.ifTrue

@Composable
fun CredentialCard(
    credential: Credential,
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
                if (credential.unauthorized) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_alert),
                            contentDescription = null,
                            tint = Color(0xFFA82C00),
                            modifier = Modifier.size(16.dp)
                        )
                        Gap(width = 4.dp)
                        Text(
                            text = "Invalid credentials",
                            fontFamily = interFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFFA82C00)
                        )
                    }
                    Gap(height = 8.dp)
                }
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = credential.businessName,
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                        fontFamily = interFontFamily,
                    )
                    Gap(width = 8.dp)
                    ApiModeBadge(mode = credential.mode)
                }
                Gap(height = 6.dp)
                Text(
                    text = credential.accessKey,
                    color = TextSecondary,
                    fontFamily = interFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
            XSwitch(
                value = credential.enabled,
                onValueChange = onEnabledChange,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun ApiModeBadge(
    modifier: Modifier = Modifier,
    mode: Mode
) {
    val bgColor = if (mode == Mode.LIVE) Color(0xFFD7F7C2) else Color(0xFFFCEDB9)
    val textColor = if (mode == Mode.LIVE) Color(0xFF043B15) else Color(0xFF5F1A05)
    val label = if (mode == Mode.LIVE) "LIVE" else "TEST"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .wrapContentSize()
                .ifTrue(true) { Modifier },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontFamily = interFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = 1.dp,
                        color = textColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}