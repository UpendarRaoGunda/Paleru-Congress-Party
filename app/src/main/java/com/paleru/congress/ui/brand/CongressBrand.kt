package com.paleru.congress.ui.brand

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BackHand
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Stable, high-contrast Congress colors for illustrations and non-text accents. */
object CongressPalette {
    val Saffron = Color(0xFFFF8A24)
    val SaffronDeep = Color(0xFFA94700)
    val White = Color(0xFFFFFFFF)
    val Green = Color(0xFF138A4B)
    val GreenDeep = Color(0xFF075F36)
    val CongressBlue = Color(0xFF174A8B)
    val CongressBlueDark = Color(0xFF0B2851)
    val Ink = Color(0xFF172019)
    val WarmCanvas = Color(0xFFFFFAF5)
}

@Immutable
data class CongressBrandColors(
    val saffron: Color,
    val saffronStrong: Color,
    val green: Color,
    val greenStrong: Color,
    val hand: Color,
    val flagWhite: Color,
    val softSaffron: Color,
    val softGreen: Color,
    val softBlue: Color,
    val scrim: Color,
    val isDark: Boolean
)

val LightCongressBrandColors = CongressBrandColors(
    saffron = CongressPalette.Saffron,
    saffronStrong = CongressPalette.SaffronDeep,
    green = CongressPalette.Green,
    greenStrong = CongressPalette.GreenDeep,
    hand = CongressPalette.CongressBlue,
    flagWhite = CongressPalette.White,
    softSaffron = Color(0xFFFFE4CC),
    softGreen = Color(0xFFD8F4E3),
    softBlue = Color(0xFFDCE9FF),
    scrim = Color(0xFF07130C),
    isDark = false
)

val DarkCongressBrandColors = CongressBrandColors(
    saffron = Color(0xFFFFB06F),
    saffronStrong = Color(0xFFFFC293),
    green = Color(0xFF69D89B),
    greenStrong = Color(0xFF94E8B7),
    hand = Color(0xFFA9C8FF),
    flagWhite = CongressPalette.White,
    softSaffron = Color(0xFF4A2B18),
    softGreen = Color(0xFF153D29),
    softBlue = Color(0xFF193454),
    scrim = Color(0xFF020704),
    isDark = true
)

val LocalCongressBrandColors = staticCompositionLocalOf { LightCongressBrandColors }

object CongressBrand {
    val colors: CongressBrandColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCongressBrandColors.current
}

/**
 * Compact Congress identity mark built entirely in Compose.
 *
 * Pass a translated [contentDescription], or `null` when a nearby title already names the party.
 */
@Composable
fun CongressBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    contentDescription: String? = "Indian National Congress"
) {
    val colors = CongressBrand.colors
    val accessibility = if (contentDescription == null) {
        Modifier.clearAndSetSemantics { }
    } else {
        Modifier.semantics { this.contentDescription = contentDescription }
    }

    Box(
        modifier = modifier
            .size(size)
            .then(accessibility)
            .shadow(8.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val bandHeight = this.size.height / 3f
            drawRect(colors.saffron, size = androidx.compose.ui.geometry.Size(this.size.width, bandHeight))
            drawRect(
                colors.flagWhite,
                topLeft = androidx.compose.ui.geometry.Offset(0f, bandHeight),
                size = androidx.compose.ui.geometry.Size(this.size.width, bandHeight)
            )
            drawRect(
                colors.green,
                topLeft = androidx.compose.ui.geometry.Offset(0f, bandHeight * 2f),
                size = androidx.compose.ui.geometry.Size(this.size.width, bandHeight + 1f)
            )
        }
        Surface(
            modifier = Modifier.size(size * 0.62f),
            shape = CircleShape,
            color = colors.flagWhite,
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                colors.hand.copy(alpha = 0.18f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.BackHand,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.40f),
                    tint = colors.hand
                )
            }
        }
    }
}

/** A scalable Congress flag motif for headers, empty states, and identity panels. */
@Composable
fun CongressFlag(
    modifier: Modifier = Modifier,
    showHand: Boolean = true,
    contentDescription: String? = "Congress flag"
) {
    val colors = CongressBrand.colors
    val shape = RoundedCornerShape(16.dp)
    val accessibility = if (contentDescription == null) {
        Modifier.clearAndSetSemantics { }
    } else {
        Modifier.semantics { this.contentDescription = contentDescription }
    }

    Box(
        modifier = modifier
            .aspectRatio(1.5f)
            .then(accessibility)
            .shadow(7.dp, shape, clip = false)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f), shape),
        contentAlignment = Alignment.Center
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxSize().background(colors.saffron))
            Box(Modifier.weight(1f).fillMaxSize().background(colors.flagWhite))
            Box(Modifier.weight(1f).fillMaxSize().background(colors.green))
        }
        if (showHand) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = colors.flagWhite,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.BackHand,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = colors.hand
                    )
                }
            }
        }
    }
}

/** A thin premium tricolor accent for cards, section headers, and navigation chrome. */
@Composable
fun CongressTricolorRail(
    modifier: Modifier = Modifier,
    height: Dp = 5.dp
) {
    val colors = CongressBrand.colors
    Row(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .clearAndSetSemantics { }
    ) {
        Box(Modifier.weight(1f).fillMaxSize().background(colors.saffron))
        Box(Modifier.weight(1f).fillMaxSize().background(colors.flagWhite))
        Box(Modifier.weight(1f).fillMaxSize().background(colors.green))
    }
}
