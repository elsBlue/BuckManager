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
import com.buckmanager.app.ui.AmountVisualTransformation
import com.buckmanager.app.ui.AppChrome
import com.buckmanager.app.ui.AppShape
import com.buckmanager.app.ui.AppStroke
import com.buckmanager.app.ui.GoldAccent
import com.buckmanager.app.ui.appTextFieldColors
import androidx.core.graphics.toColorInt
import androidx.compose.ui.platform.LocalContext

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
    var target by remember(currentConfig) { mutableStateOf(filterAmountDigits(currentConfig.targetAmount.toLong().toString())) }
    var current by remember(currentConfig) { mutableStateOf(filterAmountDigits(currentConfig.currentAmount.toLong().toString())) }

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
    var iconName by remember(currentConfig) { mutableStateOf(currentConfig.iconName) }
    var iconColorHex by remember(currentConfig) { mutableStateOf(currentConfig.iconColorHex) }
    var nameColorHex by remember(currentConfig) { mutableStateOf(currentConfig.nameColorHex) }
    var nameFontFamily by remember(currentConfig) { mutableStateOf(currentConfig.nameFontFamily) }
    var percentColorHex by remember(currentConfig) { mutableStateOf(currentConfig.percentColorHex) }
    var currentSavedColorHex by remember(currentConfig) { mutableStateOf(currentConfig.currentSavedColorHex) }
    var targetAmountColorHex by remember(currentConfig) { mutableStateOf(currentConfig.targetAmountColorHex) }
    var remainingColorHex by remember(currentConfig) { mutableStateOf(currentConfig.remainingColorHex) }
    var progressTrackColorHex by remember(currentConfig) { mutableStateOf(currentConfig.progressTrackColorHex) }
    var progressFillColorHex by remember(currentConfig) { mutableStateOf(currentConfig.progressFillColorHex) }
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
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize Fund Goal",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
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
            FundGoalLook(
                config = currentConfig.copy(
                    name = name,
                    targetAmount = parseAmountInput(target),
                    currentAmount = parseAmountInput(current),
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
                    borderColorHex = borderColorHex,
                    paddingTop = paddingTop,
                    paddingRight = paddingRight,
                    paddingBottom = paddingBottom,
                    paddingLeft = paddingLeft,
                    useGradient = useGradient,
                    gradientColors = if (useGradient) listOf(gradColor1, gradColor2) else emptyList(),
                    gradientAngle = gradientAngle,
                    labelColorHex = labelColor,
                    valueColorHex = valueColor,
                    btnBgColorHex = btnBgColorHex,
                    btnTextColorHex = btnTextColorHex,
                    iconName = iconName,
                    iconColorHex = iconColorHex,
                    nameColorHex = nameColorHex,
                    nameFontFamily = nameFontFamily,
                    percentColorHex = percentColorHex,
                    currentSavedColorHex = currentSavedColorHex,
                    targetAmountColorHex = targetAmountColorHex,
                    remainingColorHex = remainingColorHex,
                    progressTrackColorHex = progressTrackColorHex,
                    progressFillColorHex = progressFillColorHex
                ),
                isDarkMode = isDarkMode,
                showChrome = false,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            )

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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            onValueChange = { target = filterAmountDigits(it) },
                            label = { Text("Target Amount (${CurrencyConfig.currencyCode})", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = AmountVisualTransformation,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.button),
                            colors = appTextFieldColors(isDarkMode)
                        )
                        OutlinedTextField(
                            value = current,
                            onValueChange = { current = filterAmountDigits(it) },
                            label = { Text("Current Saved Amount", color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF64748B)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = AmountVisualTransformation,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(AppShape.button),
                            colors = appTextFieldColors(isDarkMode)
                        )
                        Text("Icon", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(IconPresets) { icon ->
                                val selected = iconName == icon
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) GoldAccent.copy(alpha = 0.2f) else if (isDarkMode) Color(0xFF181C26) else Color(0xFFF1F5F9))
                                        .border(2.dp, if (selected) GoldAccent else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { iconName = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(getIconVector(icon), contentDescription = icon, tint = parseHexColor(iconColorHex, GoldAccent), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        RichColorPicker("Icon color", iconColorHex, { iconColorHex = it }, isDarkMode)
                        Text("Goal name font", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("sans" to "Sans", "serif" to "Serif", "mono" to "Mono").forEach { (id, label) ->
                                val selected = nameFontFamily == id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(if (selected) GoldAccent.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(1.dp, if (selected) GoldAccent else sheetBorder, RoundedCornerShape(50))
                                        .clickable { nameFontFamily = id }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        label,
                                        color = if (selected) GoldAccent else textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = goalFontFamily(id)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        Text("Goal look", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        RichColorPicker("Goal name", nameColorHex, { nameColorHex = it }, isDarkMode)
                        RichColorPicker("Percent", percentColorHex, { percentColorHex = it }, isDarkMode)
                        RichColorPicker("Current saved", currentSavedColorHex, { currentSavedColorHex = it }, isDarkMode)
                        RichColorPicker("Target amount", targetAmountColorHex, { targetAmountColorHex = it }, isDarkMode)
                        RichColorPicker("Remaining", remainingColorHex, { remainingColorHex = it }, isDarkMode)
                        RichColorPicker("Progress track", progressTrackColorHex, { progressTrackColorHex = it }, isDarkMode)
                        RichColorPicker("Progress fill", progressFillColorHex, { progressFillColorHex = it }, isDarkMode)
                        HorizontalDivider(color = sheetBorder.copy(alpha = 0.3f))
                        Text("Card", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gradient background", color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Switch(
                                checked = useGradient,
                                onCheckedChange = { useGradient = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = GoldAccent.copy(alpha = 0.5f))
                            )
                        }
                        if (useGradient) {
                            RichColorPicker(title = "Gradient 1", selectedColorHex = gradColor1, onColorSelected = { gradColor1 = it }, isDarkMode = isDarkMode)
                            RichColorPicker(title = "Gradient 2", selectedColorHex = gradColor2, onColorSelected = { gradColor2 = it }, isDarkMode = isDarkMode)
                            CompactSlider(label = "Angle", value = gradientAngle, onValueChange = { gradientAngle = it }, valueRange = 0f..360f, unit = "°", isDarkMode = isDarkMode)
                        } else {
                            RichColorPicker(title = "Background", selectedColorHex = selectedBg, onColorSelected = { selectedBg = it }, isDarkMode = isDarkMode)
                        }
                        RichColorPicker(title = "Border", selectedColorHex = borderColorHex, onColorSelected = { borderColorHex = it }, isDarkMode = isDarkMode)
                        RichColorPicker(title = "Button fill", selectedColorHex = btnBgColorHex, onColorSelected = { btnBgColorHex = it }, isDarkMode = isDarkMode)
                        RichColorPicker(title = "Button text", selectedColorHex = btnTextColorHex, onColorSelected = { btnTextColorHex = it }, isDarkMode = isDarkMode)
                    }
                    2 -> {
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
                        CompactSlider(
                            label = "Border",
                            value = borderTop.toFloat(),
                            onValueChange = {
                                val newVal = it.toInt()
                                borderTop = newVal
                                borderRight = newVal
                                borderBottom = newVal
                                borderLeft = newVal
                            },
                            valueRange = 0f..10f,
                            isDarkMode = isDarkMode
                        )
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

                        CompactSlider(
                            label = "Dim",
                            value = dimOpacity,
                            onValueChange = { dimOpacity = it },
                            valueRange = 0f..100f,
                            unit = "%",
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }

            // BOTTOM ACTION BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        name = currentConfig.name
                        target = filterAmountDigits(currentConfig.targetAmount.toLong().toString())
                        current = filterAmountDigits(currentConfig.currentAmount.toLong().toString())
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
                        iconName = currentConfig.iconName
                        iconColorHex = currentConfig.iconColorHex
                        nameColorHex = currentConfig.nameColorHex
                        nameFontFamily = currentConfig.nameFontFamily
                        percentColorHex = currentConfig.percentColorHex
                        currentSavedColorHex = currentConfig.currentSavedColorHex
                        targetAmountColorHex = currentConfig.targetAmountColorHex
                        remainingColorHex = currentConfig.remainingColorHex
                        progressTrackColorHex = currentConfig.progressTrackColorHex
                        progressFillColorHex = currentConfig.progressFillColorHex
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
                                targetAmount = parseAmountInput(target),
                                currentAmount = parseAmountInput(current),
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
                                labelColorHex = progressFillColorHex,
                                valueColorHex = nameColorHex,
                                borderColorHex = borderColorHex,
                                btnBgColorHex = btnBgColorHex,
                                btnTextColorHex = btnTextColorHex,
                                iconName = iconName,
                                iconColorHex = iconColorHex,
                                nameColorHex = nameColorHex,
                                nameFontFamily = nameFontFamily,
                                percentColorHex = percentColorHex,
                                currentSavedColorHex = currentSavedColorHex,
                                targetAmountColorHex = targetAmountColorHex,
                                remainingColorHex = remainingColorHex,
                                progressTrackColorHex = progressTrackColorHex,
                                progressFillColorHex = progressFillColorHex
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
    onCurrencyChanged: () -> Unit = {},
    onExportJson: (android.net.Uri) -> Unit = {},
    onShareLook: (String) -> Unit = {},
    onImportLook: (android.net.Uri) -> Unit = {},
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
    val importLookLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let(onImportLook)
    }
    var showShareLookDialog by remember { mutableStateOf(false) }
    var shareLookName by remember { mutableStateOf("My Buck look") }

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

                            Surface(
                                onClick = { showShareLookDialog = true },
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
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share look",
                                            tint = GoldAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("Share look", color = if (isDarkMode) Color.White else Color(0xFF121926), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "Colors and photos as a file. Not your money.",
                                                color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A))
                                }
                            }

                            Divider(color = if (isDarkMode) Color(0xFF282436) else Color(0xFFE2E8F0))

                            Surface(
                                onClick = { importLookLauncher.launch("*/*") },
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
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Import look",
                                            tint = Color(0xFF3673FC),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("Import look", color = if (isDarkMode) Color.White else Color(0xFF121926), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "Apply a .bucktheme file from a friend",
                                                color = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A),
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF5A667A))
                                }
                            }

                            Divider(color = if (isDarkMode) Color(0xFF282436) else Color(0xFFE2E8F0))

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

    if (showShareLookDialog) {
        AlertDialog(
            onDismissRequest = { showShareLookDialog = false },
            containerColor = dialogContainer,
            title = {
                Text("Share look", color = dialogTitleColor, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Sends colors, photos, and shapes. Balances, envelope %, and goal amounts stay private. Matching envelopes (Needs, Wants, Savings, Main) pick up the look.",
                        color = dialogSubtitleColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    OutlinedTextField(
                        value = shareLookName,
                        onValueChange = { shareLookName = it.take(40) },
                        label = { Text("Look name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = appTextFieldColors(isDarkMode)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showShareLookDialog = false
                    onShareLook(shareLookName.ifBlank { "My Buck look" })
                }) {
                    Text("Share", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareLookDialog = false }) {
                    Text("Cancel", color = dialogSubtitleColor)
                }
            }
        )
    }

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
                    onValueChange = { amountText = filterAmountDigits(it) },
                    label = { Text(if (isWithdraw) "Jumlah Penarikan (Rp)" else "Jumlah Setoran (Rp)", color = dialogSubtitleColor) },
                    placeholder = { Text("Contoh: 100.000", color = dialogSubtitleColor.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = AmountVisualTransformation,
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
                    val amt = parseAmountInput(amountText)
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
