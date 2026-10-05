package com.example.shopeeafftracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ShopeeOrange,
    secondary = ShopeeOrangeDark,
    tertiary = CardBorderYellow,
    background = ScreenBackground,
    surface = ScreenBackground
)

@Composable
fun ShopeeAffTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
