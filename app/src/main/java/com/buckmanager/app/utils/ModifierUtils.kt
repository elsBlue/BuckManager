package com.buckmanager.app.utils

import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

fun Modifier.customCardStyle(
    shape: Shape,
    backgroundColor: Color,
    useGradient: Boolean = false,
    gradientColors: List<Color> = emptyList(),
    gradientAngle: Float = 0f,
    borderTop: Dp,
    borderRight: Dp,
    borderBottom: Dp,
    borderLeft: Dp,
    borderColor: Color
): Modifier {
    val isUniformBorder = borderTop == borderRight && borderRight == borderBottom && borderBottom == borderLeft && borderTop > 0.dp
    
    return this.then(
        Modifier
            .clip(shape)
            .drawWithCache {
                val bgBrush = if (useGradient && gradientColors.isNotEmpty()) {
                    val angleRad = (gradientAngle % 360) * (Math.PI / 180f)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = kotlin.math.sqrt((size.width / 2) * (size.width / 2) + (size.height / 2) * (size.height / 2))
                    val start = Offset(
                        (center.x - radius * cos(angleRad)).toFloat(),
                        (center.y - radius * sin(angleRad)).toFloat()
                    )
                    val end = Offset(
                        (center.x + radius * cos(angleRad)).toFloat(),
                        (center.y + radius * sin(angleRad)).toFloat()
                    )
                    Brush.linearGradient(
                        colors = if (gradientColors.size == 1) listOf(gradientColors[0], gradientColors[0]) else gradientColors,
                        start = start,
                        end = end
                    )
                } else {
                    SolidColor(backgroundColor)
                }

                val outline = shape.createOutline(size, layoutDirection, this)

                onDrawBehind {
                    drawPath(outline.toPath(), brush = bgBrush)

                    if (!isUniformBorder) {
                        val topPx = borderTop.toPx()
                        val rightPx = borderRight.toPx()
                        val bottomPx = borderBottom.toPx()
                        val leftPx = borderLeft.toPx()

                        if (topPx > 0 || rightPx > 0 || bottomPx > 0 || leftPx > 0) {
                            val innerOutline = if (outline is androidx.compose.ui.graphics.Outline.Rounded) {
                                val rr = outline.roundRect
                                androidx.compose.ui.graphics.Outline.Rounded(
                                    androidx.compose.ui.geometry.RoundRect(
                                        left = rr.left + leftPx,
                                        top = rr.top + topPx,
                                        right = rr.right - rightPx,
                                        bottom = rr.bottom - bottomPx,
                                        topLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                            maxOf(0f, rr.topLeftCornerRadius.x - leftPx),
                                            maxOf(0f, rr.topLeftCornerRadius.y - topPx)
                                        ),
                                        topRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                            maxOf(0f, rr.topRightCornerRadius.x - rightPx),
                                            maxOf(0f, rr.topRightCornerRadius.y - topPx)
                                        ),
                                        bottomRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                            maxOf(0f, rr.bottomRightCornerRadius.x - rightPx),
                                            maxOf(0f, rr.bottomRightCornerRadius.y - bottomPx)
                                        ),
                                        bottomLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                            maxOf(0f, rr.bottomLeftCornerRadius.x - leftPx),
                                            maxOf(0f, rr.bottomLeftCornerRadius.y - bottomPx)
                                        )
                                    )
                                )
                            } else if (outline is androidx.compose.ui.graphics.Outline.Rectangle) {
                                val rect = outline.rect
                                androidx.compose.ui.graphics.Outline.Rectangle(
                                    androidx.compose.ui.geometry.Rect(
                                        left = rect.left + leftPx,
                                        top = rect.top + topPx,
                                        right = rect.right - rightPx,
                                        bottom = rect.bottom - bottomPx
                                    )
                                )
                            } else {
                                null
                            }

                            if (innerOutline != null) {
                                val outerPath = outline.toPath()
                                val innerPath = innerOutline.toPath()
                                val borderPath = androidx.compose.ui.graphics.Path()
                                borderPath.op(outerPath, innerPath, androidx.compose.ui.graphics.PathOperation.Difference)
                                drawPath(borderPath, color = borderColor)
                            } else {
                                if (topPx > 0) drawRect(color = borderColor, topLeft = Offset.Zero, size = Size(size.width, topPx))
                                if (rightPx > 0) drawRect(color = borderColor, topLeft = Offset(size.width - rightPx, 0f), size = Size(rightPx, size.height))
                                if (bottomPx > 0) drawRect(color = borderColor, topLeft = Offset(0f, size.height - bottomPx), size = Size(size.width, bottomPx))
                                if (leftPx > 0) drawRect(color = borderColor, topLeft = Offset.Zero, size = Size(leftPx, size.height))
                            }
                        }
                    }
                }
            }
            .then(if (isUniformBorder) Modifier.border(borderTop, borderColor, shape) else Modifier)
    )
}

private fun androidx.compose.ui.graphics.Outline.toPath(): androidx.compose.ui.graphics.Path {
    return when (this) {
        is androidx.compose.ui.graphics.Outline.Rectangle -> androidx.compose.ui.graphics.Path().apply { addRect(this@toPath.rect) }
        is androidx.compose.ui.graphics.Outline.Rounded -> androidx.compose.ui.graphics.Path().apply { addRoundRect(this@toPath.roundRect) }
        is androidx.compose.ui.graphics.Outline.Generic -> this.path
    }
}
