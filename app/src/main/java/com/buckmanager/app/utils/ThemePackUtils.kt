package com.buckmanager.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import com.buckmanager.app.model.Envelope
import com.buckmanager.app.model.FundGoalConfig
import com.buckmanager.app.model.SharedCustomizationPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ThemePackUtils {
    const val FILE_EXTENSION = "bucktheme"
    const val MANIFEST_NAME = "theme.json"
    const val PREVIEW_NAME = "preview.jpg"

    private const val MAX_IMAGE_EDGE = 1600
    private const val PREVIEW_EDGE = 720
    private const val JPEG_QUALITY = 72
    private const val MAX_ENTRY_BYTES = 8L * 1024 * 1024
    private const val MAX_UNCOMPRESSED_BYTES = 25L * 1024 * 1024

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun fileProviderAuthority(context: Context): String = "${context.packageName}.fileprovider"

    fun sanitizeFileStem(name: String): String {
        val cleaned = name.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_')
        return cleaned.take(40).ifBlank { "BuckLook" }
    }

    fun resolveZipEntry(destDir: File, entryName: String): File? {
        if (entryName.isBlank() || entryName.startsWith("/") || entryName.contains("\\")) return null
        val target = File(destDir, entryName).canonicalFile
        val root = destDir.canonicalFile
        val prefix = root.path + File.separator
        return if (target.path == root.path || target.path.startsWith(prefix)) target else null
    }

    fun createPackFile(context: Context, payload: SharedCustomizationPayload): File {
        val dir = File(context.cacheDir, "theme_packs").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val outFile = File(dir, "${sanitizeFileStem(payload.themeName)}.$FILE_EXTENSION")

        val packedUris = mutableMapOf<String, String>()
        val imageBytes = mutableMapOf<String, ByteArray>()

        collectImageSlots(payload).forEach { (key, uri) ->
            if (uri.isNullOrBlank()) return@forEach
            if (uri.startsWith("http://") || uri.startsWith("https://")) {
                packedUris[key] = uri
                return@forEach
            }
            val jpeg = compressUriToJpeg(context, uri, MAX_IMAGE_EDGE, JPEG_QUALITY) ?: return@forEach
            val packPath = "images/$key.jpg"
            imageBytes[packPath] = jpeg
            packedUris[key] = packPath
        }

        val packedPayload = rewriteImageSlots(payload, packedUris)
        val manifest = json.encodeToString(packedPayload).toByteArray(Charsets.UTF_8)
        val preview = imageBytes["images/wallpaper.jpg"]
            ?: makeColorPreview(payload.globalBackground.backgroundColorHex, payload.themeName)

        ZipOutputStream(FileOutputStream(outFile)).use { zip ->
            zip.setLevel(6)
            putZipBytes(zip, MANIFEST_NAME, manifest)
            putZipBytes(zip, PREVIEW_NAME, preview)
            imageBytes.forEach { (path, bytes) -> putZipBytes(zip, path, bytes) }
        }
        return outFile
    }

    fun importPack(context: Context, uri: Uri): SharedCustomizationPayload {
        val cr = context.contentResolver
        val peek = cr.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Couldn't read that file.")

        if (peek.isNotEmpty() && peek[0] == '{'.code.toByte()) {
            val payload = json.decodeFromString<SharedCustomizationPayload>(String(peek, Charsets.UTF_8))
            return persistRemoteImages(context, payload)
        }

        val unpackDir = File(context.cacheDir, "theme_import").apply {
            deleteRecycled()
            mkdirs()
        }

        var uncompressed = 0L
        ZipInputStream(peek.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val dest = resolveZipEntry(unpackDir, entry.name)
                    ?: throw IllegalArgumentException("Look file is damaged.")
                if (entry.isDirectory) {
                    dest.mkdirs()
                    continue
                }
                dest.parentFile?.mkdirs()
                val bytes = zip.readBytes()
                if (bytes.size > MAX_ENTRY_BYTES) throw IllegalArgumentException("A photo in this look is too large.")
                uncompressed += bytes.size
                if (uncompressed > MAX_UNCOMPRESSED_BYTES) throw IllegalArgumentException("This look file is too large.")
                dest.writeBytes(bytes)
            }
        }

        val manifestFile = File(unpackDir, MANIFEST_NAME).takeIf { it.exists() }
            ?: unpackDir.walkTopDown().firstOrNull { it.name == MANIFEST_NAME }
            ?: throw IllegalArgumentException("This isn't a Buck look file.")
        val payload = json.decodeFromString<SharedCustomizationPayload>(manifestFile.readText())
        return persistPackImages(context, payload, unpackDir)
    }

    fun mergeEnvelopeLook(current: Envelope, packed: Envelope): Envelope = current.copy(
        colorHex = packed.colorHex,
        iconName = packed.iconName,
        backgroundColorHex = packed.backgroundColorHex,
        backgroundImageUri = packed.backgroundImageUri,
        dimOpacity = packed.dimOpacity,
        labelColorHex = packed.labelColorHex,
        valueColorHex = packed.valueColorHex,
        descriptionColorHex = packed.descriptionColorHex,
        radiusTopLeft = packed.radiusTopLeft,
        radiusTopRight = packed.radiusTopRight,
        radiusBottomRight = packed.radiusBottomRight,
        radiusBottomLeft = packed.radiusBottomLeft,
        borderTop = packed.borderTop,
        borderRight = packed.borderRight,
        borderBottom = packed.borderBottom,
        borderLeft = packed.borderLeft,
        borderColorHex = packed.borderColorHex,
        paddingTop = packed.paddingTop,
        paddingRight = packed.paddingRight,
        paddingBottom = packed.paddingBottom,
        paddingLeft = packed.paddingLeft,
        useGradient = packed.useGradient,
        gradientColors = packed.gradientColors,
        gradientAngle = packed.gradientAngle,
        elevation = packed.elevation
    )

    fun mergeFundGoalLook(current: FundGoalConfig, packed: FundGoalConfig): FundGoalConfig = current.copy(
        backgroundColorHex = packed.backgroundColorHex,
        backgroundImageUri = packed.backgroundImageUri,
        dimOpacity = packed.dimOpacity,
        radiusTopLeft = packed.radiusTopLeft,
        radiusTopRight = packed.radiusTopRight,
        radiusBottomRight = packed.radiusBottomRight,
        radiusBottomLeft = packed.radiusBottomLeft,
        labelColorHex = packed.labelColorHex,
        valueColorHex = packed.valueColorHex,
        borderTop = packed.borderTop,
        borderRight = packed.borderRight,
        borderBottom = packed.borderBottom,
        borderLeft = packed.borderLeft,
        borderColorHex = packed.borderColorHex,
        paddingTop = packed.paddingTop,
        paddingRight = packed.paddingRight,
        paddingBottom = packed.paddingBottom,
        paddingLeft = packed.paddingLeft,
        useGradient = packed.useGradient,
        gradientColors = packed.gradientColors,
        gradientAngle = packed.gradientAngle,
        btnBgColorHex = packed.btnBgColorHex,
        btnTextColorHex = packed.btnTextColorHex,
        elevation = packed.elevation
    )

    fun stripPrivateAmounts(payload: SharedCustomizationPayload): SharedCustomizationPayload =
        payload.copy(
            fundGoal = payload.fundGoal.copy(name = "Goal", targetAmount = 0.0, currentAmount = 0.0)
        )

    private fun persistPackImages(
        context: Context,
        payload: SharedCustomizationPayload,
        unpackDir: File
    ): SharedCustomizationPayload {
        fun resolve(uri: String?): String? {
            if (uri.isNullOrBlank()) return null
            if (uri.startsWith("http://") || uri.startsWith("https://")) return uri
            val relative = uri.removePrefix("pack://")
            val file = resolveZipEntry(unpackDir, relative) ?: return null
            if (!file.exists()) return null
            return persistBackgroundImage(context, Uri.fromFile(file).toString())
        }
        return mapImageUris(payload, ::resolve)
    }

    private fun persistRemoteImages(context: Context, payload: SharedCustomizationPayload): SharedCustomizationPayload {
        fun resolve(uri: String?): String? {
            if (uri.isNullOrBlank()) return null
            if (uri.startsWith("http://") || uri.startsWith("https://")) return uri
            return persistBackgroundImage(context, uri)
        }
        return mapImageUris(payload, ::resolve)
    }

    private fun collectImageSlots(payload: SharedCustomizationPayload): List<Pair<String, String?>> = buildList {
        add("wallpaper" to payload.globalBackground.backgroundImageUri)
        add("header_net" to payload.headerCardsConfig.netWorth.backgroundImageUri)
        add("header_income" to payload.headerCardsConfig.income.backgroundImageUri)
        add("header_expense" to payload.headerCardsConfig.expense.backgroundImageUri)
        add("goal" to payload.fundGoal.backgroundImageUri)
        payload.envelopes.forEach { env ->
            val key = "env_" + env.id.replace(Regex("[^A-Za-z0-9_-]"), "_")
            add(key to env.backgroundImageUri)
        }
    }

    private fun rewriteImageSlots(
        payload: SharedCustomizationPayload,
        packedByKey: Map<String, String>
    ): SharedCustomizationPayload {
        val originalToPacked = collectImageSlots(payload).mapNotNull { (key, uri) ->
            val packed = packedByKey[key] ?: return@mapNotNull null
            uri to packed
        }.toMap()
        return mapImageUris(payload) { uri ->
            if (uri.isNullOrBlank()) null
            else originalToPacked[uri] ?: uri
        }
    }

    private fun mapImageUris(
        payload: SharedCustomizationPayload,
        transform: (String?) -> String?
    ): SharedCustomizationPayload = payload.copy(
        globalBackground = payload.globalBackground.copy(
            backgroundImageUri = transform(payload.globalBackground.backgroundImageUri)
        ),
        headerCardsConfig = payload.headerCardsConfig.copy(
            netWorth = payload.headerCardsConfig.netWorth.copy(
                backgroundImageUri = transform(payload.headerCardsConfig.netWorth.backgroundImageUri)
            ),
            income = payload.headerCardsConfig.income.copy(
                backgroundImageUri = transform(payload.headerCardsConfig.income.backgroundImageUri)
            ),
            expense = payload.headerCardsConfig.expense.copy(
                backgroundImageUri = transform(payload.headerCardsConfig.expense.backgroundImageUri)
            )
        ),
        fundGoal = payload.fundGoal.copy(
            backgroundImageUri = transform(payload.fundGoal.backgroundImageUri)
        ),
        envelopes = payload.envelopes.map { env ->
            env.copy(backgroundImageUri = transform(env.backgroundImageUri))
        }
    )

    private fun compressUriToJpeg(context: Context, uri: String, maxEdge: Int, quality: Int): ByteArray? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openUriInputStream(context, uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val sample = sampleSize(bounds.outWidth, bounds.outHeight, maxEdge)
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = openUriInputStream(context, uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: return null
            val scaled = scaleToMaxEdge(bitmap, maxEdge)
            val out = java.io.ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            if (scaled !== bitmap) scaled.recycle()
            bitmap.recycle()
            out.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun scaleToMaxEdge(bitmap: Bitmap, maxEdge: Int): Bitmap {
        val edge = maxOf(bitmap.width, bitmap.height)
        if (edge <= maxEdge) return bitmap
        val scale = maxEdge.toFloat() / edge
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun sampleSize(width: Int, height: Int, maxEdge: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= maxEdge && h / 2 >= maxEdge) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private fun makeColorPreview(colorHex: String, title: String): ByteArray {
        val bitmap = Bitmap.createBitmap(PREVIEW_EDGE, (PREVIEW_EDGE * 1.6f).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val color = try {
            Color.parseColor(if (colorHex.startsWith("#")) colorHex else "#$colorHex")
        } catch (_: Exception) {
            Color.parseColor("#0F1117")
        }
        canvas.drawColor(color)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = 42f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title.take(28), bitmap.width / 2f, bitmap.height / 2f, paint)
        val out = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        bitmap.recycle()
        return out.toByteArray()
    }

    private fun putZipBytes(zip: ZipOutputStream, name: String, bytes: ByteArray) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(bytes)
        zip.closeEntry()
    }

    private fun File.deleteRecycled() {
        if (exists()) deleteRecursively()
    }
}

private fun ByteArray.inputStream() = java.io.ByteArrayInputStream(this)
