package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.presentation.theme.epilogueFontFamily
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.orcuspay.dakpion.util.ifTrue

@Composable
fun XButton(
    modifier: Modifier = Modifier,
    height: Dp = 54.dp,
    text: String,
    loading: Boolean = false,
    enabled: Boolean = true,
    fillMaxWidth: Boolean = true,
    style: TextStyle? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: () -> Unit = {}
) {

    Button(
        modifier = modifier
            .height(height)
            .ifTrue(fillMaxWidth) {
                Modifier.fillMaxWidth()
            },
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFFD1BEFE),
            contentColor = Color(0xFF102C60),
            disabledBackgroundColor = Color(0xFFD1BEFE).copy(alpha = 0.5f),
            disabledContentColor = Color.White
        ),
        contentPadding = contentPadding,
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp
        )
    ) {
        if (!loading) {
            Text(
                text = text,
                style = style ?: TextStyle(
                    fontFamily = interFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF102C60)
                )
            )
        } else {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}