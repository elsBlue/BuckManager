package com.buckmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.buckmanager.app.model.Envelope
import com.buckmanager.app.model.FundGoalConfig
import com.buckmanager.app.model.HeaderCardConfig
import com.buckmanager.app.model.formatRp
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.GoldAccent
import com.buckmanager.app.utils.customCardStyle

@Composable
fun CardShell(
    backgroundColorHex: String,
    fallbackBg: Color,
    backgroundImageUri: String?,
    dimOpacity: Int,
    useGradient: Boolean,
    gradientColors: List<String>,
    gradientAngle: Float,
    radiusTopLeft: Int,
    radiusTopRight: Int,
    radiusBottomRight: Int,
    radiusBottomLeft: Int,
    borderTop: Int,
    borderRight: Int,
    borderBottom: Int,
    borderLeft: Int,
    borderColorHex: String,
    borderFallback: Color = Color.Gray,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val hasPhoto = !backgroundImageUri.isNullOrBlank()
    val fill = if (hasPhoto) Color.Transparent else parseHexColor(backgroundColorHex, fallbackBg)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .customCardStyle(
                shape = RoundedCornerShape(
                    topStart = radiusTopLeft.dp,
                    topEnd = radiusTopRight.dp,
                    bottomEnd = radiusBottomRight.dp,
                    bottomStart = radiusBottomLeft.dp
                ),
                backgroundColor = fill,
                useGradient = useGradient && !hasPhoto,
                gradientColors = gradientColors.map { parseHexColor(it) },
                gradientAngle = gradientAngle,
                borderTop = borderTop.dp,
                borderRight = borderRight.dp,
                borderBottom = borderBottom.dp,
                borderLeft = borderLeft.dp,
                borderColor = parseHexColor(borderColorHex, borderFallback)
            )
    ) {
        if (hasPhoto) {
            AsyncImage(
                model = backgroundImageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = (dimOpacity / 100f).coerceIn(0f, 0.98f)))
            )
        }
        content()
    }
}

@Composable
fun HeaderCardLook(
    cardKey: String,
    config: HeaderCardConfig,
    valueText: String,
    showEdit: Boolean = false,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val fallback = when (cardKey) {
        "income" -> Color(0xFFF4F7FE)
        "expense" -> Color(0xFFFDF9ED)
        else -> Color(0xFF3673FC)
    }
    CardShell(
        backgroundColorHex = config.backgroundColorHex,
        fallbackBg = fallback,
        backgroundImageUri = config.backgroundImageUri,
        dimOpacity = config.dimOpacity,
        useGradient = config.useGradient,
        gradientColors = config.gradientColors,
        gradientAngle = config.gradientAngle,
        radiusTopLeft = config.radiusTopLeft,
        radiusTopRight = config.radiusTopRight,
        radiusBottomRight = config.radiusBottomRight,
        radiusBottomLeft = config.radiusBottomLeft,
        borderTop = config.borderTop,
        borderRight = config.borderRight,
        borderBottom = config.borderBottom,
        borderLeft = config.borderLeft,
        borderColorHex = config.borderColorHex,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(
                start = config.paddingLeft.dp,
                top = config.paddingTop.dp,
                end = config.paddingRight.dp,
                bottom = config.paddingBottom.dp
            )
        ) {
            when (cardKey) {
                "income", "expense" -> {
                    val isIncome = cardKey == "income"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isIncome) Color(0xFF3673FC) else Color(0xFFFCBF36)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isIncome) "INCOME" else "EXPENSE",
                                color = parseHexColor(config.labelColorHex, Color(0xFF9CA3AF)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        if (showEdit) HeaderEditDot(24.dp, onEdit)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = valueText,
                        color = parseHexColor(config.valueColorHex, if (isIncome) Color(0xFF10B981) else Color(0xFFFB7185)),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "NET WORTH",
                                color = parseHexColor(config.labelColorHex, Color.White.copy(alpha = 0.8f)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = valueText,
                                color = parseHexColor(config.valueColorHex, Color.White),
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp
                            )
                        }
                        if (showEdit) HeaderEditDot(32.dp, onEdit)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderEditDot(size: androidx.compose.ui.unit.Dp, onEdit: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onEdit),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Edit, contentDescription = "Edit Card", tint = Color.Black, modifier = Modifier.size(if (size < 28.dp) 14.dp else 16.dp))
    }
}

