package com.orcuspay.dakpion.presentation.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.R

// Set of Material typography styles to start with

val epilogueFontFamily = FontFamily(
    Font(R.font.epilogue_regular, weight = FontWeight.Normal),
    Font(R.font.epilogue_medium, weight = FontWeight.Medium),
    Font(R.font.epilogue_semibold, weight = FontWeight.SemiBold),
)

val interFontFamily = FontFamily(
    Font(R.font.inter_medium, weight = FontWeight.Medium),
)

val gilroyFontFamily = FontFamily(
    Font(R.font.gilroy_heavy, weight = FontWeight.Black)
)

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    h1 = TextStyle(
        fontFamily = epilogueFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    ),
    h2 = TextStyle(
        fontFamily = epilogueFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    ),
    h3 = TextStyle(
        fontFamily = epilogueFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    ),
    subtitle1 = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    subtitle2 = TextStyle(
        fontFamily = epilogueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)