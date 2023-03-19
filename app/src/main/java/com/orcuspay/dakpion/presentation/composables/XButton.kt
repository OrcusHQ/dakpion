package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.presentation.theme.epilogueFontFamily
import com.orcuspay.dakpion.util.ifTrue

@Composable
fun XButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    style: TextStyle? = null,
    onClick: () -> Unit = {}
) {

    Button(
        modifier = modifier.ifTrue(fillMaxWidth) {
            Modifier.fillMaxWidth()
        },
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primaryVariant,
            contentColor = Color.White,
            disabledBackgroundColor = MaterialTheme.colors.secondary,
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = text,
            style = style ?: TextStyle(
                fontFamily = epilogueFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}