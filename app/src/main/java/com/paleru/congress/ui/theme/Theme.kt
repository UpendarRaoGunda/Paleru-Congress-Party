package com.paleru.congress.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paleru.congress.ui.brand.DarkCongressBrandColors
import com.paleru.congress.ui.brand.LightCongressBrandColors
import com.paleru.congress.ui.brand.LocalCongressBrandColors

private val LightColors = lightColorScheme(
    primary = Color(0xFF075F36),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F4E3),
    onPrimaryContainer = Color(0xFF002112),
    secondary = Color(0xFFA94700),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE4CC),
    onSecondaryContainer = Color(0xFF351000),
    tertiary = Color(0xFF174A8B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE9FF),
    onTertiaryContainer = Color(0xFF001B3D),
    background = Color(0xFFFFFAF5),
    onBackground = Color(0xFF172019),
    surface = Color(0xFFFFFAF5),
    onSurface = Color(0xFF172019),
    surfaceVariant = Color(0xFFE6EAE3),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF717971),
    outlineVariant = Color(0xFFC1C9C1),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    inverseSurface = Color(0xFF2B322C),
    inverseOnSurface = Color(0xFFF0F2ED),
    inversePrimary = Color(0xFF69D89B),
    surfaceTint = Color(0xFF075F36),
    scrim = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF69D89B),
    onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF075F36),
    onPrimaryContainer = Color(0xFFD8F4E3),
    secondary = Color(0xFFFFB06F),
    onSecondary = Color(0xFF562000),
    secondaryContainer = Color(0xFF793400),
    onSecondaryContainer = Color(0xFFFFE4CC),
    tertiary = Color(0xFFA9C8FF),
    onTertiary = Color(0xFF003063),
    tertiaryContainer = Color(0xFF174A8B),
    onTertiaryContainer = Color(0xFFDCE9FF),
    background = Color(0xFF101411),
    onBackground = Color(0xFFE0E6DF),
    surface = Color(0xFF101411),
    onSurface = Color(0xFFE0E6DF),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC1C9C1),
    outline = Color(0xFF8B938B),
    outlineVariant = Color(0xFF414942),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE0E6DF),
    inverseOnSurface = Color(0xFF2B322C),
    inversePrimary = Color(0xFF075F36),
    surfaceTint = Color(0xFF69D89B),
    scrim = Color.Black
)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "tnum"
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun PaleruCongressTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCongressBrandColors provides if (darkTheme) {
            DarkCongressBrandColors
        } else {
            LightCongressBrandColors
        }
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
