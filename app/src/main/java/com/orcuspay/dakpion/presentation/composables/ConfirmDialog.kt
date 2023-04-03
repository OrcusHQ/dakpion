package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.presentation.theme.interFontFamily


@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    title: String,
    cancelLabel: String = "No",
    confirmLabel: String = "Yes",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = 8.dp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier
                .background(Color.White)
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Start,
                fontFamily = interFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        onCancel()
                    }
                ) {
                    Text(
                        text = cancelLabel,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                        fontFamily = interFontFamily,
                        fontSize = 14.sp
                    )
                }
                TextButton(
                    onClick = {
                        onConfirm()
                    }
                ) {
                    Text(
                        text = confirmLabel,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Blue,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                        fontFamily = interFontFamily,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}