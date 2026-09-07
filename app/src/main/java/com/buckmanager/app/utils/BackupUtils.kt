package com.buckmanager.app.utils

import android.content.Context
import android.net.Uri
import com.buckmanager.app.data.AppDatabase
import com.buckmanager.app.data.SettingEntity
import com.buckmanager.app.data.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class BackupData(
    val transactions: List<TransactionEntity>,
    val settings: List<SettingEntity>
)

object BackupUtils {

    suspend fun createLocalSnapshot(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbName = "buckmanager.db"
            
            // Checkpoint WAL before backup to ensure all data is flushed
            try {
                AppDatabase.getInstance(context).query("PRAGMA wal_checkpoint(FULL)", null)?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val dbFile = context.getDatabasePath(dbName)
            val walFile = context.getDatabasePath("$dbName-wal")
            val shmFile = context.getDatabasePath("$dbName-shm")

            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            
            // Clean up old backups (keep only last 5)
            val existingBackups = backupDir.listFiles()?.filter { it.isDirectory }?.sortedBy { it.name }
            if (existingBackups != null && existingBackups.size >= 5) {
                existingBackups.first().deleteRecursively()
            }

            val currentBackupDir = File(backupDir, "backup_$timestamp")
            currentBackupDir.mkdirs()

            if (dbFile.exists()) copyFile(dbFile, File(currentBackupDir, dbName))
            if (walFile.exists()) copyFile(walFile, File(currentBackupDir, "$dbName-wal"))
            if (shmFile.exists()) copyFile(shmFile, File(currentBackupDir, "$dbName-shm"))

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportToJson(context: Context, db: AppDatabase, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val transactions = db.transactionDao().getAllTransactions()
            val settings = db.settingDao().getAllSettings()

            val backupData = BackupData(transactions, settings)
            val jsonString = Json { prettyPrint = true }.encodeToString(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun copyFile(src: File, dst: File) {
        FileInputStream(src).use { inStream ->
            FileOutputStream(dst).use { outStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inStream.read(buffer).also { length = it } > 0) {
                    outStream.write(buffer, 0, length)
                }
            }
        }
    }
}
