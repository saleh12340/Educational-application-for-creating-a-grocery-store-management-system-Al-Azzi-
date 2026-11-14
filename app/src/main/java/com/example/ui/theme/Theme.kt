package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val AlAzziLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PureWhite,
    secondary = DarkCardPrimary,
    onSecondary = TextOnDark,
    tertiary = AmberDebt,
    onTertiary = DarkCardPrimary,
    background = PureWhite,
    onBackground = TextOnWhite,
    surface = PureWhite,
    onSurface = TextOnWhite,
    surfaceVariant = LightInputSurface,
    onSurfaceVariant = TextMutedOnWhite,
    outline = CrispDivider,
    error = CrimsonDanger,
    onError = PureWhite
)

@Composable
fun AlAzziPosTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = AlAzziLightColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
