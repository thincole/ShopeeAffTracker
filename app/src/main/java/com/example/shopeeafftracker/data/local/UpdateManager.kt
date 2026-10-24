package com.example.shopeeafftracker.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val apkUrl: String
)

object UpdateManager {

    private const val UPDATE_JSON_URL =
        "https://raw.githubusercontent.com/thincole/ShopeeAffTracker/main/version.json"

    private const val CURRENT_VERSION_CODE = 2 // Current App VersionCode

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdates(): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(UPDATE_JSON_URL)
                .addHeader("User-Agent", "Mozilla/5.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string() ?: ""

            if (!response.isSuccessful || jsonStr.isBlank()) {
                return@withContext Result.success(null)
            }

            val root = JsonParser.parseString(jsonStr).asJsonObject
            val remoteCode = if (root.has("versionCode")) root.get("versionCode").asInt else 0
            val remoteName = if (root.has("versionName")) root.get("versionName").asString else ""
            val releaseNotes = if (root.has("releaseNotes")) root.get("releaseNotes").asString else ""
            val apkUrl = if (root.has("apkUrl")) root.get("apkUrl").asString else ""

            if (remoteCode > CURRENT_VERSION_CODE && apkUrl.isNotBlank()) {
                val updateInfo = AppUpdateInfo(
                    versionCode = remoteCode,
                    versionName = remoteName,
                    releaseNotes = releaseNotes,
                    apkUrl = apkUrl
                )
                Result.success(updateInfo)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAndInstallApk(context: Context, apkUrl: String, onProgress: (Int) -> Unit): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(apkUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body ?: return@withContext Result.failure(Exception("File APK rỗng"))

            val apkFile = File(context.externalCacheDir, "update.apk")
            if (apkFile.exists()) apkFile.delete()

            val contentLength = body.contentLength()
            val inputStream = body.byteStream()
            val outputStream = apkFile.outputStream()

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                if (contentLength > 0) {
                    val progress = ((totalRead * 100) / contentLength).toInt()
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                installApk(context, apkFile)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)
    }
}
