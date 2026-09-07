package com.buckmanager.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.buckmanager.app.ui.GoldAccent
import kotlin.random.Random

@Composable
fun ParticleEffectCanvas(effectType: String) {
    if (effectType == "none") return

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    val particles = remember {
        List(25) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.5f,
                size = Random.nextFloat() * 4f + 2f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            when (effectType) {
                "starfall" -> {
                    val currentY = ((particle.y + progress * particle.speed) % 1.0f) * height
                    val currentX = particle.x * width
                    drawCircle(
                        color = GoldAccent.copy(alpha = 0.4f),
                        radius = particle.size,
                        center = Offset(currentX, currentY)
                    )
                }
                "lines" -> {
                    val currentX = ((particle.x + progress * particle.speed) % 1.0f) * width
                    val currentY = particle.y * height
                    drawLine(
                        color = Color(0xFF5B9CF5).copy(alpha = 0.3f),
                        start = Offset(currentX, currentY),
                        end = Offset(currentX + 30f, currentY + 15f),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float
)
