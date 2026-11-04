package com.payra.calling.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PayraLightScheme = lightColorScheme(
    primary = PayraColors.Primary,
    onPrimary = Color.White,
    secondary = PayraColors.AccentLight,
    onSecondary = PayraColors.Ink,
    background = PayraColors.Background,
    onBackground = PayraColors.Ink,
    surface = PayraColors.Background,
    onSurface = PayraColors.Ink,
    onSurfaceVariant = PayraColors.Muted,
    error = PayraColors.Danger,
)

@Composable
fun PayraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PayraLightScheme,
        typography = PayraTypography,
        content = content,
    )
}
