package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.orcuspay.dakpion.domain.model.Credential

@Composable
fun CredentialCard(
    credential: Credential,
    checked: Boolean,
    onCheckedChange: (value: Boolean) -> Unit,
) {

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = credential.businessName)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = credential.mode)
        }
        XSwitch(
            value = checked,
            onValueChange = onCheckedChange,
            modifier = Modifier.padding(end = 16.dp)
        )
    }

}