package com.buckmanager.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
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

        private fun generateWidgetBackground(context: Context, config: FundGoalConfig): android.graphics.Bitmap {
            val width = 600
            val height = 300
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            val density = 2.5f
            val radiusTopLeft = config.radiusTopLeft * density
            val radiusTopRight = config.radiusTopRight * density
            val radiusBottomRight = config.radiusBottomRight * density
            val radiusBottomLeft = config.radiusBottomLeft * density

            val path = android.graphics.Path()
            val radii = floatArrayOf(
                radiusTopLeft, radiusTopLeft,
                radiusTopRight, radiusTopRight,
                radiusBottomRight, radiusBottomRight,
                radiusBottomLeft, radiusBottomLeft
            )
            val rect = android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat())
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

            val maxStroke = maxOf(config.borderTop, config.borderBottom, config.borderLeft, config.borderRight) * density
            if (maxStroke > 0) {
                val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                strokePaint.color = try {
                    android.graphics.Color.parseColor(config.borderColorHex)
                } catch (e: Exception) {
                    android.graphics.Color.TRANSPARENT
                }
                strokePaint.style = android.graphics.Paint.Style.STROKE
                strokePaint.strokeWidth = maxStroke
                canvas.drawPath(path, strokePaint)
            }

            return bitmap
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                com.buckmanager.app.model.CurrencyConfig.load(context)
                val fundGoal = getGoalFromPrefs(context)
                val views = RemoteViews(context.packageName, R.layout.widget_goal_layout)

                val name = fundGoal.name.ifBlank { "Target Savings" }
                views.setTextViewText(R.id.widget_title, name)
                views.setTextViewText(R.id.widget_current_amount, formatRp(fundGoal.currentAmount))
                views.setTextViewText(R.id.widget_target_amount, "Target: ${formatRp(fundGoal.targetAmount)}")

                fun parseOr(hex: String, fallback: Int): Int = try {
                    android.graphics.Color.parseColor(hex)
                } catch (_: Exception) { fallback }

                val labelColor = parseOr(fundGoal.labelColorHex, android.graphics.Color.parseColor("#D4A54A"))
                val iconColor = parseOr(fundGoal.iconColorHex, labelColor)
                val nameColor = parseOr(fundGoal.nameColorHex, parseOr(fundGoal.valueColorHex, android.graphics.Color.WHITE))
                val percentColor = parseOr(fundGoal.percentColorHex, nameColor)
                val currentColor = parseOr(fundGoal.currentSavedColorHex, nameColor)
                val targetColor = parseOr(fundGoal.targetAmountColorHex, labelColor)
                val remainingColor = parseOr(fundGoal.remainingColorHex, targetColor)
                val fillColor = parseOr(fundGoal.progressFillColorHex, labelColor)
                val trackColor = parseOr(fundGoal.progressTrackColorHex, android.graphics.Color.parseColor("#40808080"))

                views.setTextColor(R.id.widget_title, nameColor)
                views.setTextColor(R.id.widget_current_amount, currentColor)
                views.setTextColor(R.id.widget_percentage, percentColor)
                views.setTextColor(R.id.widget_target_amount, targetColor)
                views.setTextColor(R.id.widget_remaining, remainingColor)
                views.setImageViewResource(R.id.widget_title_icon, goalIconRes(fundGoal.iconName))
                views.setInt(R.id.widget_title_icon, "setColorFilter", iconColor)

                val progressRatio = if (fundGoal.targetAmount > 0) {
                    (fundGoal.currentAmount / fundGoal.targetAmount).coerceIn(0.0, 1.0)
                } else 0.0
                val percentageInt = (progressRatio * 100).toInt()
                val remainingAmount = (fundGoal.targetAmount - fundGoal.currentAmount).coerceAtLeast(0.0)
                views.setTextViewText(R.id.widget_percentage, "${percentageInt}%")
                views.setTextViewText(
                    R.id.widget_remaining,
                    if (remainingAmount <= 0) "Goal reached!" else "Remaining: ${formatRp(remainingAmount)}"
                )
                views.setImageViewBitmap(R.id.widget_progress_image, progressBitmap(fillColor, trackColor, progressRatio.toFloat()))

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val density = context.resources.displayMetrics.density
                    val pt = (fundGoal.paddingTop * density).toInt()
                    val pr = (fundGoal.paddingRight * density).toInt()
                    val pb = (fundGoal.paddingBottom * density).toInt()
                    val pl = (fundGoal.paddingLeft * density).toInt()
                    views.setViewPadding(R.id.widget_content_container, pl, pt, pr, pb)
                }

                try {
                    val bitmap = generateWidgetBackground(context, fundGoal)
                    views.setImageViewBitmap(R.id.widget_bg_image, bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

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

                appWidgetManager.updateAppWidget(appWidgetId, views)
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

        private fun progressBitmap(fill: Int, track: Int, ratio: Float): android.graphics.Bitmap {
            val width = 600
            val height = 20
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            val rect = android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat())
            paint.color = track
            canvas.drawRoundRect(rect, 10f, 10f, paint)
            if (ratio > 0f) {
                paint.color = fill
                canvas.drawRoundRect(
                    android.graphics.RectF(0f, 0f, width * ratio.coerceIn(0f, 1f), height.toFloat()),
                    10f, 10f, paint
                )
            }
            return bitmap
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}
