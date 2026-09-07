package com.buckmanager.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

private const val BACKGROUNDS_DIR = "backgrounds"

fun backgroundsDir(context: Context): File {
    val dir = File(context.filesDir, BACKGROUNDS_DIR)
    if (!dir.exists()) dir.mkdirs()
    return dir
}

fun openUriInputStream(context: Context, uriString: String): InputStream? {
    return try {
        if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
            java.net.URL(uriString).openStream()
        } else {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val path = uri.path ?: return null
                FileInputStream(File(path))
            } else {
                context.contentResolver.openInputStream(uri)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Copy any picker/content/cache URI into durable app storage under filesDir/backgrounds.
 * HTTP(S) preset URLs are left unchanged (loaded remotely).
 * Already-persisted files under backgrounds/ are returned as-is.
 */
fun persistBackgroundImage(context: Context, sourceUri: String?): String? {
    if (sourceUri.isNullOrBlank()) return null
    if (sourceUri.startsWith("http://") || sourceUri.startsWith("https://")) {
        return sourceUri
    }

    try {
        val uri = Uri.parse(sourceUri)
        if (uri.scheme == "file") {
            val path = uri.path
            if (path != null) {
                val file = File(path)
                val backgrounds = backgroundsDir(context)
                if (file.exists() && file.absolutePath.startsWith(backgrounds.absolutePath)) {
                    return Uri.fromFile(file).toString()
                }
            }
        }

        val input = openUriInputStream(context, sourceUri) ?: return null
        val dest = File(backgroundsDir(context), "bg_${UUID.randomUUID()}.jpg")
        FileOutputStream(dest).use { out ->
            input.copyTo(out)
            out.flush()
        }
        input.close()
        return Uri.fromFile(dest).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun cropImageBitmap(
    context: Context,
    imageUri: String,
    containerWidth: Float,
    containerHeight: Float,
    rectOffsetX: Float,
    rectOffsetY: Float,
    rectWidth: Float,
    rectHeight: Float,
    rotation: Float
): String? {
    try {
        val inputStream = openUriInputStream(context, imageUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return null

        // Get Exif rotation
        var exifRotation = 0f
        val exifInputStream = openUriInputStream(context, imageUri)
        if (exifInputStream != null) {
            val exif = ExifInterface(exifInputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            exifRotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            exifInputStream.close()
        }

        val matrix = Matrix()
        matrix.postRotate(exifRotation + rotation)

        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
        )

        val imageAspect = rotatedBitmap.width.toFloat() / rotatedBitmap.height.toFloat()
        val containerAspect = containerWidth / containerHeight

        var drawWidth = containerWidth
        var drawHeight = containerHeight
        var drawX = 0f
        var drawY = 0f

        if (imageAspect > containerAspect) {
            drawHeight = containerWidth / imageAspect
            drawY = (containerHeight - drawHeight) / 2f
        } else {
            drawWidth = containerHeight * imageAspect
            drawX = (containerWidth - drawWidth) / 2f
        }

        val cropX = ((rectOffsetX - drawX) / drawWidth) * rotatedBitmap.width
        val cropY = ((rectOffsetY - drawY) / drawHeight) * rotatedBitmap.height
        val cropW = (rectWidth / drawWidth) * rotatedBitmap.width
        val cropH = (rectHeight / drawHeight) * rotatedBitmap.height

        val finalX = cropX.toInt().coerceIn(0, rotatedBitmap.width - 1)
        val finalY = cropY.toInt().coerceIn(0, rotatedBitmap.height - 1)
        val finalW = cropW.toInt().coerceIn(1, rotatedBitmap.width - finalX)
        val finalH = cropH.toInt().coerceIn(1, rotatedBitmap.height - finalY)

        val finalBitmap = Bitmap.createBitmap(rotatedBitmap, finalX, finalY, finalW, finalH)

        val file = File(backgroundsDir(context), "bg_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
        }

        return Uri.fromFile(file).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
