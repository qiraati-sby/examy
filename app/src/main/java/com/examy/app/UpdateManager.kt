package com.examy.app

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Manages in-app automatic update checking, APK downloading, and installation from GitHub Releases.
 */
object UpdateManager {

    private const val GITHUB_REPO = "qiraati-sby/examy"
    private const val RELEASES_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    data class UpdateInfo(
        val tagName: String,
        val versionName: String,
        val releaseName: String,
        val changelog: String,
        val apkDownloadUrl: String,
        val apkFileName: String,
        val apkSize: Long
    )

    fun getAppVersionName(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.2"
        } catch (_: Exception) {
            "1.0.2"
        }
    }

    /**
     * Checks GitHub Releases for a newer version of Examy.
     */
    fun checkForUpdate(activity: Activity, isManualCheck: Boolean = false) {
        if (activity.isFinishing || activity.isDestroyed) return

        if (isManualCheck) {
            Toast.makeText(activity, "Memeriksa pembaruan rilis...", Toast.LENGTH_SHORT).show()
        }

        thread {
            try {
                val currentVersionName = getAppVersionName(activity)
                val conn = openConnectionWithRedirects(RELEASES_API_URL)
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.setRequestProperty("User-Agent", "Examy-Android-App")

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseStr)
                    val tagName = json.optString("tag_name", "").trim()
                    val releaseName = json.optString("name", "Examy Update").trim()
                    val body = json.optString("body", "").trim()

                    // Parse APK asset
                    val assets = json.optJSONArray("assets")
                    var downloadUrl = ""
                    var apkName = "Examy-v3.apk"
                    var apkSize = 0L

                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                val url = asset.optString("browser_download_url", "")
                                val size = asset.optLong("size", 0L)
                                // Prioritize Examy-v3 or primary Examy apk
                                if (name.contains("v3", ignoreCase = true) || name == "Examy.apk" || downloadUrl.isBlank()) {
                                    downloadUrl = url
                                    apkName = name
                                    apkSize = size
                                }
                            }
                        }
                    }

                    val remoteVersion = tagName.removePrefix("v").trim()
                    val localVersion = currentVersionName.removePrefix("v").trim()
                    val isNewer = isVersionNewer(remoteVersion, localVersion)

                    Handler(Looper.getMainLooper()).post {
                        if (!activity.isFinishing && !activity.isDestroyed) {
                            if (isNewer && downloadUrl.isNotBlank()) {
                                val info = UpdateInfo(
                                    tagName = tagName,
                                    versionName = remoteVersion,
                                    releaseName = releaseName,
                                    changelog = body,
                                    apkDownloadUrl = downloadUrl,
                                    apkFileName = apkName,
                                    apkSize = apkSize
                                )
                                showUpdatePromptDialog(activity, info)
                            } else if (isManualCheck) {
                                Toast.makeText(
                                    activity,
                                    "Aplikasi sudah menggunakan versi terbaru ($currentVersionName)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        if (isManualCheck && !activity.isFinishing && !activity.isDestroyed) {
                            Toast.makeText(activity, "Gagal memeriksa pembaruan (Server: ${conn.responseCode})", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (_: Exception) {
                Handler(Looper.getMainLooper()).post {
                    if (isManualCheck && !activity.isFinishing && !activity.isDestroyed) {
                        Toast.makeText(activity, "Tidak dapat terhubung ke server pembaruan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Compares semantic versions: returns true if remoteVer > localVer.
     */
    fun isVersionNewer(remoteVer: String, localVer: String): Boolean {
        val rParts = remoteVer.split(".").mapNotNull { it.toIntOrNull() }
        val lParts = localVer.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(rParts.size, lParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val l = lParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }

    private fun showUpdatePromptDialog(activity: Activity, info: UpdateInfo) {
        val sizeMb = if (info.apkSize > 0) String.format("%.2f MB", info.apkSize.toFloat() / (1024 * 1024)) else "2.3 MB"

        AlertDialog.Builder(activity)
            .setTitle("Pembaruan Aplikasi Tersedia!")
            .setMessage("Versi terbaru ${info.tagName} telah dirilis.\n\nUkuran Berkas: $sizeMb\n\nApakah Anda ingin mengunduh dan memperbarui Examy sekarang?")
            .setPositiveButton("Perbarui Sekarang") { _, _ ->
                downloadAndInstall(activity, info)
            }
            .setNegativeButton("Nanti Saja", null)
            .show()
    }

    /**
     * Downloads APK from GitHub Releases and triggers system package installer.
     */
    fun downloadAndInstall(activity: Activity, info: UpdateInfo) {
        val progressDialog = ProgressDialog(activity).apply {
            setTitle("Mengunduh Pembaruan Examy")
            setMessage("Sedang mengunduh berkas ${info.apkFileName}...")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            isIndeterminate = false
            max = 100
            progress = 0
            setCancelable(false)
            show()
        }

        thread {
            try {
                val conn = openConnectionWithRedirects(info.apkDownloadUrl)
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.setRequestProperty("User-Agent", "Examy-Android-App")

                val totalSize = conn.contentLength.takeIf { it > 0 } ?: info.apkSize.toInt()

                // Save to app external cache directory (world-readable via FileProvider)
                val targetDir = activity.getExternalFilesDir(null) ?: activity.cacheDir
                val apkFile = File(targetDir, info.apkFileName)
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                val input = conn.inputStream
                val output = FileOutputStream(apkFile)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (totalSize > 0) {
                        val progressPct = ((totalBytesRead * 100) / totalSize).toInt().coerceIn(0, 100)
                        Handler(Looper.getMainLooper()).post {
                            progressDialog.progress = progressPct
                        }
                    }
                }

                output.flush()
                output.close()
                input.close()

                Handler(Looper.getMainLooper()).post {
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        progressDialog.dismiss()
                        installApk(activity, apkFile)
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        progressDialog.dismiss()
                        Toast.makeText(activity, "Gagal mengunduh pembaruan: ${e.localizedMessage ?: "Koneksi terputus"}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /**
     * Launches Android Package Installer using FileProvider.
     */
    fun installApk(activity: Activity, apkFile: File) {
        if (!apkFile.exists() || apkFile.length() <= 0) {
            Toast.makeText(activity, "Berkas APK pembaruan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // On Android 8.0+ (Oreo), verify unknown sources permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activity.packageManager.canRequestPackageInstalls()) {
                AlertDialog.Builder(activity)
                    .setTitle("Izin Instalasi Diperlukan")
                    .setMessage("Untuk memperbarui aplikasi Examy secara otomatis, izinkan instalasi dari aplikasi ini pada menu Pengaturan.")
                    .setPositiveButton("Buka Pengaturan") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        }
                        activity.startActivity(intent)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
                return
            }
        }

        try {
            val authority = "${activity.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(activity, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(activity, "Gagal membuka installer APK: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Follows HTTP/HTTPS redirects (e.g. GitHub 302 to AWS S3 CDN).
     */
    private fun openConnectionWithRedirects(initialUrl: String): HttpURLConnection {
        var currentUrl = initialUrl
        var redirects = 0
        while (redirects < 5) {
            val url = URL(currentUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.instanceFollowRedirects = false
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("User-Agent", "Examy-Android-App")

            val status = conn.responseCode
            if (status in 300..399) {
                val newUrl = conn.getHeaderField("Location") ?: break
                conn.disconnect()
                currentUrl = newUrl
                redirects++
            } else {
                return conn
            }
        }
        val finalUrl = URL(currentUrl)
        val finalConn = finalUrl.openConnection() as HttpURLConnection
        finalConn.connectTimeout = 10000
        finalConn.readTimeout = 10000
        finalConn.setRequestProperty("User-Agent", "Examy-Android-App")
        return finalConn
    }
}
