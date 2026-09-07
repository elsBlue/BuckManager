package com.buckmanager.app.utils

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

class DriveServiceHelper(private val driveService: Drive) {

    suspend fun uploadBackup(zipFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            // Delete existing backup first (if any)
            val result = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='backup.zip'")
                .execute()
            val existingFiles = result.files
            if (existingFiles != null && existingFiles.isNotEmpty()) {
                for (file in existingFiles) {
                    driveService.files().delete(file.id).execute()
                }
            }

            // Create new backup file
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = "backup.zip"
                parents = listOf("appDataFolder")
            }
            val mediaContent = FileContent("application/zip", zipFile)

            driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            true
        } catch (e: UserRecoverableAuthIOException) {
            throw e // Rethrow to handle in ViewModel
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun downloadBackup(destinationFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='backup.zip'")
                .setFields("nextPageToken, files(id, name)")
                .execute()

            val files = result.files
            if (files == null || files.isEmpty()) {
                return@withContext false
            }

            val fileId = files.first().id
            FileOutputStream(destinationFile).use { fos ->
                driveService.files().get(fileId).executeMediaAndDownloadTo(fos)
            }
            true
        } catch (e: UserRecoverableAuthIOException) {
            throw e // Rethrow to handle in ViewModel
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        fun getDriveService(context: Context, accountName: String): DriveServiceHelper {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
            )
            credential.selectedAccountName = accountName
            
            // Validate that the account is available on the device
            if (credential.selectedAccount == null) {
                throw IllegalStateException(
                    "Google account '$accountName' not found on this device. Please sign out and sign in again."
                )
            }
            
            val googleDriveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("Buck Manager")
                .build()
                
            return DriveServiceHelper(googleDriveService)
        }
    }
}
