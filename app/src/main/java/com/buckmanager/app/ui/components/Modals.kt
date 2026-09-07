package com.buckmanager.app.ui.components
import com.buckmanager.app.utils.customCardStyle
import com.buckmanager.app.utils.persistBackgroundImage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import coil.compose.AsyncImage
import com.buckmanager.app.model.*
import com.buckmanager.app.ui.AppChrome
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.AppStroke
import com.buckmanager.app.ui.GoldAccent
import com.buckmanager.app.ui.appTextFieldColors
import androidx.core.graphics.toColorInt
import androidx.compose.ui.platform.LocalContext
import com.buckmanager.app.widget.GoalAppWidgetProvider

fun parseHexColor(hex: String, defaultColor: Color = Color.DarkGray): Color {
    return try {
        Color(hex.toColorInt())
    } catch (e: Exception) {
        defaultColor
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundGoalEditorModal(
    visible: Boolean,
    currentConfig: FundGoalConfig,
    isDarkMode: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (FundGoalConfig) -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sheetBg = if (isDarkMode) Color(0xFF0F1117) else Color(0xFFFFFFFF)
    val sheetBorder = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val btnBg = if (isDarkMode) Color(0xFF181C26) else Color(0xFFE2E8F0)

    var name by remember(currentConfig) { mutableStateOf(currentConfig.name) }
    val context = LocalContext.current
    var target by remember(currentConfig) { mutableStateOf(currentConfig.targetAmount.toLong().toString()) }
    var current by remember(currentConfig) { mutableStateOf(currentConfig.currentAmount.toLong().toString()) }

    var selectedBg by remember(currentConfig) { mutableStateOf(currentConfig.backgroundColorHex) }
    var radiusTopLeft by remember(currentConfig) { mutableIntStateOf(currentConfig.radiusTopLeft) }
    var radiusTopRight by remember(currentConfig) { mutableIntStateOf(currentConfig.radiusTopRight) }
    var radiusBottomRight by remember(currentConfig) { mutableIntStateOf(currentConfig.radiusBottomRight) }
    var radiusBottomLeft by remember(currentConfig) { mutableIntStateOf(currentConfig.radiusBottomLeft) }
    var borderTop by remember(currentConfig) { mutableIntStateOf(currentConfig.borderTop) }
    var borderRight by remember(currentConfig) { mutableIntStateOf(currentConfig.borderRight) }
    var borderBottom by remember(currentConfig) { mutableIntStateOf(currentConfig.borderBottom) }
    var borderLeft by remember(currentConfig) { mutableIntStateOf(currentConfig.borderLeft) }
    var paddingTop by remember(currentConfig) { mutableIntStateOf(currentConfig.paddingTop) }
    var paddingRight by remember(currentConfig) { mutableIntStateOf(currentConfig.paddingRight) }
    var paddingBottom by remember(currentConfig) { mutableIntStateOf(currentConfig.paddingBottom) }
    var paddingLeft by remember(currentConfig) { mutableIntStateOf(currentConfig.paddingLeft) }
    var useGradient by remember(currentConfig) { mutableStateOf(currentConfig.useGradient) }
    var gradientAngle by remember(currentConfig) { mutableFloatStateOf(currentConfig.gradientAngle) }
    var gradColor1 by remember(currentConfig) { mutableStateOf(currentConfig.gradientColors.getOrNull(0) ?: currentConfig.backgroundColorHex) }
    var gradColor2 by remember(currentConfig) { mutableStateOf(currentConfig.gradientColors.getOrNull(1) ?: currentConfig.backgroundColorHex) }
    
    var selectedElevation by remember(currentConfig) { mutableIntStateOf(currentConfig.elevation) }
    var labelColor by remember(currentConfig) { mutableStateOf(currentConfig.labelColorHex) }
    var valueColor by remember(currentConfig) { mutableStateOf(currentConfig.valueColorHex) }
    var borderColorHex by remember(currentConfig) { mutableStateOf(currentConfig.borderColorHex) }
    var btnBgColorHex by remember(currentConfig) { mutableStateOf(currentConfig.btnBgColorHex) }
    var btnTextColorHex by remember(currentConfig) { mutableStateOf(currentConfig.btnTextColorHex) }
    var bgUri by remember(currentConfig) { mutableStateOf(currentConfig.backgroundImageUri ?: "") }
    var dimOpacity by remember(currentConfig) { mutableFloatStateOf(currentConfig.dimOpacity.toFloat()) }

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

    val targetNum = target.toDoubleOrNull() ?: 1.0
    val currentNum = current.toDoubleOrNull() ?: 0.0
    val progressRatio = (currentNum / targetNum.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize Fund Goal",
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

            // STICKY CARD LIVE PREVIEW
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .customCardStyle(
                            shape = RoundedCornerShape(radiusTopLeft.dp, radiusTopRight.dp, radiusBottomRight.dp, radiusBottomLeft.dp),
                            backgroundColor = parseHexColor(selectedBg, Color(0xFF181C26)),
                            useGradient = useGradient,
                            gradientColors = listOf(parseHexColor(gradColor1), parseHexColor(gradColor2)),
                            gradientAngle = gradientAngle,
                            borderTop = borderTop.dp,
                            borderRight = borderRight.dp,
                            borderBottom = borderBottom.dp,
                            borderLeft = borderLeft.dp,
                            borderColor = parseHexColor(borderColorHex, Color(0xFF3B82F6))
                        )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (bgUri.isNotBlank()) {
                            AsyncImage(
                                model = bgUri,
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

                        Column(modifier = Modifier.padding(start = paddingLeft.dp, top = paddingTop.dp, end = paddingRight.dp, bottom = paddingBottom.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎯", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name.ifBlank { "My Goal" },
                                        color = parseHexColor(valueColor, Color.White),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Text(
                                    text = "${(progressRatio * 100).toInt()}%",
                                    color = parseHexColor(labelColor, GoldAccent),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Saved: ${com.buckmanager.app.model.formatRp(currentNum)} / ${com.buckmanager.app.model.formatRp(targetNum)}",
                                color = parseHexColor(labelColor, Color(0xFF9CA3AF)),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = parseHexColor(labelColor, GoldAccent),
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }

            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("Goal Details", "Colors", "Layout")

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = { Divider(color = sheetBorder) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if (selectedTab == index) GoldAccent else textColor) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Goal Name", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.button),
                            colors = appTextFieldColors(isDarkMode)
                        )
                        OutlinedTextField(
                            value = target,
                            onValueChange = { target = it.filter { c -> c.isDigit() } },
                            label = { Text("Target Amount (Rp)", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.button),
                            colors = appTextFieldColors(isDarkMode)
                        )
                        OutlinedTextField(
                            value = current,
                            onValueChange = { current = it.filter { c -> c.isDigit() } },
                            label = { Text("Current Saved Amount (Rp)", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.button),
                            colors = appTextFieldColors(isDarkMode)
                        )
                    }
                    1 -> {
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
                            RichColorPicker(title = "Background Color", selectedColorHex = selectedBg, onColorSelected = { selectedBg = it }, isDarkMode = isDarkMode)
                        }
                        RichColorPicker(
                            title = "Accent / Progress Bar Color",
                            selectedColorHex = labelColor,
                            onColorSelected = { labelColor = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "Title & Value Text Color",
                            selectedColorHex = valueColor,
                            onColorSelected = { valueColor = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "Border Color",
                            selectedColorHex = borderColorHex,
                            onColorSelected = { borderColorHex = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "Button Background Color",
                            selectedColorHex = btnBgColorHex,
                            onColorSelected = { btnBgColorHex = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "Button Text Color",
                            selectedColorHex = btnTextColorHex,
                            onColorSelected = { btnTextColorHex = it },
                            isDarkMode = isDarkMode
                        )
                    }
                    2 -> {
                        Text("Corner Radius", color = textColor, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("TL: ${radiusTopLeft}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(value = radiusTopLeft.toFloat(), onValueChange = { radiusTopLeft = it.toInt() }, valueRange = 0f..40f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("TR: ${radiusTopRight}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(value = radiusTopRight.toFloat(), onValueChange = { radiusTopRight = it.toInt() }, valueRange = 0f..40f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BL: ${radiusBottomLeft}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(value = radiusBottomLeft.toFloat(), onValueChange = { radiusBottomLeft = it.toInt() }, valueRange = 0f..40f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BR: ${radiusBottomRight}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(value = radiusBottomRight.toFloat(), onValueChange = { radiusBottomRight = it.toInt() }, valueRange = 0f..40f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                        }

                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        Text("Border Width", color = textColor, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("All Sides: ${borderTop}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(
                                    value = borderTop.toFloat(), 
                                    onValueChange = { 
                                        val newVal = it.toInt()
                                        borderTop = newVal
                                        borderRight = newVal
                                        borderBottom = newVal
                                        borderLeft = newVal
                                    }, 
                                    valueRange = 0f..10f, 
                                    colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                                )
                            }
                        }

                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        Text("Inner Padding", color = textColor, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Top: ${paddingTop}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(value = paddingTop.toFloat(), onValueChange = { paddingTop = it.toInt() }, valueRange = 0f..40f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Right: ${paddingRight}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(value = paddingRight.toFloat(), onValueChange = { paddingRight = it.toInt() }, valueRange = 0f..40f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bottom: ${paddingBottom}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(value = paddingBottom.toFloat(), onValueChange = { paddingBottom = it.toInt() }, valueRange = 0f..40f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Left: ${paddingLeft}dp", fontSize = 10.sp, color = GoldAccent)
                                Slider(value = paddingLeft.toFloat(), onValueChange = { paddingLeft = it.toInt() }, valueRange = 0f..40f, colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent))
                            }
                        }

                        Divider(color = sheetBorder)

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
                                        .clip(RoundedCornerShape(AppShape.tick))
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
                    }
                }
            }

            // BOTTOM ACTION BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        name = currentConfig.name
                        target = currentConfig.targetAmount.toLong().toString()
                        current = currentConfig.currentAmount.toLong().toString()
                        selectedBg = currentConfig.backgroundColorHex
                        radiusTopLeft = currentConfig.radiusTopLeft
                        radiusTopRight = currentConfig.radiusTopRight
                        radiusBottomRight = currentConfig.radiusBottomRight
                        radiusBottomLeft = currentConfig.radiusBottomLeft
                        borderTop = currentConfig.borderTop
                        borderRight = currentConfig.borderRight
                        borderBottom = currentConfig.borderBottom
                        borderLeft = currentConfig.borderLeft
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
                        borderColorHex = currentConfig.borderColorHex
                        btnBgColorHex = currentConfig.btnBgColorHex
                        btnTextColorHex = currentConfig.btnTextColorHex
                        bgUri = currentConfig.backgroundImageUri ?: ""
                        dimOpacity = currentConfig.dimOpacity.toFloat()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)),
                    border = BorderStroke(1.dp, sheetBorder)
                ) {
                    Text("RESET TO DEFAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSave(
                            FundGoalConfig(
                                name = name.ifBlank { "Set a Goal" },
                                targetAmount = target.toDoubleOrNull() ?: 0.0,
                                currentAmount = current.toDoubleOrNull() ?: 0.0,
                                backgroundColorHex = selectedBg,
                                backgroundImageUri = bgUri.ifBlank { null },
                                dimOpacity = dimOpacity.toInt(),
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
                                elevation = selectedElevation,
                                useGradient = useGradient,
                                gradientColors = if (useGradient) listOf(gradColor1, gradColor2) else emptyList(),
                                gradientAngle = gradientAngle,
                                labelColorHex = labelColor,
                                valueColorHex = valueColor,
                                borderColorHex = borderColorHex,
                                btnBgColorHex = btnBgColorHex,
                                btnTextColorHex = btnTextColorHex
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("SAVE GOAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}





@Composable
fun SettingsModal(
    visible: Boolean,
    notificationEnabled: Boolean,
    userEmail: String?,
    userProfilePicUrl: String?,
    lastBackupDate: String?,
    monetization: MonetizationState,
    isDarkMode: Boolean = true,
    isThemeCustomized: Boolean = false,
    isCustomizationLocked: Boolean = true,
    onDismiss: () -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    onToggleTheme: (Boolean) -> Unit = {},
    onResetCustomization: () -> Unit = {},
    onUnlockCustomization: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onOpenCustomizeWidget: () -> Unit = {},
    onCurrencyChanged: () -> Unit = {},
    onExportJson: (android.net.Uri) -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onToggleTestPremium: (Boolean) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showCurrencyModal by remember { mutableStateOf(false) }
    val isLocalSession = AuthSession.isLocal(userEmail)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let(onExportJson)
    }

    // Full Screen Overlay & Drawer Panel
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { onDismiss() }
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.85f)
                        .clickable(enabled = false) {}, // Prevent clicks from passing to background
                    color = if (isDarkMode) AppChrome.pageDark else AppChrome.pageLight,
                    shape = RoundedCornerShape(topStart = AppShape.panel, bottomStart = AppShape.panel)
                ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp)
            ) {
                // Drawer Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Menu",
                        color = if (isDarkMode) Color.White else Color(0xFF121926),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(AppShape.chip))
                            .background(if (isDarkMode) Color(0xFF231F33) else Color(0xFFE2E8F0))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Menu",
                            tint = if (isDarkMode) Color.White else Color(0xFF121926),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Profile Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.panel),
                        color = if (isDarkMode) AppChrome.rowDark else Color(0xFFF4F7FE)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.4.dp, GoldAccent, CircleShape)
                                    .background(if (isDarkMode) Color(0xFF2B263B) else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userProfilePicUrl != null) {
                                    AsyncImage(
                                        model = userProfilePicUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = if (userEmail != null) userEmail.take(1).uppercase() else "G",
                                        color = GoldAccent,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = when {
                                    isLocalSession -> "LOCAL SESSION"
                                    userEmail != null -> "LOGGED IN AS"
                                    else -> "NOT SIGNED IN"
                                },
                                color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            val displayName = when {
                                isLocalSession -> "On this device"
                                userEmail == "iqbalhasandc200@gmail.com" -> "Iqbal Hasan"
                                userEmail != null -> userEmail.substringBefore("@").replace(".", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                                else -> "Guest User"
                            }

                            Text(
                                text = displayName,
                                color = if (isDarkMode) Color.White else Color(0xFF121926),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = when {
                                    isLocalSession -> "Saved on this phone"
                                    else -> userEmail ?: "Local Offline Session"
                                },
                                color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Sign Out / In Row
                    Surface(
                        onClick = {
                            when {
                                isLocalSession -> onLoginClick()
                                userEmail != null -> onLogoutClick()
                                else -> onLoginClick()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.panel),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF282436) else Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (AuthSession.isGoogle(userEmail)) Icons.Default.ExitToApp else Icons.Default.Login,
                                contentDescription = "Account",
                                tint = if (AuthSession.isGoogle(userEmail)) Color(0xFFFB7185) else GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (AuthSession.isGoogle(userEmail)) "Sign Out" else "Sign In with Google",
                                color = if (AuthSession.isGoogle(userEmail)) Color(0xFFFB7185) else GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }



                    // 3. SAVE YOUR PROGRESS
                    Text(
                        text = "DATA & PREMIUM",
                        color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Surface(
                        onClick = {
                            exportLauncher.launch("buck-manager-backup.json")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.card),
                        color = if (isDarkMode) AppChrome.rowDark else Color(0xFFF4F7FE),
                        border = BorderStroke(AppStroke.thin, if (isDarkMode) AppChrome.mutedDark else AppChrome.mutedLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(AppShape.chip))
                                    .background(GoldAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Export JSON",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Export JSON backup",
                                    color = if (isDarkMode) Color.White else Color(0xFF121926),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Keep this file. Drive sync comes later.",
                                    color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = onRestorePurchases,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.card),
                        color = if (isDarkMode) AppChrome.rowDark else Color(0xFFF4F7FE),
                        border = BorderStroke(AppStroke.thin, if (isDarkMode) AppChrome.mutedDark else AppChrome.mutedLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(AppShape.chip))
                                    .background(Color(0xFF4ECB8D).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Restore purchases",
                                    tint = Color(0xFF4ECB8D),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (monetization.isPremium) "Lifetime Premium active" else "Restore purchases",
                                    color = if (isDarkMode) Color.White else Color(0xFF121926),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (monetization.isPremium) "Tap to re-check this Play account" else "Already bought? Recover it here.",
                                    color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    if (com.buckmanager.app.BuildConfig.DEBUG) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.card),
                            color = if (isDarkMode) AppChrome.rowDark else Color(0xFFF4F7FE)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(AppShape.chip))
                                        .background(GoldAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Test Premium",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Test Premium",
                                        color = if (isDarkMode) Color.White else Color(0xFF121926),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Debug only. Unlock customization without Play Billing.",
                                        color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                                Switch(
                                    checked = monetization.isPremium,
                                    onCheckedChange = onToggleTestPremium,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoldAccent,
                                        checkedTrackColor = GoldAccent.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = onOpenCustomizeWidget,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.card),
                        color = if (isDarkMode) AppChrome.rowDark else Color(0xFFF4F7FE),
                        border = BorderStroke(AppStroke.thin, if (isDarkMode) AppChrome.mutedDark else AppChrome.mutedLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(AppShape.chip))
                                    .background(Color(0xFF3673FC).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = "Goal widget",
                                    tint = Color(0xFF3673FC),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Goal homescreen widget",
                                    color = if (isDarkMode) Color.White else Color(0xFF121926),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Style and pin the goal widget",
                                    color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.card),
                        color = if (isDarkMode) AppChrome.rowDark else Color(0xFFF4F7FE),
                        border = BorderStroke(AppStroke.thin, if (isDarkMode) AppChrome.mutedDark else AppChrome.mutedLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(AppShape.chip))
                                    .background(Color(0xFF6C5CE7).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Drive coming soon",
                                    tint = if (isDarkMode) Color(0xFFA29BFE) else Color(0xFF6C5CE7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Google Drive sync",
                                    color = if (isDarkMode) Color.White else Color(0xFF121926),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Coming soon — use Export JSON for now" +
                                        if (lastBackupDate != null) " · Last: $lastBackupDate" else "",
                                    color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }



                    // 4. SETTINGS
                    Text(
                        text = "SETTINGS",
                        color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppShape.panel),
                        color = if (isDarkMode) AppChrome.rowDark else Color(0xFFF4F7FE)
                    ) {
                        Column {
                            // Theme Row
                            val isCustomUnlocked = !isCustomizationLocked
                            
                            if (!isThemeCustomized) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                            contentDescription = "Theme",
                                            tint = if (isThemeCustomized) (if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A)) else (if (isDarkMode) Color(0xFFA29BFE) else Color(0xFF6C5CE7)),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("Theme Mode", color = if (isDarkMode) Color.White else Color(0xFF121926), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            if (isThemeCustomized) {
                                                Text("Disabled (Custom active)", color = Color(0xFFFB7185), fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    if (isThemeCustomized) {
                                        Surface(
                                            shape = RoundedCornerShape(AppShape.card),
                                            color = if (isDarkMode) Color(0xFF282436) else Color(0xFFE2E8F0),
                                            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF38334A) else Color(0xFFCBD5E1))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .clickable { onResetCustomization() }
                                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Custom",
                                                    color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Reset",
                                                    color = GoldAccent,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(AppShape.chip))
                                                .background(if (isDarkMode) Color(0xFF282436) else Color(0xFFE2E8F0))
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(AppShape.tick),
                                                color = if (isDarkMode) GoldAccent else Color.Transparent,
                                                modifier = Modifier.clickable { onToggleTheme(true) }
                                            ) {
                                                Text(
                                                    text = "Dark",
                                                    color = if (isDarkMode) Color.White else Color(0xFF5A667A),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(AppShape.tick),
                                                color = if (!isDarkMode) GoldAccent else Color.Transparent,
                                                modifier = Modifier.clickable { onToggleTheme(false) }
                                            ) {
                                                Text(
                                                    text = "Light",
                                                    color = if (!isDarkMode) Color.White else Color(0xFF9CA3AF),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Divider(color = if (isDarkMode) Color(0xFF282436) else Color(0xFFE2E8F0))
                            }

                            // Daily Reminder Row
                            // Currency Row
                            Surface(
                                onClick = { showCurrencyModal = true },
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = "Currency",
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("Currency Settings", color = if (isDarkMode) Color.White else Color(0xFF121926), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            Text(com.buckmanager.app.model.CurrencyConfig.currencyCode, color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A), fontSize = 11.sp)
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A)
                                    )
                                }
                            }
                        }
                    }



                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        }
    }
    }

    val dialogContainer = if (isDarkMode) Color(0xFF181C26) else Color(0xFFFFFFFF)
    val dialogTitleColor = if (isDarkMode) Color.White else Color(0xFF121926)
    val dialogSubtitleColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A)
    val dialogBorderColor = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)


    CurrencyModal(
        visible = showCurrencyModal,
        isDarkMode = isDarkMode,
        onDismiss = { showCurrencyModal = false },
        onSelect = { code, symbol ->
            com.buckmanager.app.model.CurrencyConfig.save(context, code, symbol)
            showCurrencyModal = false
            onCurrencyChanged()
        }
    )
}
@Composable
fun GoalDepositModal(
    visible: Boolean,
    goalName: String,
    currentAmount: Double,
    targetAmount: Double,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onConfirmDeposit: (Double) -> Unit
) {
    if (!visible) return

    var amountText by remember { mutableStateOf("") }
    var isWithdraw by remember { mutableStateOf(false) }

    val dialogContainer = if (isDarkMode) Color(0xFF181C26) else Color(0xFFFFFFFF)
    val dialogTitleColor = if (isDarkMode) Color.White else Color(0xFF121926)
    val dialogSubtitleColor = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A)
    val dialogBorderColor = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogContainer,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎯 ", fontSize = 20.sp)
                Column {
                    Text(
                        text = if (isWithdraw) "Tarik Tabungan Goal" else "Nabung ke Goal",
                        color = dialogTitleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = goalName,
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppShape.chip))
                        .background(if (isDarkMode) Color(0xFF0F1117) else Color(0xFFF1F5F9))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(AppShape.tick))
                            .background(if (!isWithdraw) GoldAccent else Color.Transparent)
                            .clickable { isWithdraw = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Deposit (Nabung)",
                            color = if (!isWithdraw) Color.White else dialogSubtitleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(AppShape.tick))
                            .background(if (isWithdraw) Color(0xFFFB7185) else Color.Transparent)
                            .clickable { isWithdraw = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "- Withdraw (Tarik)",
                            color = if (isWithdraw) Color.White else dialogSubtitleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text(if (isWithdraw) "Jumlah Penarikan (Rp)" else "Jumlah Setoran (Rp)", color = dialogSubtitleColor) },
                    placeholder = { Text("Contoh: 100000", color = dialogSubtitleColor.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.button),
                    colors = appTextFieldColors(isDarkMode)
                )

                Text("Pilihan Cepat:", color = dialogSubtitleColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                val presets = listOf(20000.0, 50000.0, 100000.0, 250000.0, 500000.0, 1000000.0)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(AppShape.tick),
                            color = if (isDarkMode) Color(0xFF282436) else Color(0xFFE2E8F0),
                            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF38334A) else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { amountText = preset.toLong().toString() }
                        ) {
                            Text(
                                text = "${com.buckmanager.app.model.CurrencyConfig.symbol}${preset.toLong() / 1000}rb",
                                color = dialogTitleColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        val finalAmt = if (isWithdraw) -amt else amt
                        onConfirmDeposit(finalAmt)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWithdraw) Color(0xFFFB7185) else GoldAccent
                )
            ) {
                Text(
                    text = if (isWithdraw) "Tarik Tabungan" else "Simpan Tabungan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = dialogSubtitleColor)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCustomizerModal(
    visible: Boolean,
    fundGoalConfig: FundGoalConfig,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSaveFundGoal: (FundGoalConfig) -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // State for Goal Widget customization
    var currentBgColor by remember(fundGoalConfig) { mutableStateOf(fundGoalConfig.backgroundColorHex) }
    var currentBgImageUri by remember(fundGoalConfig) { mutableStateOf(fundGoalConfig.backgroundImageUri ?: "") }
    var currentDimOpacity by remember(fundGoalConfig) { mutableFloatStateOf(fundGoalConfig.dimOpacity.toFloat()) }

    var currentBorderRadius by remember(fundGoalConfig) { mutableFloatStateOf(fundGoalConfig.radiusTopLeft.toFloat()) }
    var currentBorderWidth by remember(fundGoalConfig) { mutableFloatStateOf(fundGoalConfig.borderTop.toFloat()) }
    var currentBorderColor by remember(fundGoalConfig) { mutableStateOf(fundGoalConfig.borderColorHex) }

    var currentPadding by remember(fundGoalConfig) { mutableFloatStateOf(fundGoalConfig.paddingTop.toFloat()) }

    var currentLabelColor by remember(fundGoalConfig) { mutableStateOf(fundGoalConfig.labelColorHex) } // Accent / percentage
    var currentValueColor by remember(fundGoalConfig) { mutableStateOf(fundGoalConfig.valueColorHex) } // Title & amount text
    var currentBtnBgColor by remember(fundGoalConfig) { mutableStateOf(fundGoalConfig.btnBgColorHex) }
    var currentBtnTextColor by remember(fundGoalConfig) { mutableStateOf(fundGoalConfig.btnTextColorHex) }

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
            currentBgImageUri = persistBackgroundImage(context, croppedUri) ?: croppedUri
            croppingImageUri = null
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDarkMode) Color(0xFF0F1117) else Color(0xFFF8FAFC),
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
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📱 ", fontSize = 18.sp)
                        Text(
                            text = "Customize Home Screen Widget",
                            color = if (isDarkMode) Color.White else Color(0xFF121926),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = "Pengaturan Kostumisasi Widget Goal Android Home",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF181C26) else Color(0xFFE2E8F0))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (isDarkMode) Color.White else Color(0xFF121926),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // LIVE PREVIEW OF ANDROID GOAL WIDGET
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = "PREVIEW WIDGET ASLI:",
                    color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(currentBorderRadius.dp),
                    color = if (currentBgImageUri.isNotBlank()) Color.Transparent else parseHexColor(currentBgColor, Color(0xFF181C26)),
                    border = BorderStroke(currentBorderWidth.dp, parseHexColor(currentBorderColor, GoldAccent)),
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (currentBgImageUri.isNotBlank()) {
                            AsyncImage(
                                model = currentBgImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = (currentDimOpacity / 100f).coerceIn(0f, 0.98f)))
                            )
                        }

                        Column(
                            modifier = Modifier.padding(currentPadding.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top Row: Title & Percentage Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val goalName = fundGoalConfig.name.ifBlank { "Target Savings" }
                                Text(
                                    text = if (goalName.startsWith("🎯")) goalName else "🎯 $goalName",
                                    color = parseHexColor(currentValueColor, Color.White),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                val progressPct = if (fundGoalConfig.targetAmount > 0) {
                                    ((fundGoalConfig.currentAmount / fundGoalConfig.targetAmount) * 100).toInt()
                                } else 0
                                Text(
                                    text = "$progressPct%",
                                    color = parseHexColor(currentLabelColor, GoldAccent),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Amount Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = formatRp(fundGoalConfig.currentAmount),
                                    color = parseHexColor(currentValueColor, Color.White),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Target: ${formatRp(fundGoalConfig.targetAmount)}",
                                    color = parseHexColor(currentLabelColor, Color(0xFF9CA3AF)),
                                    fontSize = 11.sp
                                )
                            }

                            // Progress Bar
                            val pctFloat = if (fundGoalConfig.targetAmount > 0) {
                                (fundGoalConfig.currentAmount / fundGoalConfig.targetAmount).toFloat().coerceIn(0f, 1f)
                            } else 0f
                            LinearProgressIndicator(
                                progress = { pctFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = parseHexColor(currentLabelColor, GoldAccent),
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )

                            // Deposit Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(AppShape.tick))
                                        .background(parseHexColor(currentBtnBgColor, GoldAccent)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+ Deposit", color = parseHexColor(currentBtnTextColor, Color.White), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(AppShape.tick))
                                        .background(parseHexColor(currentBtnBgColor, GoldAccent))
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+50rb", color = parseHexColor(currentBtnTextColor, Color.White), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(AppShape.tick))
                                        .background(parseHexColor(currentBtnBgColor, GoldAccent))
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+100rb", color = parseHexColor(currentBtnTextColor, Color.White), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("Widget Type", "Colors", "Layout")

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = { Divider(color = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, color = if (selectedTab == index) GoldAccent else if (isDarkMode) Color.White else Color(0xFF121926)) }
                    )
                }
            }

            // CONTROLS LIST
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        Text(
                            "Currently only Goal Widget customization is supported.",
                            color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                            fontSize = 14.sp
                        )
                    }
                    1 -> {
                        RichColorPicker(
                            title = "🎨 Widget Background Color",
                            selectedColorHex = currentBgColor,
                            onColorSelected = { currentBgColor = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "🖼 Border Color",
                            selectedColorHex = currentBorderColor,
                            onColorSelected = { currentBorderColor = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "🏷 Progress Bar & Accent Color",
                            selectedColorHex = currentLabelColor,
                            onColorSelected = { currentLabelColor = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "✍️ Title & Amount Text Color",
                            selectedColorHex = currentValueColor,
                            onColorSelected = { currentValueColor = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "🔘 Deposit Button Background Color",
                            selectedColorHex = currentBtnBgColor,
                            onColorSelected = { currentBtnBgColor = it },
                            isDarkMode = isDarkMode
                        )
                        RichColorPicker(
                            title = "🔤 Deposit Button Text Color",
                            selectedColorHex = currentBtnTextColor,
                            onColorSelected = { currentBtnTextColor = it },
                            isDarkMode = isDarkMode
                        )
                    }
                    2 -> {
                        // Sliders
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🔘 Border Radius (Corner Roundness)", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A), fontSize = 12.sp)
                                Text("${currentBorderRadius.toInt()} dp", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Slider(
                                value = currentBorderRadius,
                                onValueChange = { currentBorderRadius = it },
                                valueRange = 0f..32f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📦 Inner Padding", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A), fontSize = 12.sp)
                                Text("${currentPadding.toInt()} dp", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Slider(
                                value = currentPadding,
                                onValueChange = { currentPadding = it },
                                valueRange = 4f..32f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("✏️ Border Width", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A), fontSize = 12.sp)
                                Text("${currentBorderWidth.toInt()} dp", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Slider(
                                value = currentBorderWidth,
                                onValueChange = { currentBorderWidth = it },
                                valueRange = 0f..8f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )
                        }

                        Divider(color = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1))

                        Text("🖼 Background Image & Crop", color = if (isDarkMode) Color.White else Color(0xFF121926), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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

                            if (currentBgImageUri.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { croppingImageUri = currentBgImageUri },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDarkMode) Color.White else Color.Black)
                                ) {
                                    Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Crop", fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { currentBgImageUri = "" },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFFB7185))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear")
                                }
                            }
                        }

                        OutlinedTextField(
                            value = currentBgImageUri,
                            onValueChange = { currentBgImageUri = it },
                            label = { Text("Image URL or Path") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.button),
                            colors = appTextFieldColors(isDarkMode)
                        )

                        Text("Sample Wallpapers", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A), fontSize = 11.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PresetBackgroundImages) { url ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(AppShape.tick))
                                        .border(2.dp, if (currentBgImageUri == url) GoldAccent else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { currentBgImageUri = url }
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
                                Text("Darken / Dim Level", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A), fontSize = 11.sp)
                                Text("${currentDimOpacity.toInt()}%", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Slider(
                                value = currentDimOpacity,
                                onValueChange = { currentDimOpacity = it },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                            )
                        }
                    }
                }
            }

            // Action Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        GoalAppWidgetProvider.pinWidgetToHomeScreen(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.card),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent)
                ) {
                    Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📌 PASANG WIDGET GOAL KE HOME SCREEN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val updatedGoal = fundGoalConfig.copy(
                            backgroundColorHex = currentBgColor,
                            backgroundImageUri = currentBgImageUri.ifBlank { null },
                            dimOpacity = currentDimOpacity.toInt(),
                            radiusTopLeft = currentBorderRadius.toInt(),
                            radiusTopRight = currentBorderRadius.toInt(),
                            radiusBottomRight = currentBorderRadius.toInt(),
                            radiusBottomLeft = currentBorderRadius.toInt(),
                            borderTop = currentBorderWidth.toInt(),
                            borderRight = currentBorderWidth.toInt(),
                            borderBottom = currentBorderWidth.toInt(),
                            borderLeft = currentBorderWidth.toInt(),
                            borderColorHex = currentBorderColor,
                            paddingTop = currentPadding.toInt(),
                            paddingRight = currentPadding.toInt(),
                            paddingBottom = currentPadding.toInt(),
                            paddingLeft = currentPadding.toInt(),
                            labelColorHex = currentLabelColor,
                            valueColorHex = currentValueColor,
                            btnBgColorHex = currentBtnBgColor,
                            btnTextColorHex = currentBtnTextColor
                        )
                        onSaveFundGoal(updatedGoal)
                        GoalAppWidgetProvider.updateAllWidgets(context)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppShape.card),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("SIMPAN KOSTUMISASI WIDGET", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }
    }
}