@Composable
fun EnvelopeLook(
    envelope: Envelope,
    remainingText: String,
    caption: String,
    isDarkMode: Boolean,
    onClick: () -> Unit = {},
    slider: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    CardShell(
        backgroundColorHex = envelope.backgroundColorHex,
        fallbackBg = if (isDarkMode) Color(0xFF181C26) else Color.White,
        backgroundImageUri = envelope.backgroundImageUri,
        dimOpacity = envelope.dimOpacity,
        useGradient = envelope.useGradient,
        gradientColors = envelope.gradientColors,
        gradientAngle = envelope.gradientAngle,
        radiusTopLeft = envelope.radiusTopLeft,
        radiusTopRight = envelope.radiusTopRight,
        radiusBottomRight = envelope.radiusBottomRight,
        radiusBottomLeft = envelope.radiusBottomLeft,
        borderTop = envelope.borderTop,
        borderRight = envelope.borderRight,
        borderBottom = envelope.borderBottom,
        borderLeft = envelope.borderLeft,
        borderColorHex = envelope.borderColorHex,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(
                        start = envelope.paddingLeft.dp,
                        top = envelope.paddingTop.dp,
                        end = envelope.paddingRight.dp,
                        bottom = if (envelope.paddingBottom > 8) (envelope.paddingBottom - 8).dp else envelope.paddingBottom.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(envelope.colorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconVector(envelope.iconName),
                        contentDescription = envelope.name,
                        tint = parseHexColor(envelope.colorHex),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${envelope.name.ifBlank { "Envelope" }} · ${envelope.percentage}%",
                        color = parseHexColor(envelope.labelColorHex, Color(0xFF64748B)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = remainingText,
                        color = parseHexColor(envelope.valueColorHex, if (isDarkMode) Color.White else Color(0xFF0F172A)),
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = caption,
                        color = parseHexColor(envelope.descriptionColorHex, Color(0xFF9CA3AF)),
                        fontSize = 11.sp
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(24.dp))
            }
            slider?.invoke()
        }
    }
}

@Composable
fun EnvelopeAllocationSlider(colorHex: String, value: Float, onChange: (Float) -> Unit, onFinished: () -> Unit, paddingH: Int) {
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    androidx.compose.material3.Slider(
        value = value,
        onValueChange = onChange,
        onValueChangeFinished = onFinished,
        valueRange = 0f..100f,
        steps = 99,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingH.dp, vertical = 0.dp)
            .padding(bottom = 8.dp),
        thumb = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(parseHexColor(colorHex), CircleShape)
            )
        },
        track = {
            val fraction = value / 100f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                parseHexColor(colorHex).copy(alpha = 0.3f),
                                parseHexColor(colorHex).copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = fraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(parseHexColor(colorHex), RoundedCornerShape(2.dp))
                )
            }
        }
    )
}

fun goalFontFamily(name: String): FontFamily = when (name) {
    "serif" -> FontFamily.Serif
    "mono" -> FontFamily.Monospace
    else -> FontFamily.SansSerif
}

