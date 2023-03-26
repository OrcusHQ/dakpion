package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
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
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.orcuspay.dakpion.util.ifTrue

@Composable
fun CredentialCard(
    credential: Credential,
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
            if (credential.unauthorized) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.ic_alert
                        ),
                        contentDescription = null,
                        tint = Color(0xFFA82C00)
                    )
                    Gap(width = 4.dp)
                    Text(
                        text = "Invalid credentials",
                        fontFamily = interFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFFA82C00)
                    )
                }
                Gap(height = 10.dp)
            }
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = credential.businessName,
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                    fontFamily = interFontFamily,
                )
                Gap(width = 8.dp)
                ApiModeBadge(
                    mode = credential.mode
                )
            }
            Gap(height = 10.dp)
            Text(
                text = credential.accessKey,
                color = Color(0xFF545969),
                fontFamily = interFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.75f)
            )
        }
        XSwitch(
            value = credential.enabled,
            onValueChange = onEnabledChange,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun ApiModeBadge(
    modifier: Modifier = Modifier,
    mode: Mode
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .ifTrue(
                    mode == Mode.LIVE,
                    ifFalse = {
                        Modifier.background(Color(0xFFFCEDB9))
                    }
                ) {
                    Modifier.background(Color(0xFFD7F7C2))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (mode == Mode.LIVE) "LIVE MODE" else "TEST MODE",
                color = if (mode == Mode.LIVE) Color(0xFF043B15) else Color(0xFF5F1A05),
                fontFamily = interFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}