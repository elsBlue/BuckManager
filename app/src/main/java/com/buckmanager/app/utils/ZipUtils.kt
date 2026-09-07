package com.buckmanager.app.utils

import android.content.Context
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {

    fun createBackupZip(context: Context, jsonPayload: String, backgroundImageName: String = "global_bg.jpg"): File {
        val zipFile = File(context.cacheDir, "backup.zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // 1. Write config.json
            val jsonEntry = ZipEntry("config.json")
            zos.putNextEntry(jsonEntry)
            zos.write(jsonPayload.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 2. Write background image if it exists
            val bgFile = File(context.filesDir, backgroundImageName)
            if (bgFile.exists()) {
                val imgEntry = ZipEntry(backgroundImageName)
                zos.putNextEntry(imgEntry)
                FileInputStream(bgFile).use { fis ->
                    fis.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
        return zipFile
    }

    fun extractBackupZip(context: Context, zipFile: File, backgroundImageName: String = "global_bg.jpg"): String? {
        var jsonPayload: String? = null
        
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when (entry.name) {
                    "config.json" -> {
                        val outputStream = ByteArrayOutputStream()
                        zis.copyTo(outputStream)
                        jsonPayload = outputStream.toString(Charsets.UTF_8.name())
                    }
                    backgroundImageName -> {
                        // Extract and overwrite background image
                        val bgFile = File(context.filesDir, backgroundImageName)
                        FileOutputStream(bgFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return jsonPayload
    }
}
