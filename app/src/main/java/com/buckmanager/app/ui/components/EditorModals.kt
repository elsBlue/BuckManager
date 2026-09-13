package com.buckmanager.app.ui.components
import com.buckmanager.app.utils.customCardStyle
import com.buckmanager.app.utils.persistBackgroundImage
import androidx.compose.material.icons.filled.Delete
import com.buckmanager.app.model.Envelope


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.graphics.luminance

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.items

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.buckmanager.app.model.*
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.AppStroke
import com.buckmanager.app.ui.GoldAccent
import com.buckmanager.app.ui.appTextFieldColors

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke

@Composable
fun CompactSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String = "dp",
    isDarkMode: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), modifier = Modifier.width(52.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f).height(24.dp),
            colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
        )
        Text("${value.toInt()}$unit", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
fun LinkedInsetControl(
    title: String,
    topStart: Int,
    topEnd: Int,
    bottomEnd: Int,
    bottomStart: Int,
    onChange: (Int, Int, Int, Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String = "",
    eachLabel: String,
    isDarkMode: Boolean
) {
    val muted = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val allSame = topStart == topEnd && topEnd == bottomEnd && bottomEnd == bottomStart
    var editEach by remember(topStart, topEnd, bottomEnd, bottomStart) { mutableStateOf(!allSame) }
    val shared = if (allSame) topStart else ((topStart + topEnd + bottomEnd + bottomStart) / 4)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = if (editEach) "Use one value" else eachLabel,
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { editEach = !editEach }
            )
        }
        if (!editEach) {
            CompactSlider(
                label = "All",
                value = shared.toFloat(),
                onValueChange = {
                    val v = it.toInt()
                    onChange(v, v, v, v)
                },
                valueRange = valueRange,
                unit = unit,
                isDarkMode = isDarkMode
            )
        } else {
            CompactSlider(label = "Top L", value = topStart.toFloat(), onValueChange = { onChange(it.toInt(), topEnd, bottomEnd, bottomStart) }, valueRange = valueRange, unit = unit, isDarkMode = isDarkMode)
            CompactSlider(label = "Top R", value = topEnd.toFloat(), onValueChange = { onChange(topStart, it.toInt(), bottomEnd, bottomStart) }, valueRange = valueRange, unit = unit, isDarkMode = isDarkMode)
            CompactSlider(label = "Bot L", value = bottomStart.toFloat(), onValueChange = { onChange(topStart, topEnd, bottomEnd, it.toInt()) }, valueRange = valueRange, unit = unit, isDarkMode = isDarkMode)
            CompactSlider(label = "Bot R", value = bottomEnd.toFloat(), onValueChange = { onChange(topStart, topEnd, it.toInt(), bottomStart) }, valueRange = valueRange, unit = unit, isDarkMode = isDarkMode)
        }
        if (!editEach) {
            Text("Tip: $eachLabel if you want a ticket / speech-bubble shape.", color = muted, fontSize = 11.sp)
        }
    }
}

@Composable
fun QuickLooksRow(
    selectedBg: String,
    isDarkMode: Boolean,
    onApply: (bg: String, label: String, value: String, radius: Int, padding: Int, gradient: Boolean, g1: String, g2: String, border: Int, borderColor: String) -> Unit
) {
    data class Look(
        val name: String,
        val bg: String,
        val label: String,
        val value: String,
        val radius: Int,
        val padding: Int,
        val gradient: Boolean = false,
        val g1: String = bg,
        val g2: String = bg,
        val border: Int = 0,
        val borderColor: String = "#E2E8F0"
    )
    val looks = listOf(
        Look("Soft blue", "#3673FC", "#E8EEFF", "#FFFFFF", 24, 20),
        Look("Ink", "#0F172A", "#94A3B8", "#FFFFFF", 20, 20),
        Look("Gold", "#1A1408", "#FCBF36", "#FFFFFF", 28, 22, border = 1, borderColor = "#FCBF36"),
        Look("Glass", "#FFFFFF", "#64748B", "#0F172A", 24, 20, border = 1, borderColor = "#E2E8F0"),
        Look("Sunset", "#3673FC", "#FFFFFF", "#FFFFFF", 24, 20, gradient = true, g1 = "#3673FC", g2 = "#FCBF36")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quick looks", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(looks) { look ->
                val selected = !look.gradient && selectedBg.equals(look.bg, ignoreCase = true)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clickable {
                            onApply(look.bg, look.label, look.value, look.radius, look.padding, look.gradient, look.g1, look.g2, look.border, look.borderColor)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (look.gradient) Modifier.background(
                                    Brush.linearGradient(listOf(parseHexColor(look.g1), parseHexColor(look.g2)))
                                ) else Modifier.background(parseHexColor(look.bg))
                            )
                            .border(
                                2.dp,
                                if (selected) GoldAccent else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(look.name, fontSize = 10.sp, color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), maxLines = 1)
                }
            }
        }
    }
}

val ColorPresets = listOf("#1D2A96", "#EC407A", "#FFB300", "#F5B041", "#10B981", "#FB7185", "#0F1117", "#F0F4FA", "#8C98B2", "#26A69A")
val IconPresets = listOf("flag", "star", "home", "wallet", "money", "heart", "flight", "shopping", "gift", "coffee", "car", "book")
val PresetBackgroundImages = listOf(
    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&q=80",
    "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=600&q=80",
    "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=600&q=80",
    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&q=80",
    "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&q=80"
)

fun getIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "home" -> Icons.Default.Home
        "gamepad", "game-controller" -> Icons.Default.SportsEsports
        "wallet" -> Icons.Default.AccountBalanceWallet
        "flight", "airplane" -> Icons.Default.Flight
        "shopping" -> Icons.Default.ShoppingCart
        "drink" -> Icons.Default.LocalDrink
        "car" -> Icons.Default.DirectionsCar
        "heart" -> Icons.Default.Favorite
        "health" -> Icons.Default.MonitorHeart
        "book" -> Icons.Default.MenuBook
        "laptop" -> Icons.Default.Laptop
        "gift" -> Icons.Default.CardGiftcard
        "coffee" -> Icons.Default.LocalCafe
        "money" -> Icons.Default.Payments
        "building" -> Icons.Default.Apartment
        "flag" -> Icons.Default.Flag
        "star" -> Icons.Default.Star
        "phone" -> Icons.Default.Phone
        else -> Icons.Default.Folder
    }
}

