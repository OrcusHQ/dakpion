package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.presentation.theme.interFontFamily

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(
    title: String = "",
    showBackButton: Boolean = false,
    actionButton: @Composable () -> Unit = {},
    onBackButtonClicked: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (showBackButton) {
            Gap(width = 16.dp)
            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                IconButton(
                    onClick = {
                        onBackButtonClicked?.invoke()
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Go back",
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground)
                    )
                }
            }
        }
        Text(
            text = title,
            modifier = Modifier
                .padding(
                    start = if (showBackButton) 12.dp else 20.dp
                )
                .weight(1f),
            fontFamily = interFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colors.onBackground,
            overflow = TextOverflow.Ellipsis
        )

        actionButton()
    }
}