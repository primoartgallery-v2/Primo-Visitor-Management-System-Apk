package com.example.primostaffscanner.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.primostaffscanner.BuildConfig
import com.example.primostaffscanner.data.model.UpdateInfo
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class OtaUpdateManager(private val context: Context) {

    companion object {
        // Default GitHub raw URL to version.json
        // User can change the repository path whenever needed
        const val DEFAULT_VERSION_URL =
            "https://raw.githubusercontent.com/primoartgallery-v2/Primo-Visitor-Management-System-Apk/main/version.json"

        const val DEFAULT_FALLBACK_APK_URL =
            "https://github.com/primoartgallery-v2/Primo-Visitor-Management-System-Apk/releases/latest/download/PrimoStaffScanner.apk"
    }

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val currentVersionCode: Int = BuildConfig.VERSION_CODE
    val currentVersionName: String = BuildConfig.VERSION_NAME

    suspend fun checkForUpdate(
        customUrl: String? = null
    ): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val targetUrl = customUrl?.ifBlank { null } ?: DEFAULT_VERSION_URL
            val request = Request.Builder()
                .url(targetUrl)
                .header("Cache-Control", "no-cache")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Failed to fetch version info: HTTP ${response.code}")
                )
            }

            val body = response.body?.string().orEmpty()
            if (body.isBlank()) {
                return@withContext Result.success(null)
            }

            val updateInfo = gson.fromJson(body, UpdateInfo::class.java)

            if (updateInfo != null && updateInfo.versionCode > currentVersionCode) {
                Result.success(updateInfo)
            } else {
                Result.success(null) // Already up to date!
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadApk(
        apkUrl: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val finalUrl = apkUrl.ifBlank { DEFAULT_FALLBACK_APK_URL }
            val request = Request.Builder()
                .url(finalUrl)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Failed to download APK: HTTP ${response.code}")
                )
            }

            val responseBody = response.body
                ?: return@withContext Result.failure(Exception("Empty APK download response"))

            val totalBytes = responseBody.contentLength()
            val destinationFile = File(context.cacheDir, "PrimoStaffScanner_update.apk")

            responseBody.byteStream().use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                    outputStream.flush()
                }
            }

            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun installApk(apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists()) {
                return Result.failure(Exception("Downloaded APK file not found"))
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