@Composable
fun ImageCropModal(
    imageUri: String?,
    visible: Boolean,
    onDismiss: () -> Unit,
    onCropConfirm: (String) -> Unit
) {
    if (!visible || imageUri.isNullOrBlank()) return

    var rotationAngle by remember(imageUri) { mutableFloatStateOf(0f) }
    var aspectMode by remember(imageUri) { mutableIntStateOf(0) }
    
    var containerWidth by remember { mutableFloatStateOf(0f) }
    var containerHeight by remember { mutableFloatStateOf(0f) }
    var rectWidth by remember { mutableFloatStateOf(0f) }
    var rectHeight by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0D0C15)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { rotationAngle = (rotationAngle + 90f) % 360f }) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Rotate",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = { aspectMode = (aspectMode + 1) % 3 }) {
                            Icon(
                                imageVector = Icons.Default.CropOriginal,
                                contentDescription = "Aspect Ratio",
                                tint = Color.White
                            )
                        }

                        TextButton(
                            onClick = {
                                if (containerWidth > 0 && containerHeight > 0) {
                                    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        val croppedUri = com.buckmanager.app.utils.cropImageBitmap(
                                            context = context,
                                            imageUri = imageUri,
                                            containerWidth = containerWidth,
                                            containerHeight = containerHeight,
                                            rectOffsetX = offsetX,
                                            rectOffsetY = offsetY,
                                            rectWidth = rectWidth,
                                            rectHeight = rectHeight,
                                            rotation = rotationAngle
                                        )
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            onCropConfirm(croppedUri ?: persistBackgroundImage(context, imageUri) ?: imageUri)
                                        }
                                    }
                                } else {
                                    onCropConfirm(persistBackgroundImage(context, imageUri) ?: imageUri)
                                }
                            }
                        ) {
                            Text(
                                "CROP",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Interactive Crop Frame View
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    val cw = constraints.maxWidth.toFloat()
                    val ch = constraints.maxHeight.toFloat()
                    
                    LaunchedEffect(cw, ch) {
                        containerWidth = cw
                        containerHeight = ch
                        // Initialize only once on layout
                        if (rectWidth == 0f) {
                            rectWidth = cw * 0.85f
                            rectHeight = cw * 0.85f
                            offsetX = (cw - rectWidth) / 2f
                            offsetY = (ch - rectHeight) / 2f
                        }
                    }

                    LaunchedEffect(aspectMode) {
                        if (containerWidth > 0) {
                            if (aspectMode == 1) { // 1:1
                                val size = minOf(containerWidth, containerHeight) * 0.85f
                                rectWidth = size
                                rectHeight = size
                            } else if (aspectMode == 2) { // 16:9
                                val w = containerWidth * 0.95f
                                val h = w * (9f / 16f)
                                rectWidth = w
                                rectHeight = h
                            }
                            offsetX = (containerWidth - rectWidth) / 2f
                            offsetY = (containerHeight - rectHeight) / 2f
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Crop Image Target",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(rotationZ = rotationAngle)
                        )
                    }

                    // 3x3 Rule of Thirds Grid Overlay
                    val density = LocalDensity.current
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                            .size(
                                width = with(density) { rectWidth.toDp() },
                                height = with(density) { rectHeight.toDp() }
                            )
                            .border(1.4.dp, Color.White, RoundedCornerShape(2.dp))
                            .pointerInput(aspectMode) {
                                // Allow dragging the whole box
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offsetX = (offsetX + dragAmount.x).coerceIn(0f, containerWidth - rectWidth)
                                    offsetY = (offsetY + dragAmount.y).coerceIn(0f, containerHeight - rectHeight)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = androidx.compose.ui.geometry.Offset(w / 3f, 0f),
                                end = androidx.compose.ui.geometry.Offset(w / 3f, h),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = androidx.compose.ui.geometry.Offset(2 * w / 3f, 0f),
                                end = androidx.compose.ui.geometry.Offset(2 * w / 3f, h),
                                strokeWidth = 1f
                            )

                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = androidx.compose.ui.geometry.Offset(0f, h / 3f),
                                end = androidx.compose.ui.geometry.Offset(w, h / 3f),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = androidx.compose.ui.geometry.Offset(0f, 2 * h / 3f),
                                end = androidx.compose.ui.geometry.Offset(w, 2 * h / 3f),
                                strokeWidth = 1f
                            )
                        }

                        // Corner markers
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(48.dp)
                                .offset(x = (-16).dp, y = (-16).dp)
                                .pointerInput(aspectMode) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (aspectMode != 0) return@detectDragGestures // Lock if not Free mode
                                        val newWidth = (rectWidth - dragAmount.x).coerceAtLeast(100f)
                                        val newHeight = (rectHeight - dragAmount.y).coerceAtLeast(100f)
                                        offsetX = (offsetX + (rectWidth - newWidth)).coerceAtLeast(0f)
                                        offsetY = (offsetY + (rectHeight - newHeight)).coerceAtLeast(0f)
                                        rectWidth = newWidth
                                        rectHeight = newHeight
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).border(BorderStroke(4.dp, Color.White), RoundedCornerShape(topStart = 4.dp)))
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(48.dp)
                                .offset(x = 16.dp, y = (-16).dp)
                                .pointerInput(aspectMode) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (aspectMode != 0) return@detectDragGestures
                                        val newWidth = (rectWidth + dragAmount.x).coerceAtLeast(100f).coerceAtMost(containerWidth - offsetX)
                                        val newHeight = (rectHeight - dragAmount.y).coerceAtLeast(100f)
                                        offsetY = (offsetY + (rectHeight - newHeight)).coerceAtLeast(0f)
                                        rectWidth = newWidth
                                        rectHeight = newHeight
                                    }
                                }
                        ) {
                           Box(modifier = Modifier.align(Alignment.BottomStart).size(32.dp).border(BorderStroke(4.dp, Color.White), RoundedCornerShape(topEnd = 4.dp)))
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(48.dp)
                                .offset(x = (-16).dp, y = 16.dp)
                                .pointerInput(aspectMode) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (aspectMode != 0) return@detectDragGestures
                                        val newWidth = (rectWidth - dragAmount.x).coerceAtLeast(100f)
                                        val newHeight = (rectHeight + dragAmount.y).coerceAtLeast(100f).coerceAtMost(containerHeight - offsetY)
                                        offsetX = (offsetX + (rectWidth - newWidth)).coerceAtLeast(0f)
                                        rectWidth = newWidth
                                        rectHeight = newHeight
                                    }
                                }
                        ) {
                           Box(modifier = Modifier.align(Alignment.TopEnd).size(32.dp).border(BorderStroke(4.dp, Color.White), RoundedCornerShape(bottomStart = 4.dp)))
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(48.dp)
                                .offset(x = 16.dp, y = 16.dp)
                                .pointerInput(aspectMode) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (aspectMode != 0) return@detectDragGestures
                                        val newWidth = (rectWidth + dragAmount.x).coerceAtLeast(100f).coerceAtMost(containerWidth - offsetX)
                                        val newHeight = (rectHeight + dragAmount.y).coerceAtLeast(100f).coerceAtMost(containerHeight - offsetY)
                                        rectWidth = newWidth
                                        rectHeight = newHeight
                                    }
                                }
                        ) {
                           Box(modifier = Modifier.align(Alignment.TopStart).size(32.dp).border(BorderStroke(4.dp, Color.White), RoundedCornerShape(bottomEnd = 4.dp)))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun AccordionSection(
    title: String,
    expanded: Boolean,
    isDarkMode: Boolean = true,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val bg = if (isDarkMode) Color(0xFF181C26) else Color(0xFFF8FAFC)
    val border = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFE2E8F0)
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val dividerColor = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFE2E8F0)
    val iconTint = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppShape.card))
            .clickable { onToggle() },
        color = bg,
        border = BorderStroke(AppStroke.thin, border)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = titleColor, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle",
                    tint = iconTint
                )
            }
            if (expanded) {
                HorizontalDivider(color = dividerColor)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundEditorModal(
    visible: Boolean,
    currentConfig: GlobalBackgroundConfig,
    isDarkMode: Boolean = true,
    onDismiss: () -> Unit,
    onLiveChange: (GlobalBackgroundConfig) -> Unit = {},
    onSave: (GlobalBackgroundConfig) -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val sheetBg = if (isDarkMode) Color(0xFF0F1117) else Color(0xFFFFFFFF)
    val sheetBorder = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val btnBg = if (isDarkMode) Color(0xFF181C26) else Color(0xFFE2E8F0)

    var selectedBg by remember(currentConfig) { mutableStateOf(currentConfig.backgroundColorHex) }
    var selectedTextColor by remember(currentConfig) { mutableStateOf(currentConfig.textColorHex) }
    var appNameColorHex by remember(currentConfig) { mutableStateOf(currentConfig.appNameColorHex) }
    var titleColorHex by remember(currentConfig) { mutableStateOf(currentConfig.titleColorHex) }
    var budgetEnvelopesColorHex by remember(currentConfig) { mutableStateOf(currentConfig.budgetEnvelopesColorHex) }
    var selectedEffect by remember(currentConfig) { mutableStateOf(currentConfig.particleEffect) }
    var bgUri by remember(currentConfig) { mutableStateOf(currentConfig.backgroundImageUri ?: "") }
    var dimOpacity by remember(currentConfig) { mutableFloatStateOf(currentConfig.dimOpacity.toFloat()) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Color", "Wallpaper", "Effect")

    val draft = currentConfig.copy(
        backgroundColorHex = selectedBg,
        textColorHex = selectedTextColor,
        appNameColorHex = appNameColorHex,
        titleColorHex = titleColorHex,
        budgetEnvelopesColorHex = budgetEnvelopesColorHex,
        particleEffect = selectedEffect,
        backgroundImageUri = bgUri.ifBlank { null },
        dimOpacity = dimOpacity.toInt()
    )
    LaunchedEffect(selectedBg, selectedTextColor, appNameColorHex, titleColorHex, budgetEnvelopesColorHex, selectedEffect, bgUri, dimOpacity) {
        onLiveChange(draft)
    }

    // Image Crop modal state
    var croppingImageUri by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            croppingImageUri = uri.toString()
        }
    }

    // Image Crop Modal
    ImageCropModal(
        imageUri = croppingImageUri,
        visible = croppingImageUri != null,
        onDismiss = { croppingImageUri = null },
        onCropConfirm = { croppedUri ->
            bgUri = persistBackgroundImage(context, croppedUri) ?: croppedUri
            croppingImageUri = null
        }
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.5f
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        scrimColor = Color.Transparent,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
        ) {
            // Top Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard Background",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(btnBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = textColor,
                divider = { HorizontalDivider(color = sheetBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SCROLLABLE SECTIONS
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Canvas Color", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        RichColorPicker(
                            title = "Background",
                            selectedColorHex = selectedBg,
                            onColorSelected = { selectedBg = it },
                            isDarkMode = isDarkMode
                        )
                    }

                    HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Text Colors", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        RichColorPicker(
                            title = "Dashboard Default Text",
                            selectedColorHex = selectedTextColor,
                            onColorSelected = { selectedTextColor = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "\"BUCK MANAGER\" App Name",
                            selectedColorHex = appNameColorHex,
                            onColorSelected = { appNameColorHex = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "\"Dashboard\" Title",
                            selectedColorHex = titleColorHex,
                            onColorSelected = { titleColorHex = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "\"Budget Envelopes\" Header",
                            selectedColorHex = budgetEnvelopesColorHex,
                            onColorSelected = { budgetEnvelopesColorHex = it },
                            isDarkMode = isDarkMode
                        )
                    }
                } else if (selectedTab == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Wallpaper & Dimming", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(AppShape.button),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pick Photo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            if (bgUri.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { croppingImageUri = bgUri },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                                    border = BorderStroke(1.dp, sheetBorder),
                                    shape = RoundedCornerShape(AppShape.button),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Crop", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { bgUri = "" },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(AppShape.chip))
                                        .background(Color(0xFFFB7185).copy(alpha = 0.1f)),
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFB7185))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear Image")
                                }
                            }
                        }

                        OutlinedTextField(
                            value = bgUri,
                            onValueChange = { bgUri = it },
                            label = { Text("Image URL or Path", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.button),
                            colors = appTextFieldColors(isDarkMode)
                        )

                        Text("Curated Background Wallpapers", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(PresetBackgroundImages) { url ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(AppShape.chip))
                                        .border(
                                            2.dp,
                                            if (bgUri == url) GoldAccent else Color.Transparent,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { bgUri = url }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Darken / Dim Level", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${dimOpacity.toInt()}%", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Slider(
                                value = dimOpacity,
                                onValueChange = { dimOpacity = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )
                        }
                    }
                } else if (selectedTab == 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Particle Animation", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("none", "lines", "starfall").forEach { effect ->
                                val selected = selectedEffect == effect
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (selected) GoldAccent.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(1.dp, if (selected) GoldAccent else sheetBorder, RoundedCornerShape(50))
                                        .clickable { selectedEffect = effect }
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        effect.replaceFirstChar { it.uppercase() },
                                        color = if (selected) GoldAccent else textColor,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM ACTION BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedBg = if (isDarkMode) "#0F1117" else "#F6FAFD"
                        selectedTextColor = if (isDarkMode) "#FFFFFF" else "#0F172A"
                        appNameColorHex = if (isDarkMode) "#FFFFFF" else "#000000"
                        titleColorHex = if (isDarkMode) "#FFFFFF" else "#000000"
                        budgetEnvelopesColorHex = if (isDarkMode) "#FFFFFF" else "#000000"
                        selectedEffect = "none"
                        bgUri = ""
                        dimOpacity = 30f
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(AppShape.button),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)),
                    border = BorderStroke(1.dp, sheetBorder)
                ) {
                    Text("RESET TO DEFAULT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSave(draft)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(AppShape.button),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("APPLY CANVAS THEME", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderCardEditorModal(
    visible: Boolean,
    cardKey: String?,
    currentCards: HeaderCardsConfig,
    isDarkMode: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (String, HeaderCardConfig) -> Unit
) {
    if (!visible || cardKey == null) return

    val context = LocalContext.current
    val sheetBg = if (isDarkMode) Color(0xFF0F1117) else Color(0xFFFFFFFF)
    val sheetBorder = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val btnBg = if (isDarkMode) Color(0xFF181C26) else Color(0xFFE2E8F0)

    val currentConfig = when (cardKey) {
        "netWorth" -> currentCards.netWorth
        "income" -> currentCards.income
        "expense" -> currentCards.expense
        else -> currentCards.netWorth
    }

    var selectedBg by remember(cardKey) { mutableStateOf(currentConfig.backgroundColorHex) }
    var radiusTopLeft by remember(cardKey) { mutableIntStateOf(currentConfig.radiusTopLeft) }
    var radiusTopRight by remember(cardKey) { mutableIntStateOf(currentConfig.radiusTopRight) }
    var radiusBottomRight by remember(cardKey) { mutableIntStateOf(currentConfig.radiusBottomRight) }
    var radiusBottomLeft by remember(cardKey) { mutableIntStateOf(currentConfig.radiusBottomLeft) }
    
    var borderWidth by remember(cardKey) { mutableIntStateOf(currentConfig.borderTop) }
    
    var paddingTop by remember(cardKey) { mutableIntStateOf(currentConfig.paddingTop) }
    var paddingRight by remember(cardKey) { mutableIntStateOf(currentConfig.paddingRight) }
    var paddingBottom by remember(cardKey) { mutableIntStateOf(currentConfig.paddingBottom) }
    var paddingLeft by remember(cardKey) { mutableIntStateOf(currentConfig.paddingLeft) }
    
    var useGradient by remember(cardKey) { mutableStateOf(currentConfig.useGradient) }
    var gradientAngle by remember(cardKey) { mutableFloatStateOf(currentConfig.gradientAngle) }
    var gradColor1 by remember(cardKey) { mutableStateOf(currentConfig.gradientColors.getOrNull(0) ?: currentConfig.backgroundColorHex) }
    var gradColor2 by remember(cardKey) { mutableStateOf(currentConfig.gradientColors.getOrNull(1) ?: currentConfig.backgroundColorHex) }
    
    var selectedElevation by remember(cardKey) { mutableIntStateOf(currentConfig.elevation) }
    var labelColor by remember(cardKey) { mutableStateOf(currentConfig.labelColorHex) }
    var valueColor by remember(cardKey) { mutableStateOf(currentConfig.valueColorHex) }
    var borderColorHex by remember(cardKey) { mutableStateOf(currentConfig.borderColorHex) }
    var bgUri by remember(cardKey) { mutableStateOf(currentConfig.backgroundImageUri ?: "") }
    var dimOpacity by remember(cardKey) { mutableFloatStateOf(currentConfig.dimOpacity.toFloat()) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Look", "Photo", "Shape")
    val cardTitle = when (cardKey) {
        "netWorth" -> "Net Worth"
        "income" -> "Income"
        "expense" -> "Expense"
        else -> "Card"
    }

    // Image Crop modal state
    var croppingImageUri by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            croppingImageUri = uri.toString()
        }
    }

    // Image Crop Modal
    ImageCropModal(
        imageUri = croppingImageUri,
        visible = croppingImageUri != null,
        onDismiss = { croppingImageUri = null },
        onCropConfirm = { croppedUri ->
            bgUri = persistBackgroundImage(context, croppedUri) ?: croppedUri
            croppingImageUri = null
        }
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize $cardTitle",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(btnBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // STICKY CARD LIVE PREVIEW
            HeaderCardLook(
                cardKey = cardKey,
                config = liveHeaderConfig(
                    backgroundColorHex = selectedBg,
                    backgroundImageUri = bgUri.ifBlank { null },
                    dimOpacity = dimOpacity.toInt(),
                    radiusTopLeft = radiusTopLeft,
                    radiusTopRight = radiusTopRight,
                    radiusBottomRight = radiusBottomRight,
                    radiusBottomLeft = radiusBottomLeft,
                    borderWidth = borderWidth,
                    borderColorHex = borderColorHex,
                    paddingTop = paddingTop,
                    paddingRight = paddingRight,
                    paddingBottom = paddingBottom,
                    paddingLeft = paddingLeft,
                    useGradient = useGradient,
                    gradientColors = if (useGradient) listOf(gradColor1, gradColor2) else emptyList(),
                    gradientAngle = gradientAngle,
                    labelColorHex = labelColor,
                    valueColorHex = valueColor
                ),
                valueText = when (cardKey) {
                    "income" -> "+Rp 8.400.000"
                    "expense" -> "-Rp 2.150.000"
                    else -> "Rp 12.500.000"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = textColor,
                divider = { HorizontalDivider(color = sheetBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SCROLLABLE SECTIONS
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) { // Look
                    QuickLooksRow(
                        selectedBg = selectedBg,
                        isDarkMode = isDarkMode
                    ) { bg, label, value, radius, padding, gradient, g1, g2, border, borderColor ->
                        selectedBg = bg
                        labelColor = label
                        valueColor = value
                        radiusTopLeft = radius
                        radiusTopRight = radius
                        radiusBottomRight = radius
                        radiusBottomLeft = radius
                        paddingTop = padding
                        paddingRight = padding
                        paddingBottom = padding
                        paddingLeft = padding
                        useGradient = gradient
                        gradColor1 = g1
                        gradColor2 = g2
                        borderWidth = border
                        borderColorHex = borderColor
                    }
                    HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gradient fill", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = useGradient,
                            onCheckedChange = { useGradient = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = GoldAccent.copy(alpha = 0.5f))
                        )
                    }
                    if (useGradient) {
                        RichColorPicker(title = "From", selectedColorHex = gradColor1, onColorSelected = { gradColor1 = it }, isDarkMode = isDarkMode)
                        RichColorPicker(title = "To", selectedColorHex = gradColor2, onColorSelected = { gradColor2 = it }, isDarkMode = isDarkMode)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Angle", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${gradientAngle.toInt()}°", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Slider(value = gradientAngle, onValueChange = { gradientAngle = it }, valueRange = 0f..360f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                        }
                    } else {
                        RichColorPicker(title = "Card color", selectedColorHex = selectedBg, onColorSelected = { selectedBg = it }, isDarkMode = isDarkMode)
                    }
                    HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                    RichColorPicker(title = "Label", selectedColorHex = labelColor, onColorSelected = { labelColor = it }, isDarkMode = isDarkMode)
                    RichColorPicker(title = "Amount", selectedColorHex = valueColor, onColorSelected = { valueColor = it }, isDarkMode = isDarkMode)
                } else if (selectedTab == 1) { // Photo
                    Text("Background Image & Crop", color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick Photo", fontSize = 11.sp)
                        }

                        if (bgUri.isNotBlank()) {
                            OutlinedButton(
                                onClick = { croppingImageUri = bgUri },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDarkMode) Color.White else Color(0xFF0F172A))
                            ) {
                                Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Crop", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = { bgUri = "" },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFB7185))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear Image")
                            }
                        }
                    }

                    var showImageLink by remember { mutableStateOf(false) }
                    Text(
                        text = if (showImageLink) "Hide image link" else "Paste image link",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showImageLink = !showImageLink }
                    )
                    if (showImageLink) {
                        OutlinedTextField(
                            value = bgUri,
                            onValueChange = { bgUri = it },
                            label = { Text("Image link", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = appTextFieldColors(isDarkMode)
                        )
                    }

                    Text("Sample photos", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PresetBackgroundImages) { url ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        2.dp,
                                        if (bgUri == url) GoldAccent else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { bgUri = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Darken photo", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 12.sp)
                            Text("${dimOpacity.toInt()}%", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Slider(
                            value = dimOpacity,
                            onValueChange = { dimOpacity = it },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                        )
                    }
                } else if (selectedTab == 2) { // Shape
                    LinkedInsetControl(
                        title = "Roundness",
                        topStart = radiusTopLeft,
                        topEnd = radiusTopRight,
                        bottomEnd = radiusBottomRight,
                        bottomStart = radiusBottomLeft,
                        onChange = { tl, tr, br, bl ->
                            radiusTopLeft = tl
                            radiusTopRight = tr
                            radiusBottomRight = br
                            radiusBottomLeft = bl
                        },
                        valueRange = 0f..40f,
                        eachLabel = "Each corner",
                        isDarkMode = isDarkMode
                    )
                    HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                    CompactSlider(label = "Border", value = borderWidth.toFloat(), onValueChange = { borderWidth = it.toInt() }, valueRange = 0f..10f, isDarkMode = isDarkMode)
                    RichColorPicker(title = "Border color", selectedColorHex = borderColorHex, onColorSelected = { borderColorHex = it }, isDarkMode = isDarkMode)
                    HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                    LinkedInsetControl(
                        title = "Padding",
                        topStart = paddingTop,
                        topEnd = paddingRight,
                        bottomEnd = paddingBottom,
                        bottomStart = paddingLeft,
                        onChange = { t, r, b, l ->
                            paddingTop = t
                            paddingRight = r
                            paddingBottom = b
                            paddingLeft = l
                        },
                        valueRange = 0f..40f,
                        eachLabel = "Each side",
                        isDarkMode = isDarkMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTTOM ACTION BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedBg = currentConfig.backgroundColorHex
                        radiusTopLeft = currentConfig.radiusTopLeft
                        radiusTopRight = currentConfig.radiusTopRight
                        radiusBottomRight = currentConfig.radiusBottomRight
                        radiusBottomLeft = currentConfig.radiusBottomLeft
                        borderWidth = currentConfig.borderTop
                        paddingTop = currentConfig.paddingTop
                        paddingRight = currentConfig.paddingRight
                        paddingBottom = currentConfig.paddingBottom
                        paddingLeft = currentConfig.paddingLeft
                        useGradient = currentConfig.useGradient
                        gradientAngle = currentConfig.gradientAngle
                        gradColor1 = currentConfig.gradientColors.getOrNull(0) ?: currentConfig.backgroundColorHex
                        gradColor2 = currentConfig.gradientColors.getOrNull(1) ?: currentConfig.backgroundColorHex
                        labelColor = currentConfig.labelColorHex
                        valueColor = currentConfig.valueColorHex
                        bgUri = currentConfig.backgroundImageUri ?: ""
                        dimOpacity = currentConfig.dimOpacity.toFloat()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9CA3AF)),
                    border = BorderStroke(1.dp, Color(0xFF2A273C))
                ) {
                    Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSave(
                            cardKey,
                            currentConfig.copy(
                                backgroundColorHex = selectedBg,
                                radiusTopLeft = radiusTopLeft,
                                radiusTopRight = radiusTopRight,
                                radiusBottomRight = radiusBottomRight,
                                radiusBottomLeft = radiusBottomLeft,
                                borderTop = borderWidth,
                                borderRight = borderWidth,
                                borderBottom = borderWidth,
                                borderLeft = borderWidth,
                                borderColorHex = borderColorHex,
                                elevation = selectedElevation,
                                paddingTop = paddingTop,
                                paddingBottom = paddingBottom,
                                paddingLeft = paddingLeft,
                                paddingRight = paddingRight,
                                useGradient = useGradient,
                                gradientColors = if (useGradient) listOf(gradColor1, gradColor2) else emptyList(),
                                gradientAngle = gradientAngle,
                                labelColorHex = labelColor,
                                valueColorHex = valueColor,
                                backgroundImageUri = bgUri.ifBlank { null },
                                dimOpacity = dimOpacity.toInt()
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save look", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvelopeEditorModal(
    envelope: Envelope?,
    visible: Boolean,
    isDarkMode: Boolean = true,
    hasPremium: Boolean = false,
    totalAllocatedPercentage: Int = 100,
    onDismiss: () -> Unit,
    onSave: (Envelope) -> Unit,
    onDelete: (String) -> Unit
) {
    if (!visible || envelope == null) return

    val context = LocalContext.current
    val sheetBg = if (isDarkMode) Color(0xFF0F1117) else Color(0xFFFFFFFF)
    val sheetBorder = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)

    var name by remember(envelope) { mutableStateOf(envelope.name) }
    var percentage by remember(envelope) { mutableFloatStateOf(envelope.percentage.toFloat()) }
    var colorHex by remember(envelope) { mutableStateOf(envelope.colorHex) }
    var iconName by remember(envelope) { mutableStateOf(envelope.iconName) }
    var bgHex by remember(envelope) { mutableStateOf(envelope.backgroundColorHex) }
    var valueColorHex by remember(envelope) { mutableStateOf(envelope.valueColorHex) }
    var selectedElevation by remember(envelope) { mutableIntStateOf(envelope.elevation) }

    var radiusTopLeft by remember(envelope) { mutableIntStateOf(envelope.radiusTopLeft) }
    var radiusTopRight by remember(envelope) { mutableIntStateOf(envelope.radiusTopRight) }
    var radiusBottomRight by remember(envelope) { mutableIntStateOf(envelope.radiusBottomRight) }
    var radiusBottomLeft by remember(envelope) { mutableIntStateOf(envelope.radiusBottomLeft) }
    var borderWidth by remember(envelope) { mutableIntStateOf(envelope.borderTop) }
    var paddingTop by remember(envelope) { mutableIntStateOf(envelope.paddingTop) }
    var paddingRight by remember(envelope) { mutableIntStateOf(envelope.paddingRight) }
    var paddingBottom by remember(envelope) { mutableIntStateOf(envelope.paddingBottom) }
    var paddingLeft by remember(envelope) { mutableIntStateOf(envelope.paddingLeft) }
    var useGradient by remember(envelope) { mutableStateOf(envelope.useGradient) }
    var gradientAngle by remember(envelope) { mutableFloatStateOf(envelope.gradientAngle) }
    var gradColor1 by remember(envelope) { mutableStateOf(envelope.gradientColors.getOrNull(0) ?: envelope.backgroundColorHex) }
    var gradColor2 by remember(envelope) { mutableStateOf(envelope.gradientColors.getOrNull(1) ?: envelope.backgroundColorHex) }
    var borderColorHex by remember(envelope) { mutableStateOf(envelope.borderColorHex) }
    var bgUri by remember(envelope) { mutableStateOf(envelope.backgroundImageUri ?: "") }
    var dimOpacity by remember(envelope) { mutableFloatStateOf(envelope.dimOpacity.toFloat()) }

    var croppingImageUri by remember { mutableStateOf<String?>(null) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            croppingImageUri = uri.toString()
        }
    }

    ImageCropModal(
        imageUri = croppingImageUri,
        visible = croppingImageUri != null,
        onDismiss = { croppingImageUri = null },
        onCropConfirm = { croppedUri ->
            bgUri = persistBackgroundImage(context, croppedUri) ?: croppedUri
            croppingImageUri = null
        }
    )

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Envelope", "Look", "Shape")
    var confirmDelete by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Envelope",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor)
                }
            }

            // Sticky preview
            EnvelopeLook(
                envelope = envelope.copy(
                    name = name,
                    percentage = percentage.toInt(),
                    colorHex = colorHex,
                    labelColorHex = colorHex,
                    iconName = iconName,
                    backgroundColorHex = bgHex,
                    valueColorHex = valueColorHex,
                    borderColorHex = borderColorHex,
                    radiusTopLeft = radiusTopLeft,
                    radiusTopRight = radiusTopRight,
                    radiusBottomRight = radiusBottomRight,
                    radiusBottomLeft = radiusBottomLeft,
                    borderTop = borderWidth,
                    borderRight = borderWidth,
                    borderBottom = borderWidth,
                    borderLeft = borderWidth,
                    paddingTop = paddingTop,
                    paddingRight = paddingRight,
                    paddingBottom = paddingBottom,
                    paddingLeft = paddingLeft,
                    useGradient = useGradient,
                    gradientColors = if (useGradient) listOf(gradColor1, gradColor2) else emptyList(),
                    gradientAngle = gradientAngle,
                    backgroundImageUri = bgUri.ifBlank { null },
                    dimOpacity = dimOpacity.toInt()
                ),
                remainingText = "Rp 1.250.000",
                caption = if (envelope.id == "savings") "Secured funds. Do not touch!" else "Left of Rp 2.500.000",
                isDarkMode = isDarkMode,
                slider = if (envelope.id != "main") {
                    {
                        EnvelopeAllocationSlider(
                            colorHex = colorHex,
                            value = percentage,
                            onChange = {},
                            onFinished = {},
                            paddingH = paddingLeft
                        )
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = textColor,
                divider = { HorizontalDivider(color = sheetBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Envelope Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.button),
                        colors = appTextFieldColors(isDarkMode)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select Icon", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(IconPresets) { icon ->
                                val isSelected = iconName == icon
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) GoldAccent.copy(alpha = 0.2f) else if (isDarkMode) Color(0xFF181C26) else Color(0xFFF1F5F9))
                                        .border(2.dp, if (isSelected) GoldAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { iconName = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconVector(icon),
                                        contentDescription = icon,
                                        tint = if (isSelected) GoldAccent else if (isDarkMode) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (envelope.id != "main") {
                        Text("Allocation: ${percentage.toInt()}%", color = textColor)
                        Slider(
                            value = percentage,
                            onValueChange = { percentage = it },
                            valueRange = 0f..100f,
                            steps = 99
                        )
                        val othersTotal = totalAllocatedPercentage - envelope.percentage
                        val remaining = 100 - othersTotal - percentage.toInt()
                        val indicatorColor = if (remaining >= 0) Color(0xFF34D399) else Color(0xFFFB7185)
                        Text(
                            text = if (remaining >= 0) "${othersTotal + percentage.toInt()}% used — ${remaining}% available" else "Over-allocated by ${-remaining}%",
                            color = indicatorColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Allocation is managed automatically for the Main (Buffer) envelope.", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                    }
                } else if (selectedTab == 1 || selectedTab == 2) {
                    if (!hasPremium) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            shape = RoundedCornerShape(AppShape.button),
                            color = if (isDarkMode) Color(0xFF181C26) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Unlock Premium", color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Customize colors and layout with Premium.", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                            }
                        }
                    } else if (selectedTab == 1) {
                        QuickLooksRow(
                            selectedBg = bgHex,
                            isDarkMode = isDarkMode
                        ) { bg, label, value, radius, padding, gradient, g1, g2, border, borderColor ->
                            bgHex = bg
                            colorHex = label
                            valueColorHex = value
                            radiusTopLeft = radius
                            radiusTopRight = radius
                            radiusBottomRight = radius
                            radiusBottomLeft = radius
                            paddingTop = padding
                            paddingRight = padding
                            paddingBottom = padding
                            paddingLeft = padding
                            useGradient = gradient
                            gradColor1 = g1
                            gradColor2 = g2
                            borderWidth = border
                            borderColorHex = borderColor
                        }
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gradient fill", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Switch(
                                checked = useGradient,
                                onCheckedChange = { useGradient = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = GoldAccent.copy(alpha = 0.5f))
                            )
                        }
                        if (useGradient) {
                            RichColorPicker(title = "Gradient Color 1", selectedColorHex = gradColor1, onColorSelected = { gradColor1 = it }, isDarkMode = isDarkMode)
                            RichColorPicker(title = "Gradient Color 2", selectedColorHex = gradColor2, onColorSelected = { gradColor2 = it }, isDarkMode = isDarkMode)
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Gradient Angle", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${gradientAngle.toInt()}°", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Slider(value = gradientAngle, onValueChange = { gradientAngle = it }, valueRange = 0f..360f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                        } else {
                            RichColorPicker(
                                title = "Background Color",
                                selectedColorHex = bgHex,
                                onColorSelected = { bgHex = it },
                                isDarkMode = isDarkMode
                            )
                        }
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        RichColorPicker(
                            title = "Color",
                            selectedColorHex = colorHex,
                            onColorSelected = { colorHex = it },
                            isDarkMode = isDarkMode
                        )
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        RichColorPicker(
                            title = "Value Color",
                            selectedColorHex = valueColorHex,
                            onColorSelected = { valueColorHex = it },
                            isDarkMode = isDarkMode
                        )
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        RichColorPicker(
                            title = "Border Color",
                            selectedColorHex = borderColorHex,
                            onColorSelected = { borderColorHex = it },
                            isDarkMode = isDarkMode
                        )
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        
                        Text("Background Image & Crop", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pick Photo", fontSize = 11.sp)
                            }

                            if (bgUri.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { croppingImageUri = bgUri },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                                ) {
                                    Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Crop", fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { bgUri = "" },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFB7185))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear Image")
                                }
                            }
                        }

                        OutlinedTextField(
                            value = bgUri,
                            onValueChange = { bgUri = it },
                            label = { Text("Image URL or Path", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.button),
                            colors = appTextFieldColors(isDarkMode)
                        )

                        Text("Sample Wallpapers", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 11.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PresetBackgroundImages) { url ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            2.dp,
                                            if (bgUri == url) GoldAccent else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { bgUri = url }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Darken / Dim Level", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 11.sp)
                                Text("${dimOpacity.toInt()}%", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Slider(
                                value = dimOpacity,
                                onValueChange = { dimOpacity = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )
                        }
                    } else if (selectedTab == 2) {
                        LinkedInsetControl(
                            title = "Roundness",
                            topStart = radiusTopLeft,
                            topEnd = radiusTopRight,
                            bottomEnd = radiusBottomRight,
                            bottomStart = radiusBottomLeft,
                            onChange = { tl, tr, br, bl ->
                                radiusTopLeft = tl
                                radiusTopRight = tr
                                radiusBottomRight = br
                                radiusBottomLeft = bl
                            },
                            valueRange = 0f..40f,
                            eachLabel = "Each corner",
                            isDarkMode = isDarkMode
                        )
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        CompactSlider(label = "Border", value = borderWidth.toFloat(), onValueChange = { borderWidth = it.toInt() }, valueRange = 0f..10f, isDarkMode = isDarkMode)
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        LinkedInsetControl(
                            title = "Padding",
                            topStart = paddingTop,
                            topEnd = paddingRight,
                            bottomEnd = paddingBottom,
                            bottomStart = paddingLeft,
                            onChange = { t, r, b, l ->
                                paddingTop = t
                                paddingRight = r
                                paddingBottom = b
                                paddingLeft = l
                            },
                            valueRange = 0f..40f,
                            eachLabel = "Each side",
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }

            // Bottom Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (envelope.id != "main") {
                    OutlinedButton(
                        onClick = { confirmDelete = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(AppShape.button),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = {
                        onSave(envelope.copy(
                            name = name,
                            percentage = percentage.toInt(),
                            colorHex = colorHex,
                            labelColorHex = colorHex,
                            iconName = iconName,
                            backgroundColorHex = bgHex,
                            valueColorHex = valueColorHex,
                            borderColorHex = borderColorHex,
                            elevation = selectedElevation,
                            radiusTopLeft = radiusTopLeft,
                            radiusTopRight = radiusTopRight,
                            radiusBottomRight = radiusBottomRight,
                            radiusBottomLeft = radiusBottomLeft,
                            borderTop = borderWidth,
                            borderRight = borderWidth,
                            borderBottom = borderWidth,
                            borderLeft = borderWidth,
                            paddingTop = paddingTop,
                            paddingRight = paddingRight,
                            paddingBottom = paddingBottom,
                            paddingLeft = paddingLeft,
                            useGradient = useGradient,
                            gradientColors = if (useGradient) listOf(gradColor1, gradColor2) else emptyList(),
                            gradientAngle = gradientAngle,
                            backgroundImageUri = bgUri.ifBlank { null },
                            dimOpacity = dimOpacity.toInt()
                        ))
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(AppShape.button),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${envelope.name}?") },
            text = { Text("Spending in this envelope moves to Main. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(envelope.id)
                    confirmDelete = false
                    onDismiss()
                }) { Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEnvelopeModal(
    visible: Boolean,
    isDarkMode: Boolean = true,
    hasPremium: Boolean = false,
    totalAllocatedPercentage: Int = 100,
    onDismiss: () -> Unit,
    onAdd: (Envelope) -> Unit
) {
    if (!visible) return

    val sheetBg = if (isDarkMode) Color(0xFF0F1117) else Color(0xFFFFFFFF)
    val sheetBorder = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)

    var name by remember { mutableStateOf("") }
    var percentage by remember { mutableFloatStateOf(10f) }
    var colorHex by remember { mutableStateOf("#3B82F6") }
    var iconName by remember { mutableStateOf("folder") }
    var bgHex by remember { mutableStateOf(if (isDarkMode) "#181C26" else "#FFFFFF") }
    var valueColorHex by remember { mutableStateOf(if (isDarkMode) "#FFFFFF" else "#0F172A") }
    var selectedElevation by remember { mutableIntStateOf(0) }
    
    var radiusTopLeft by remember { mutableIntStateOf(24) }
    var radiusTopRight by remember { mutableIntStateOf(24) }
    var radiusBottomRight by remember { mutableIntStateOf(24) }
    var radiusBottomLeft by remember { mutableIntStateOf(24) }
    var borderTop by remember { mutableIntStateOf(1) }
    var borderRight by remember { mutableIntStateOf(1) }
    var borderBottom by remember { mutableIntStateOf(1) }
    var borderLeft by remember { mutableIntStateOf(1) }
    var paddingTop by remember { mutableIntStateOf(21) }
    var paddingRight by remember { mutableIntStateOf(21) }
    var paddingBottom by remember { mutableIntStateOf(21) }
    var paddingLeft by remember { mutableIntStateOf(21) }
    var useGradient by remember { mutableStateOf(false) }
    var gradientAngle by remember { mutableFloatStateOf(0f) }
    var gradColor1 by remember { mutableStateOf(bgHex) }
    var gradColor2 by remember { mutableStateOf(bgHex) }
    var borderColorHex by remember { mutableStateOf(if (isDarkMode) "#2A273C" else "#E2E8F0") }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Envelope", "Look", "Shape")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Envelope",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor)
                }
            }

            // Sticky preview
            EnvelopeLook(
                envelope = Envelope(
                    id = "preview",
                    name = name.ifBlank { "New Envelope" },
                    percentage = percentage.toInt(),
                    colorHex = colorHex,
                    labelColorHex = colorHex,
                    iconName = iconName,
                    backgroundColorHex = bgHex,
                    valueColorHex = valueColorHex,
                    borderColorHex = borderColorHex,
                    radiusTopLeft = radiusTopLeft,
                    radiusTopRight = radiusTopRight,
                    radiusBottomRight = radiusBottomRight,
                    radiusBottomLeft = radiusBottomLeft,
                    borderTop = borderTop,
                    borderRight = borderRight,
                    borderBottom = borderBottom,
                    borderLeft = borderLeft,
                    paddingTop = paddingTop,
                    paddingRight = paddingRight,
                    paddingBottom = paddingBottom,
                    paddingLeft = paddingLeft,
                    useGradient = useGradient,
                    gradientColors = if (useGradient) listOf(gradColor1, gradColor2) else emptyList(),
                    gradientAngle = gradientAngle
                ),
                remainingText = "Rp 0",
                caption = "No money assigned yet",
                isDarkMode = isDarkMode,
                slider = {
                    EnvelopeAllocationSlider(
                        colorHex = colorHex,
                        value = percentage,
                        onChange = {},
                        onFinished = {},
                        paddingH = paddingLeft
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = textColor,
                divider = { HorizontalDivider(color = sheetBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Envelope Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.button),
                        colors = appTextFieldColors(isDarkMode)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select Icon", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(IconPresets) { icon ->
                                val isSelected = iconName == icon
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) GoldAccent.copy(alpha = 0.2f) else if (isDarkMode) Color(0xFF181C26) else Color(0xFFF1F5F9))
                                        .border(2.dp, if (isSelected) GoldAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { iconName = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconVector(icon),
                                        contentDescription = icon,
                                        tint = if (isSelected) GoldAccent else if (isDarkMode) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text("Allocation: ${percentage.toInt()}%", color = textColor)
                    Slider(
                        value = percentage,
                        onValueChange = { percentage = it },
                        valueRange = 0f..100f,
                        steps = 99
                    )
                    val remaining = 100 - totalAllocatedPercentage - percentage.toInt()
                    val indicatorColor = if (remaining >= 0) Color(0xFF34D399) else Color(0xFFFB7185)
                    Text(
                        text = if (remaining >= 0) "${totalAllocatedPercentage + percentage.toInt()}% used — ${remaining}% available" else "Over-allocated by ${-remaining}%",
                        color = indicatorColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (selectedTab == 1 || selectedTab == 2) {
                    if (!hasPremium) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            shape = RoundedCornerShape(AppShape.button),
                            color = if (isDarkMode) Color(0xFF181C26) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Unlock Premium", color = if (isDarkMode) Color.White else Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Customize colors and layout with Premium.", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                            }
                        }
                    } else if (selectedTab == 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Use Gradient Background", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = useGradient,
                            onCheckedChange = { useGradient = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = GoldAccent.copy(alpha = 0.5f))
                        )
                    }
                    if (useGradient) {
                        RichColorPicker(title = "Gradient Color 1", selectedColorHex = gradColor1, onColorSelected = { gradColor1 = it }, isDarkMode = isDarkMode)
                        RichColorPicker(title = "Gradient Color 2", selectedColorHex = gradColor2, onColorSelected = { gradColor2 = it }, isDarkMode = isDarkMode)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gradient Angle", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${gradientAngle.toInt()}°", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Slider(value = gradientAngle, onValueChange = { gradientAngle = it }, valueRange = 0f..360f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                        }
                    } else {
                        RichColorPicker(title = "Background Color", selectedColorHex = bgHex, onColorSelected = { bgHex = it }, isDarkMode = isDarkMode)
                    }
                    HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                    RichColorPicker(
                        title = "Color",
                        selectedColorHex = colorHex,
                        onColorSelected = { colorHex = it },
                        isDarkMode = isDarkMode
                    )
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        RichColorPicker(
                            title = "Value Color",
                            selectedColorHex = valueColorHex,
                            onColorSelected = { valueColorHex = it },
                            isDarkMode = isDarkMode
                        )
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        RichColorPicker(
                            title = "Border Color",
                            selectedColorHex = borderColorHex,
                            onColorSelected = { borderColorHex = it },
                            isDarkMode = isDarkMode
                        )
                    } else if (selectedTab == 2) {
                    LinkedInsetControl(
                        title = "Roundness",
                        topStart = radiusTopLeft,
                        topEnd = radiusTopRight,
                        bottomEnd = radiusBottomRight,
                        bottomStart = radiusBottomLeft,
                        onChange = { tl, tr, br, bl ->
                            radiusTopLeft = tl
                            radiusTopRight = tr
                            radiusBottomRight = br
                            radiusBottomLeft = bl
                        },
                        valueRange = 0f..40f,
                        eachLabel = "Each corner",
                        isDarkMode = isDarkMode
                    )
                    HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                    CompactSlider(label = "Border", value = borderTop.toFloat(), onValueChange = {
                        val v = it.toInt()
                        borderTop = v
                        borderRight = v
                        borderBottom = v
                        borderLeft = v
                    }, valueRange = 0f..10f, isDarkMode = isDarkMode)
                    HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                    LinkedInsetControl(
                        title = "Padding",
                        topStart = paddingTop,
                        topEnd = paddingRight,
                        bottomEnd = paddingBottom,
                        bottomStart = paddingLeft,
                        onChange = { t, r, b, l ->
                            paddingTop = t
                            paddingRight = r
                            paddingBottom = b
                            paddingLeft = l
                        },
                        valueRange = 0f..40f,
                        eachLabel = "Each side",
                        isDarkMode = isDarkMode
                    )
                }
            }
            }

            // Bottom Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = {
                        onAdd(
                            Envelope(
                                id = "",
                                name = name,
                                percentage = percentage.toInt(),
                                colorHex = colorHex,
                                labelColorHex = colorHex,
                                iconName = iconName,
                                backgroundColorHex = bgHex,
                                valueColorHex = valueColorHex,
                                elevation = selectedElevation,
                                radiusTopLeft = radiusTopLeft,
                                radiusTopRight = radiusTopRight,
                                radiusBottomRight = radiusBottomRight,
                                radiusBottomLeft = radiusBottomLeft,
                                borderTop = borderTop,
                                borderRight = borderRight,
                                borderBottom = borderBottom,
                                borderLeft = borderLeft,
                                borderColorHex = borderColorHex,
                                paddingTop = paddingTop,
                                paddingRight = paddingRight,
                                paddingBottom = paddingBottom,
                                paddingLeft = paddingLeft,
                                useGradient = useGradient,
                                gradientColors = if (useGradient) listOf(gradColor1, gradColor2) else emptyList(),
                                gradientAngle = gradientAngle
                            )
                        )
                        onDismiss()
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(AppShape.button),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Add envelope", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


