package com.orcuspay.dakpion.presentation.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorPalette = lightColors(
    primary = PrimaryColor,
    primaryVariant = AccentColor,
    onPrimary = Color.White,
    secondary = SecondaryColor,
    background = Color.White
)

@Composable
fun DakpionTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = LightColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}