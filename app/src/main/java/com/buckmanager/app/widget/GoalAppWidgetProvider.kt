package com.buckmanager.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.buckmanager.app.MainActivity
import com.buckmanager.app.R
import com.buckmanager.app.model.FundGoalConfig
import com.buckmanager.app.model.formatRp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Homescreen goal progress widget (display-only).
 * Tapping opens the app; deposits happen inside the app UI.
 */
class GoalAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val PREFS_NAME = "buckmanager_widget_prefs"
        const val KEY_FUND_GOAL = "fund_goal_json"

        private val json = Json { ignoreUnknownKeys = true }

        fun saveGoalToPrefs(context: Context, fundGoal: FundGoalConfig) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_FUND_GOAL, json.encodeToString(fundGoal)).apply()
            updateAllWidgets(context)
        }

        fun getGoalFromPrefs(context: Context): FundGoalConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_FUND_GOAL, null)
            return if (!jsonStr.isNullOrBlank()) {
                try {
                    json.decodeFromString<FundGoalConfig>(jsonStr)
                } catch (e: Exception) {
                    FundGoalConfig()
                }
            } else {
                FundGoalConfig()
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, GoalAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        internal const val MAX_WIDGET_BG_PIXELS = 600 * 280

        internal fun widgetCanvasSize(wDp: Int, hDp: Int): Pair<Int, Int> {
            val aspect = (wDp.coerceAtLeast(1).toFloat() / hDp.coerceAtLeast(1).toFloat()).coerceIn(1.2f, 4.5f)
            var width = kotlin.math.sqrt(MAX_WIDGET_BG_PIXELS * aspect).toInt().coerceIn(400, 720)
            var height = (width / aspect).toInt().coerceIn(160, 360)
            val pixels = width * height
            if (pixels > MAX_WIDGET_BG_PIXELS) {
                val scale = kotlin.math.sqrt(MAX_WIDGET_BG_PIXELS.toFloat() / pixels)
                width = (width * scale).toInt().coerceAtLeast(320)
                height = (height * scale).toInt().coerceAtLeast(140)
            }
            return width to height
        }

        internal fun widgetPixelSize(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ): Triple<Int, Int, Float> {
            var wDp = 260
            var hDp = 118
            try {
                val opts = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
                val maxW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
                val minH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
                val maxH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
                if (maxW > 0 || minW > 0) wDp = maxOf(maxW, minW)
                if (maxH > 0 || minH > 0) hDp = maxOf(maxH, minH)
            } catch (_: Exception) {}
            val (width, height) = widgetCanvasSize(wDp, hDp)
            val pxPerDp = width / wDp.coerceAtLeast(1).toFloat()
            return Triple(width, height, pxPerDp)
        }

        /**
         * Outer corner radii in px, matching the goal card. Radius 0 stays 0.
         */
        internal fun widgetCornerRadiiPx(config: FundGoalConfig, scale: Float): FloatArray {
            fun r(dp: Int) = (dp * scale).coerceAtLeast(0f)
            val tl = r(config.radiusTopLeft)
            val tr = r(config.radiusTopRight)
            val br = r(config.radiusBottomRight)
            val bl = r(config.radiusBottomLeft)
            return floatArrayOf(tl, tl, tr, tr, br, br, bl, bl)
        }

        internal fun widgetOutlineRadiusDp(config: FundGoalConfig): Float {
            return maxOf(
                config.radiusTopLeft,
                config.radiusTopRight,
                config.radiusBottomRight,
                config.radiusBottomLeft
            ).coerceAtLeast(0).toFloat()
        }

        private fun generateWidgetBackground(
            context: Context,
            config: FundGoalConfig,
            width: Int,
            height: Int,
            pxPerDp: Float
        ): android.graphics.Bitmap {
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val scale = pxPerDp.coerceAtLeast(0.5f)

            val outer = RectF(0f, 0f, width.toFloat(), height.toFloat())
            val radii = widgetCornerRadiiPx(config, scale)
            val outerPath = Path()
            outerPath.addRoundRect(outer, radii, Path.Direction.CW)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = try {
                android.graphics.Color.parseColor(config.backgroundColorHex)
            } catch (e: Exception) {
                android.graphics.Color.parseColor("#181C26")
            }
            paint.style = Paint.Style.FILL

            if (config.useGradient && config.gradientColors.isNotEmpty()) {
                try {
                    val parsed = config.gradientColors.map { android.graphics.Color.parseColor(it) }
                    val colors = if (parsed.size == 1) intArrayOf(parsed[0], parsed[0]) else parsed.toIntArray()
                    val angleRad = (config.gradientAngle % 360f) * (Math.PI / 180.0)
                    val cx = width / 2f
                    val cy = height / 2f
                    val radius = kotlin.math.sqrt((width / 2.0) * (width / 2.0) + (height / 2.0) * (height / 2.0))
                    val shader = LinearGradient(
                        (cx - radius * kotlin.math.cos(angleRad)).toFloat(),
                        (cy - radius * kotlin.math.sin(angleRad)).toFloat(),
                        (cx + radius * kotlin.math.cos(angleRad)).toFloat(),
                        (cy + radius * kotlin.math.sin(angleRad)).toFloat(),
                        colors,
                        null,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = shader
                } catch (_: Exception) {}
            }

            canvas.drawPath(outerPath, paint)

            if (!config.backgroundImageUri.isNullOrBlank()) {
                try {
                    val inputStream = com.buckmanager.app.utils.openUriInputStream(context, config.backgroundImageUri!!)
                    val bgBmp = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bgBmp != null) {
                        val layer = canvas.saveLayer(outer, null)
                        val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                        canvas.drawBitmap(
                            bgBmp,
                            coverSrcRect(bgBmp.width, bgBmp.height, width, height),
                            outer,
                            bmpPaint
                        )
                        if (config.dimOpacity > 0) {
                            val dimAlpha = (config.dimOpacity * 2.55).toInt().coerceIn(0, 255)
                            canvas.drawColor(android.graphics.Color.argb(dimAlpha, 0, 0, 0))
                        }
                        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                        maskPaint.color = android.graphics.Color.BLACK
                        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                        canvas.drawPath(outerPath, maskPaint)
                        canvas.restoreToCount(layer)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val topPx = config.borderTop * scale
            val rightPx = config.borderRight * scale
            val bottomPx = config.borderBottom * scale
            val leftPx = config.borderLeft * scale
            if (topPx > 0f || rightPx > 0f || bottomPx > 0f || leftPx > 0f) {
                val inner = RectF(
                    outer.left + leftPx,
                    outer.top + topPx,
                    outer.right - rightPx,
                    outer.bottom - bottomPx
                )
                if (inner.width() > 1f && inner.height() > 1f) {
                    val innerRadii = floatArrayOf(
                        (radii[0] - leftPx).coerceAtLeast(0f),
                        (radii[1] - topPx).coerceAtLeast(0f),
                        (radii[2] - rightPx).coerceAtLeast(0f),
                        (radii[3] - topPx).coerceAtLeast(0f),
                        (radii[4] - rightPx).coerceAtLeast(0f),
                        (radii[5] - bottomPx).coerceAtLeast(0f),
                        (radii[6] - leftPx).coerceAtLeast(0f),
                        (radii[7] - bottomPx).coerceAtLeast(0f)
                    )
                    val innerPath = Path()
                    innerPath.addRoundRect(inner, innerRadii, Path.Direction.CW)
                    val borderPath = Path()
                    borderPath.op(outerPath, innerPath, Path.Op.DIFFERENCE)
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    borderPaint.style = Paint.Style.FILL
                    borderPaint.color = try {
                        android.graphics.Color.parseColor(config.borderColorHex)
                    } catch (_: Exception) {
                        android.graphics.Color.TRANSPARENT
                    }
                    canvas.drawPath(borderPath, borderPaint)
                }
            }

            drawWidgetForeground(context, canvas, config, width, height, scale)
            return bitmap
        }

        private fun coverSrcRect(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Rect {
            if (srcW <= 0 || srcH <= 0 || dstW <= 0 || dstH <= 0) return Rect(0, 0, srcW.coerceAtLeast(1), srcH.coerceAtLeast(1))
            val srcAspect = srcW.toFloat() / srcH
            val dstAspect = dstW.toFloat() / dstH
            return if (srcAspect > dstAspect) {
                val newW = (srcH * dstAspect).toInt().coerceAtLeast(1)
                val left = ((srcW - newW) / 2).coerceAtLeast(0)
                Rect(left, 0, (left + newW).coerceAtMost(srcW), srcH)
            } else {
                val newH = (srcW / dstAspect).toInt().coerceAtLeast(1)
                val top = ((srcH - newH) / 2).coerceAtLeast(0)
                Rect(0, top, srcW, (top + newH).coerceAtMost(srcH))
            }
        }

        private fun drawWidgetForeground(
            context: Context,
            canvas: android.graphics.Canvas,
            config: FundGoalConfig,
            width: Int,
            height: Int,
            scale: Float
        ) {
            fun parseOr(hex: String, fallback: Int): Int = try {
                android.graphics.Color.parseColor(hex)
            } catch (_: Exception) { fallback }

            val labelColor = parseOr(config.labelColorHex, android.graphics.Color.parseColor("#D4A54A"))
            val iconColor = parseOr(config.iconColorHex, labelColor)
            val nameColor = parseOr(config.nameColorHex, parseOr(config.valueColorHex, android.graphics.Color.WHITE))
            val percentColor = parseOr(config.percentColorHex, nameColor)
            val currentColor = parseOr(config.currentSavedColorHex, nameColor)
            val targetColor = parseOr(config.targetAmountColorHex, labelColor)
            val remainingColor = parseOr(config.remainingColorHex, targetColor)
            val fillColor = parseOr(config.progressFillColorHex, labelColor)
            val trackColor = parseOr(config.progressTrackColorHex, android.graphics.Color.parseColor("#40808080"))

            val padL = config.paddingLeft * scale
            val padT = config.paddingTop * scale
            val padR = config.paddingRight * scale
            val padB = config.paddingBottom * scale
            val left = padL
            val right = width - padR
            val top = padT
            val bottom = height - padB
            if (right - left < 24f || bottom - top < 24f) return

            val titlePaint = textPaint(nameColor, 14f * scale, 700, config.nameFontFamily)
            val percentPaint = textPaint(percentColor, 14f * scale, 900)
            val amountPaint = textPaint(currentColor, 22f * scale, 900)
            val targetPaint = textPaint(targetColor, 12f * scale, 400)
            val remainingPaint = textPaint(remainingColor, 12f * scale, 400)
            remainingPaint.textAlign = Paint.Align.RIGHT

            val headerH = rowHeight(titlePaint).coerceAtLeast(16f * scale)
            val amountH = rowHeight(amountPaint)
            val metaH = rowHeight(targetPaint)
            val progressH = 8f * scale
            val used = headerH + amountH + metaH + progressH
            val extra = (bottom - top - used).coerceAtLeast(0f)
            val gap = extra / 3f

            var y = top
            val iconSize = 16f * scale
            val iconY = y + (headerH - iconSize) / 2f
            drawTintedIcon(context, canvas, goalIconRes(config.iconName), iconColor, left, iconY, iconSize)

            val percentText = run {
                val ratio = if (config.targetAmount > 0) {
                    (config.currentAmount / config.targetAmount).coerceIn(0.0, 1.0)
                } else 0.0
                "${(ratio * 100).toInt()}%"
            }
            val percentW = percentPaint.measureText(percentText)
            canvas.drawText(percentText, right - percentW, baseline(y, titlePaint, headerH), percentPaint)

            val titleX = left + iconSize + 6f * scale
            val titleMax = (right - percentW - 8f * scale - titleX).coerceAtLeast(24f)
            val title = ellipsize(config.name.ifBlank { "Target Savings" }, titlePaint, titleMax)
            canvas.drawText(title, titleX, baseline(y, titlePaint, headerH), titlePaint)

            y += headerH + gap
            val amount = ellipsize(formatRp(config.currentAmount), amountPaint, right - left)
            canvas.drawText(amount, left, baseline(y, amountPaint, amountH), amountPaint)

            y += amountH + gap
            val remainingAmount = (config.targetAmount - config.currentAmount).coerceAtLeast(0.0)
            val remainingText = if (remainingAmount <= 0) "Goal reached!" else "Remaining ${formatRp(remainingAmount)}"
            val targetText = "Target ${formatRp(config.targetAmount)}"
            val remainingW = remainingPaint.measureText(remainingText)
            val targetMax = (right - left - remainingW - 8f * scale).coerceAtLeast(24f)
            canvas.drawText(ellipsize(targetText, targetPaint, targetMax), left, baseline(y, targetPaint, metaH), targetPaint)
            canvas.drawText(remainingText, right, baseline(y, remainingPaint, metaH), remainingPaint)

            y += metaH + gap
            val barTop = y + ((bottom - y - progressH) / 2f).coerceAtLeast(0f)
            val bar = RectF(left, barTop, right, (barTop + progressH).coerceAtMost(bottom))
            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            barPaint.color = trackColor
            canvas.drawRoundRect(bar, progressH / 2f, progressH / 2f, barPaint)
            val ratio = if (config.targetAmount > 0) {
                (config.currentAmount / config.targetAmount).toFloat().coerceIn(0f, 1f)
            } else 0f
            if (ratio > 0f) {
                barPaint.color = fillColor
                canvas.drawRoundRect(
                    RectF(bar.left, bar.top, bar.left + bar.width() * ratio, bar.bottom),
                    progressH / 2f,
                    progressH / 2f,
                    barPaint
                )
            }
        }

        private fun textPaint(color: Int, sizePx: Float, weight: Int, familyName: String = "sans"): Paint {
            return Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                this.color = color
                textSize = sizePx.coerceAtLeast(8f)
                typeface = typefaceFor(familyName, weight)
                isFakeBoldText = weight >= 700 && Build.VERSION.SDK_INT < Build.VERSION_CODES.P
            }
        }

        private fun typefaceFor(familyName: String, weight: Int): Typeface {
            val family = when (familyName) {
                "serif" -> Typeface.SERIF
                "mono", "monospace" -> Typeface.MONOSPACE
                else -> Typeface.SANS_SERIF
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Typeface.create(family, weight.coerceIn(100, 900), false)
            } else if (weight >= 800) {
                Typeface.create("sans-serif-black", Typeface.NORMAL)
            } else if (weight >= 700) {
                Typeface.create(family, Typeface.BOLD)
            } else {
                Typeface.create(family, Typeface.NORMAL)
            }
        }

        private fun rowHeight(paint: Paint): Float {
            val fm = paint.fontMetrics
            return (fm.descent - fm.ascent)
        }

        private fun baseline(top: Float, paint: Paint, rowH: Float): Float {
            val fm = paint.fontMetrics
            val textH = fm.descent - fm.ascent
            return top + (rowH - textH) / 2f - fm.ascent
        }

        internal fun ellipsize(text: String, paint: Paint, maxWidth: Float): String {
            if (maxWidth <= 0f || paint.measureText(text) <= maxWidth) return text
            val ellipsis = "…"
            var end = text.length
            while (end > 0 && paint.measureText(text.take(end) + ellipsis) > maxWidth) end--
            return if (end <= 0) ellipsis else text.take(end) + ellipsis
        }

        private fun drawTintedIcon(
            context: Context,
            canvas: android.graphics.Canvas,
            resId: Int,
            color: Int,
            left: Float,
            top: Float,
            size: Float
        ) {
            try {
                val drawable = ContextCompat.getDrawable(context, resId)?.mutate() ?: return
                DrawableCompat.setTint(drawable, color)
                drawable.setBounds(left.toInt(), top.toInt(), (left + size).toInt(), (top + size).toInt())
                drawable.draw(canvas)
            } catch (_: Exception) {}
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                com.buckmanager.app.model.CurrencyConfig.load(context)
                val fundGoal = getGoalFromPrefs(context)
                val views = RemoteViews(context.packageName, R.layout.widget_goal_layout)

                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val appPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)
                applyWidgetOutline(views, fundGoal)

                try {
                    val (bw, bh, pxPerDp) = widgetPixelSize(context, appWidgetManager, appWidgetId)
                    val bitmap = generateWidgetBackground(context, fundGoal, bw, bh, pxPerDp)
                    views.setImageViewBitmap(R.id.widget_card_image, bitmap)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        val bitmap = generateWidgetBackground(context, fundGoal, 480, 200, 480f / 260f)
                        views.setImageViewBitmap(R.id.widget_card_image, bitmap)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    } catch (_: Exception) {}
                }
            }
        }

        private fun applyWidgetOutline(views: RemoteViews, config: FundGoalConfig) {
            val radiusDp = widgetOutlineRadiusDp(config)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setViewOutlinePreferredRadius(R.id.widget_root, radiusDp, TypedValue.COMPLEX_UNIT_DIP)
                views.setViewOutlinePreferredRadius(R.id.widget_card_image, radiusDp, TypedValue.COMPLEX_UNIT_DIP)
            }
            try {
                views.setBoolean(R.id.widget_root, "setClipToOutline", radiusDp > 0f)
                views.setBoolean(R.id.widget_card_image, "setClipToOutline", radiusDp > 0f)
            } catch (_: Exception) {}
        }

        fun pinWidgetToHomeScreen(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myProvider = ComponentName(context, GoalAppWidgetProvider::class.java)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val pinnedWidgetCallbackIntent = Intent(context, GoalAppWidgetProvider::class.java)
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    0,
                    pinnedWidgetCallbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            }
        }

        private fun goalIconRes(name: String): Int = when (name) {
            "star" -> R.drawable.ic_star
            "home" -> R.drawable.ic_home
            "heart" -> R.drawable.ic_heart
            "wallet", "money" -> R.drawable.ic_wallet
            "flight" -> R.drawable.ic_flight
            "shopping" -> R.drawable.ic_shopping
            "gift" -> R.drawable.ic_gift
            "coffee" -> R.drawable.ic_coffee
            "car" -> R.drawable.ic_car
            "book" -> R.drawable.ic_book
            else -> R.drawable.ic_flag
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }
}
