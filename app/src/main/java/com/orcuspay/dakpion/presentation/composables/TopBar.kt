package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.orcuspay.dakpion.R

@Composable
fun TopBar(
    title: String? = null,
    showBackButton: Boolean = false,
    onBackButtonClicked: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (showBackButton) {
            IconButton(
                onClick = {
                    onBackButtonClicked?.invoke()
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Go back"
                )
            }
        }
        if (title != null)
            Text(
                text = title,
                modifier = Modifier.padding(
                    start = if (showBackButton) 12.dp else 16.dp
                ),
                style = MaterialTheme.typography.h1
            )
    }
}