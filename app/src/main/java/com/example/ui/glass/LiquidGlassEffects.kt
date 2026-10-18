package com.example.ui.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DeepMatteBlack
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderFaint
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassFillMedium
import com.example.ui.theme.GlassFillThin
import com.example.ui.theme.GlassFillUltraThin

/**
 * Ambient background with deep matte black and subtle blurred liquid light orbs
 */
@Composable
fun LiquidAmbientBackground(
    glowIntensity: Float = 1.0f,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_liquid_shift")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepMatteBlack)
    ) {
        // Ambient soft light refraction canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-right soft ethereal glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x18FFFFFF).copy(alpha = 0.10f * glowIntensity),
                        Color(0x08FFFFFF).copy(alpha = 0.04f * glowIntensity),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.75f, height * 0.15f),
                    radius = (width * 0.65f) * pulse1
                )
            )

            // Center-left faint ambient liquid orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x12FFFFFF).copy(alpha = 0.08f * glowIntensity),
                        Color(0x05FFFFFF).copy(alpha = 0.02f * glowIntensity),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.2f, height * 0.55f),
                    radius = (width * 0.55f) * pulse2
                )
            )

            // Bottom-right fluid ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x15FFFFFF).copy(alpha = 0.09f * glowIntensity),
                        Color(0x06FFFFFF).copy(alpha = 0.03f * glowIntensity),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.6f, height * 0.88f),
                    radius = (width * 0.7f) * pulse1
                )
            )
        }

        content()
    }
}

/**
 * Reusable modifier applying liquid glass specular highlight borders,
 * frosted inner transparency, and organic rounded corners.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    fillAlpha: Float = 0.10f,
    borderAlpha: Float = 0.35f,
    elevation: Dp = 12.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color(0x30FFFFFF),
        spotColor = Color(0x50000000)
    )
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = fillAlpha * 1.5f),
                Color.White.copy(alpha = fillAlpha * 0.7f),
                Color.White.copy(alpha = fillAlpha * 0.4f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = borderAlpha * 0.5f),
                Color.White.copy(alpha = borderAlpha * 0.15f),
                Color.White.copy(alpha = borderAlpha * 0.35f)
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 400f)
        ),
        shape = shape
    )

/**
 * Capsule variant of liquid glass for message previews, status bar, and search bar
 */
fun Modifier.liquidGlassCapsule(
    fillAlpha: Float = 0.12f,
    borderAlpha: Float = 0.40f
): Modifier = this.liquidGlass(
    shape = RoundedCornerShape(22.dp),
    fillAlpha = fillAlpha,
    borderAlpha = borderAlpha,
    elevation = 8.dp
)

/**
 * Orb variant for avatars and circular glass buttons
 */
fun Modifier.liquidGlassOrb(
    fillAlpha: Float = 0.15f,
    borderAlpha: Float = 0.50f
): Modifier = this
    .shadow(
        elevation = 10.dp,
        shape = CircleShape,
        ambientColor = Color(0x40FFFFFF),
        spotColor = Color(0x60000000)
    )
    .clip(CircleShape)
    .background(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = fillAlpha * 1.6f),
                Color.White.copy(alpha = fillAlpha * 0.8f),
                Color.White.copy(alpha = fillAlpha * 0.3f)
            ),
            center = Offset(30f, 30f)
        )
    )
    .border(
        width = 1.2.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha),
                Color.White.copy(alpha = borderAlpha * 0.2f),
                Color.White.copy(alpha = borderAlpha * 0.6f)
            )
        ),
        shape = CircleShape
    )