@Composable
fun FundGoalLook(
    config: FundGoalConfig,
    isDarkMode: Boolean,
    hideBalances: Boolean = false,
    currencySymbol: String = "Rp",
    showChrome: Boolean = false,
    showEdit: Boolean = false,
    onDeposit: () -> Unit = {},
    onEdit: () -> Unit = {},
    onPinWidget: () -> Unit = {},
    onSetGoal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progressRatio = if (config.targetAmount > 0) {
        (config.currentAmount / config.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val percentageInt = (progressRatio * 100).toInt()
    val remainingAmount = (config.targetAmount - config.currentAmount).coerceAtLeast(0.0)
    val labelColor = parseHexColor(config.labelColorHex, GoldAccent)
    val iconColor = parseHexColor(config.iconColorHex, labelColor)
    val nameColor = parseHexColor(config.nameColorHex, parseHexColor(config.valueColorHex, if (isDarkMode) Color.White else Color(0xFF121926)))
    val percentColor = parseHexColor(config.percentColorHex, nameColor)
    val currentColor = parseHexColor(config.currentSavedColorHex, nameColor)
    val targetColor = parseHexColor(config.targetAmountColorHex, labelColor)
    val remainingColor = parseHexColor(config.remainingColorHex, labelColor)
    val progressFill = parseHexColor(config.progressFillColorHex, labelColor)
    val progressTrack = parseHexColor(
        config.progressTrackColorHex,
        if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
    )
    val nameFont = goalFontFamily(config.nameFontFamily)

    CardShell(
        backgroundColorHex = config.backgroundColorHex,
        fallbackBg = if (isDarkMode) Color(0xFF181C26) else Color.White,
        backgroundImageUri = config.backgroundImageUri,
        dimOpacity = config.dimOpacity,
        useGradient = config.useGradient,
        gradientColors = config.gradientColors,
        gradientAngle = config.gradientAngle,
        radiusTopLeft = config.radiusTopLeft,
        radiusTopRight = config.radiusTopRight,
        radiusBottomRight = config.radiusBottomRight,
        radiusBottomLeft = config.radiusBottomLeft,
        borderTop = config.borderTop,
        borderRight = config.borderRight,
        borderBottom = config.borderBottom,
        borderLeft = config.borderLeft,
        borderColorHex = config.borderColorHex,
        borderFallback = labelColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(
                start = config.paddingLeft.dp,
                top = config.paddingTop.dp,
                end = config.paddingRight.dp,
                bottom = config.paddingBottom.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(getIconVector(config.iconName), contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = config.name.ifBlank { "My Goal" },
                            color = nameColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = nameFont
                        )
                        Text(
                            text = "TARGET SAVINGS",
                            color = targetColor.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$percentageInt%",
                        color = percentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    if (showChrome) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.25f))
                                .clickable(onClick = onPinWidget),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Widgets, contentDescription = "Pin Widget", tint = iconColor, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (showEdit) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable(onClick = onEdit),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Goal settings", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (config.targetAmount <= 0.0) {
                Text(
                    text = "Set a target when you are ready. For now, record income first.",
                    color = labelColor.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = onSetGoal,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = parseHexColor(config.btnBgColorHex, labelColor)),
                    shape = RoundedCornerShape(AppShape.button)
                ) {
                    Text("Set a goal", color = parseHexColor(config.btnTextColorHex, Color.White), fontWeight = FontWeight.Bold)
                }
            } else {
                Column {
                    Text(
                        text = if (hideBalances) currencySymbol + "••••••" else formatRp(config.currentAmount),
                        color = currentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Target: ${formatRp(config.targetAmount)}", color = targetColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (remainingAmount <= 0) "Goal reached!" else "Remaining: ${if (hideBalances) currencySymbol + "•••••" else formatRp(remainingAmount)}",
                            color = if (remainingAmount <= 0) Color(0xFF34D399) else remainingColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { progressRatio },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = progressFill,
                    trackColor = progressTrack
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Surface(
                        onClick = onDeposit,
                        shape = RoundedCornerShape(AppShape.button),
                        color = parseHexColor(config.btnBgColorHex, labelColor)
                    ) {
                        Text(
                            text = "+ Deposit",
                            color = parseHexColor(config.btnTextColorHex, Color.White),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalWidgetLook(config: FundGoalConfig, modifier: Modifier = Modifier) {
    val progressRatio = if (config.targetAmount > 0) {
        (config.currentAmount / config.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val percentageInt = (progressRatio * 100).toInt()
    val labelColor = parseHexColor(config.labelColorHex, Color(0xFFD4A54A))
    val iconColor = parseHexColor(config.iconColorHex, labelColor)
    val nameColor = parseHexColor(config.nameColorHex, parseHexColor(config.valueColorHex, Color.White))
    val percentColor = parseHexColor(config.percentColorHex, labelColor)
    val currentColor = parseHexColor(config.currentSavedColorHex, nameColor)
    val targetColor = parseHexColor(config.targetAmountColorHex, labelColor)
    val remainingColor = parseHexColor(config.remainingColorHex, labelColor)
    val progressFill = parseHexColor(config.progressFillColorHex, labelColor)
    val progressTrack = parseHexColor(config.progressTrackColorHex, Color.White.copy(alpha = 0.2f))
    val nameFont = goalFontFamily(config.nameFontFamily)

    CardShell(
        backgroundColorHex = config.backgroundColorHex,
        fallbackBg = Color(0xFF181C26),
        backgroundImageUri = config.backgroundImageUri,
        dimOpacity = config.dimOpacity,
        useGradient = config.useGradient,
        gradientColors = config.gradientColors,
        gradientAngle = config.gradientAngle,
        radiusTopLeft = config.radiusTopLeft,
        radiusTopRight = config.radiusTopRight,
        radiusBottomRight = config.radiusBottomRight,
        radiusBottomLeft = config.radiusBottomLeft,
        borderTop = config.borderTop,
        borderRight = config.borderRight,
        borderBottom = config.borderBottom,
        borderLeft = config.borderLeft,
        borderColorHex = config.borderColorHex,
        borderFallback = labelColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = config.paddingLeft.dp,
                    top = config.paddingTop.dp,
                    end = config.paddingRight.dp,
                    bottom = config.paddingBottom.dp
                )
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(getIconVector(config.iconName), contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = config.name.ifBlank { "Target Savings" },
                    color = nameColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = nameFont,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Text(text = "$percentageInt%", color = percentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatRp(config.currentAmount),
                    color = currentColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Target: ${formatRp(config.targetAmount)}",
                    color = targetColor,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Remaining: ${formatRp((config.targetAmount - config.currentAmount).coerceAtLeast(0.0))}",
                color = remainingColor,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp)),
                color = progressFill,
                trackColor = progressTrack
            )
        }
    }
}

fun liveHeaderConfig(
    backgroundColorHex: String,
    backgroundImageUri: String?,
    dimOpacity: Int,
    radiusTopLeft: Int,
    radiusTopRight: Int,
    radiusBottomRight: Int,
    radiusBottomLeft: Int,
    borderWidth: Int,
    borderColorHex: String,
    paddingTop: Int,
    paddingRight: Int,
    paddingBottom: Int,
    paddingLeft: Int,
    useGradient: Boolean,
    gradientColors: List<String>,
    gradientAngle: Float,
    labelColorHex: String,
    valueColorHex: String
) = HeaderCardConfig(
    backgroundColorHex = backgroundColorHex,
    backgroundImageUri = backgroundImageUri,
    dimOpacity = dimOpacity,
    radiusTopLeft = radiusTopLeft,
    radiusTopRight = radiusTopRight,
    radiusBottomRight = radiusBottomRight,
    radiusBottomLeft = radiusBottomLeft,
    borderTop = borderWidth,
    borderRight = borderWidth,
    borderBottom = borderWidth,
    borderLeft = borderWidth,
    borderColorHex = borderColorHex,
    paddingTop = paddingTop,
    paddingRight = paddingRight,
    paddingBottom = paddingBottom,
    paddingLeft = paddingLeft,
    useGradient = useGradient,
    gradientColors = gradientColors,
    gradientAngle = gradientAngle,
    labelColorHex = labelColorHex,
    valueColorHex = valueColorHex
)
