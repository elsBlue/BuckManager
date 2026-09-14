package com.buckmanager.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
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
         * Stroke is centered on the path, so inset by half the thickness or
         * the top/bottom of the ring gets clipped off the bitmap — then
         * centerCrop made that even worse on a wide widget.
         */
        internal fun widgetBorderInsetPx(maxBorderDp: Int, density: Float): Float {
            val stroke = maxBorderDp.coerceAtLeast(0) * density
            return if (stroke > 0f) stroke / 2f else 0f
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

            val maxBorderDp = maxOf(config.borderTop, config.borderBottom, config.borderLeft, config.borderRight)
            val stroke = maxBorderDp * scale
            val inset = widgetBorderInsetPx(maxBorderDp, scale)

            val radii = floatArrayOf(
                (config.radiusTopLeft * scale - inset).coerceAtLeast(0f),
                (config.radiusTopLeft * scale - inset).coerceAtLeast(0f),
                (config.radiusTopRight * scale - inset).coerceAtLeast(0f),
                (config.radiusTopRight * scale - inset).coerceAtLeast(0f),
                (config.radiusBottomRight * scale - inset).coerceAtLeast(0f),
                (config.radiusBottomRight * scale - inset).coerceAtLeast(0f),
                (config.radiusBottomLeft * scale - inset).coerceAtLeast(0f),
                (config.radiusBottomLeft * scale - inset).coerceAtLeast(0f)
            )
            val rect = android.graphics.RectF(
                inset,
                inset,
                width - inset,
                height - inset
            )
            val path = android.graphics.Path()
            path.addRoundRect(rect, radii, android.graphics.Path.Direction.CW)

            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            paint.color = try {
                android.graphics.Color.parseColor(config.backgroundColorHex)
            } catch (e: Exception) {
                android.graphics.Color.parseColor("#181C26")
            }
            paint.style = android.graphics.Paint.Style.FILL

            if (config.useGradient && config.gradientColors.isNotEmpty()) {
                try {
                    val colors = config.gradientColors.map { android.graphics.Color.parseColor(it) }.toIntArray()
                    val shader = android.graphics.LinearGradient(
                        0f, 0f, width.toFloat(), height.toFloat(), colors, null, android.graphics.Shader.TileMode.CLAMP
                    )
                    paint.shader = shader
                } catch (e: Exception) {
                }
            }

            canvas.drawPath(path, paint)

            if (!config.backgroundImageUri.isNullOrBlank()) {
                try {
                    val inputStream = com.buckmanager.app.utils.openUriInputStream(context, config.backgroundImageUri!!)
                    val bgBmp = android.graphics.BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bgBmp != null) {
                        canvas.save()
                        canvas.clipPath(path)
                        val srcRect = android.graphics.Rect(0, 0, bgBmp.width, bgBmp.height)
                        canvas.drawBitmap(bgBmp, srcRect, android.graphics.Rect(0, 0, width, height), null)
                        canvas.restore()

                        if (config.dimOpacity > 0) {
                            val dimPaint = android.graphics.Paint()
                            dimPaint.color = android.graphics.Color.argb((config.dimOpacity * 2.55).toInt(), 0, 0, 0)
                            canvas.drawPath(path, dimPaint)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (stroke > 0f) {
                val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                strokePaint.color = try {
                    android.graphics.Color.parseColor(config.borderColorHex)
                } catch (e: Exception) {
                    android.graphics.Color.TRANSPARENT
                }
                strokePaint.style = android.graphics.Paint.Style.STROKE
                strokePaint.strokeWidth = stroke
                strokePaint.strokeJoin = Paint.Join.ROUND
                strokePaint.strokeCap = Paint.Cap.ROUND
                canvas.drawPath(path, strokePaint)
            }

            drawWidgetForeground(context, canvas, config, width, height, scale, inset)
            return bitmap
        }

        private fun drawWidgetForeground(
            context: Context,
            canvas: android.graphics.Canvas,
            config: FundGoalConfig,
            width: Int,
            height: Int,
            scale: Float,
            inset: Float
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

            val padL = config.paddingLeft * scale + inset
            val padT = config.paddingTop * scale + inset
            val padR = config.paddingRight * scale + inset
            val padB = config.paddingBottom * scale + inset
            val left = padL
            val right = width - padR
            val top = padT
            val bottom = height - padB
            if (right - left < 24f || bottom - top < 24f) return

            val titlePaint = textPaint(nameColor, 14f * scale, 700, config.nameFontFamily)
            val percentPaint = textPaint(percentColor, 14f * scale, 900)
            val amountPaint = textPaint(currentColor, 22f * scale, 900)
            val targetPaint = textPaint(targetColor, 12f * scale, 500)
            val remainingPaint = textPaint(remainingColor, 12f * scale, 600)
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
            } else if (weight >= 600) {
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
