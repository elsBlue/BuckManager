package com.buckmanager.app.ui.components

import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buckmanager.app.ui.AppShape
import kotlin.math.roundToInt

@Composable
fun RichColorPicker(
    title: String,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    isDarkMode: Boolean = true
) {
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val border = if (isDarkMode) Color(0xFF2A273C) else Color(0xFFCBD5E1)
    
    val parsedColor = parseHexColor(selectedColorHex, Color.Transparent)
    val hsv = remember(parsedColor) { 
        val arr = FloatArray(3)
        android.graphics.Color.colorToHSV(parsedColor.toArgb(), arr)
        arr
    }
    
    var hue by remember(hsv) { mutableFloatStateOf(hsv[0]) }
    var sat by remember(hsv) { mutableFloatStateOf(hsv[1]) }
    var value by remember(hsv) { mutableFloatStateOf(hsv[2]) }
    var alpha by remember(parsedColor) { mutableFloatStateOf(parsedColor.alpha) }
    var showFineTune by remember { mutableStateOf(false) }
    
    fun updateColor() {
        val newColor = Color.hsv(hue, sat, value, alpha)
        val a = (newColor.alpha * 255).toInt()
        val r = (newColor.red * 255).toInt()
        val g = (newColor.green * 255).toInt()
        val b = (newColor.blue * 255).toInt()
        
        val hexString = if (a < 255) {
            String.format("#%02X%02X%02X%02X", a, r, g, b)
        } else {
            String.format("#%02X%02X%02X", r, g, b)
        }
        onColorSelected(hexString)
    }

    val hueBrush = remember {
        Brush.horizontalGradient(
            listOf(
                Color.hsv(0f, 1f, 1f),
                Color.hsv(60f, 1f, 1f),
                Color.hsv(120f, 1f, 1f),
                Color.hsv(180f, 1f, 1f),
                Color.hsv(240f, 1f, 1f),
                Color.hsv(300f, 1f, 1f),
                Color.hsv(360f, 1f, 1f)
            )
        )
    }
    
    val satBrush = remember(hue, value) {
        Brush.horizontalGradient(
            listOf(
                Color.hsv(hue, 0f, value),
                Color.hsv(hue, 1f, value)
            )
        )
    }
    
    val valBrush = remember(hue, sat) {
        Brush.horizontalGradient(
            listOf(
                Color.hsv(hue, sat, 0f),
                Color.hsv(hue, sat, 1f)
            )
        )
    }
    
    val alphaBrush = remember(hue, sat, value) {
        Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                Color.hsv(hue, sat, value)
            )
        )
    }

    val currentColor = Color.hsv(hue, sat, value, alpha)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (title.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = if (showFineTune) "Hide sliders" else "Fine-tune",
                    color = Color(0xFFF59E0B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { showFineTune = !showFineTune }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorPresets.forEach { hex ->
                val selected = selectedColorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(hex))
                        .border(
                            if (selected) 2.dp else 1.dp,
                            if (selected) Color(0xFFF59E0B) else border,
                            CircleShape
                        )
                        .clickable { onColorSelected(hex) }
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(AppShape.chip))
                    .background(Color.LightGray)
                    .background(currentColor)
                    .border(1.dp, border, RoundedCornerShape(AppShape.chip))
            )
            OutlinedTextField(
                value = selectedColorHex,
                onValueChange = { onColorSelected(it) },
                label = { Text("Hex", color = textColor.copy(alpha = 0.7f)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(AppShape.button),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = border
                ),
                singleLine = true
            )
        }

        if (showFineTune) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HsvSlider(
                label = "H",
                value = hue,
                valueRange = 0f..360f,
                onValueChange = { hue = it; updateColor() },
                backgroundBrush = hueBrush,
                thumbColor = Color.hsv(hue, 1f, 1f),
                textColor = textColor,
                valueText = "${hue.roundToInt()}º"
            )
            
            HsvSlider(
                label = "S",
                value = sat,
                valueRange = 0f..1f,
                onValueChange = { sat = it; updateColor() },
                backgroundBrush = satBrush,
                thumbColor = Color.hsv(hue, sat, value),
                textColor = textColor,
                valueText = "${(sat * 100).roundToInt()}%"
            )
            
            HsvSlider(
                label = "V",
                value = value,
                valueRange = 0f..1f,
                onValueChange = { value = it; updateColor() },
                backgroundBrush = valBrush,
                thumbColor = Color.hsv(hue, sat, value),
                textColor = textColor,
                valueText = "${(value * 100).roundToInt()}%"
            )
            
            HsvSlider(
                label = "A",
                value = alpha,
                valueRange = 0f..1f,
                onValueChange = { alpha = it; updateColor() },
                backgroundBrush = alphaBrush,
                thumbColor = Color.hsv(hue, sat, value, alpha),
                textColor = textColor,
                valueText = "${(alpha * 255).roundToInt()}",
                baseColor = Color.LightGray
            )
        }
        }
    }
}
