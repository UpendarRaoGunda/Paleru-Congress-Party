package com.paleru.congress.ui.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

enum class CongressAccent {
    Tricolor,
    Saffron,
    Green,
    Blue,
    None
}

/** A calm branded app background with restrained saffron and green light. */
@Composable
fun CongressBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = CongressBrand.colors
    val scheme = MaterialTheme.colorScheme
    val brush = if (colors.isDark) {
        Brush.linearGradient(
            0.0f to colors.softSaffron.copy(alpha = 0.22f),
            0.34f to scheme.background,
            0.70f to scheme.background,
            1.0f to colors.softGreen.copy(alpha = 0.22f)
        )
    } else {
        Brush.linearGradient(
            0.0f to colors.softSaffron.copy(alpha = 0.48f),
            0.32f to scheme.background,
            0.72f to scheme.background,
            1.0f to colors.softGreen.copy(alpha = 0.48f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush),
        content = content
    )
}

/**
 * High-emphasis surface for the home header or a key party activity.
 * Content keeps the normal theme foreground color, so Telugu and English remain readable.
 */
@Composable
fun CongressHeroSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(22.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CongressBrand.colors
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    val brush = if (colors.isDark) {
        Brush.linearGradient(
            listOf(
                colors.softSaffron.copy(alpha = 0.56f),
                scheme.surface,
                colors.softGreen.copy(alpha = 0.50f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                colors.softSaffron.copy(alpha = 0.72f),
                scheme.surface,
                colors.softGreen.copy(alpha = 0.72f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.72f), shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/** A consistent premium card with an optional party-color accent rail. */
@Composable
fun CongressCard(
    modifier: Modifier = Modifier,
    accent: CongressAccent = CongressAccent.Tricolor,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CongressBrand.colors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 1.dp else 3.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.64f)
        )
    ) {
        Column(Modifier.fillMaxWidth()) {
            CongressAccentRail(accent = accent)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
private fun CongressAccentRail(
    accent: CongressAccent,
    modifier: Modifier = Modifier
) {
    val colors = CongressBrand.colors
    when (accent) {
        CongressAccent.Tricolor -> CongressTricolorRail(
            modifier = modifier.fillMaxWidth(),
            height = 5.dp
        )
        CongressAccent.Saffron -> Box(
            modifier.fillMaxWidth().background(colors.saffron).padding(top = 5.dp)
        )
        CongressAccent.Green -> Box(
            modifier.fillMaxWidth().background(colors.green).padding(top = 5.dp)
        )
        CongressAccent.Blue -> Box(
            modifier.fillMaxWidth().background(colors.hand).padding(top = 5.dp)
        )
        CongressAccent.None -> Unit
    }
}

/** Returns a readable foreground for user-supplied or status-chip backgrounds. */
fun contentColorFor(background: Color): Color =
    if (background.luminance() > 0.48f) Color(0xFF111812) else Color.White
