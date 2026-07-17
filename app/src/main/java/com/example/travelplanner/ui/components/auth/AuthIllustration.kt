package com.example.travelplanner.ui.components.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AuthIllustration(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "plane")
    val planeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "planeOffset"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val mapColor = if (isDark) Color.White.copy(alpha = 0.06f) else primaryColor.copy(alpha = 0.08f)
            val continentPath = Path().apply {
                moveTo(w * 0.1f, h * 0.55f)
                cubicTo(w * 0.15f, h * 0.35f, w * 0.35f, h * 0.3f, w * 0.5f, h * 0.4f)
                cubicTo(w * 0.65f, h * 0.5f, w * 0.75f, h * 0.35f, w * 0.9f, h * 0.45f)
                lineTo(w * 0.85f, h * 0.7f)
                cubicTo(w * 0.7f, h * 0.75f, w * 0.55f, h * 0.65f, w * 0.4f, h * 0.72f)
                cubicTo(w * 0.25f, h * 0.78f, w * 0.15f, h * 0.68f, w * 0.1f, h * 0.55f)
                close()
            }
            drawPath(continentPath, mapColor)

            val flightPath = Path().apply {
                moveTo(w * 0.15f, h * 0.7f)
                quadraticBezierTo(w * 0.45f, h * 0.2f, w * 0.85f, h * 0.35f)
            }
            drawPath(
                flightPath,
                color = tertiaryColor.copy(alpha = 0.5f),
                style = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                )
            )

            val pinPositions = listOf(
                Offset(w * 0.25f, h * 0.5f),
                Offset(w * 0.55f, h * 0.35f),
                Offset(w * 0.75f, h * 0.55f)
            )
            pinPositions.forEach { pos ->
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.4f),
                    radius = 8f,
                    center = pos
                )
            }
        }

        Icon(
            imageVector = Icons.Default.Flight,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .offset(
                    x = ((planeOffset - 0.5f) * 40).dp,
                    y = ((0.5f - planeOffset) * 20).dp
                ),
            tint = tertiaryColor
        )

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.TopEnd)
                .padding(end = 24.dp, top = 16.dp),
            tint = secondaryColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AuthBackground(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val background = MaterialTheme.colorScheme.background

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            background,
                            primary.copy(alpha = 0.15f),
                            background
                        )
                    } else {
                        listOf(
                            background,
                            primary.copy(alpha = 0.06f),
                            secondary.copy(alpha = 0.04f),
                            background
                        )
                    }
                )
            )
        }
    }
}
