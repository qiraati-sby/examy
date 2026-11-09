package com.examy.app

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object SecurityManager {

    private const val PREFS_NAME = "examy_secure_prefs"
    private const val KEY_PROCTOR_PIN = "proctor_pin"
    private const val KEY_RECENT_URLS = "recent_urls"
    const val DEFAULT_PIN = "1234"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getProctorPin(context: Context): String {
        return getPrefs(context).getString(KEY_PROCTOR_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setProctorPin(context: Context, newPin: String) {
        getPrefs(context).edit().putString(KEY_PROCTOR_PIN, newPin).apply()
    }

    fun verifyPin(context: Context, inputPin: String): Boolean {
        val currentPin = getProctorPin(context)
        return inputPin == currentPin
    }

    fun getRecentUrls(context: Context): List<String> {
        val raw = getPrefs(context).getString(KEY_RECENT_URLS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("|").filter { it.isNotBlank() }
    }

    fun addRecentUrl(context: Context, url: String) {
        val current = getRecentUrls(context).toMutableList()
        current.remove(url)
        current.add(0, url)
        val trimmed = current.take(5).joinToString("|")
        getPrefs(context).edit().putString(KEY_RECENT_URLS, trimmed).apply()
    }

    fun clearRecentUrls(context: Context) {
        getPrefs(context).edit().remove(KEY_RECENT_URLS).apply()
    }

    /**
     * Enables full screen lock, FLAG_SECURE, and immersive mode.
     */
    fun enableKioskSecurity(activity: Activity) {
        // 1. Prevent screenshots and screen recording
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // 2. Prevent screen timeout / sleeping
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 3. Immersive sticky fullscreen mode
        applyImmersiveFullscreen(activity)

        // 4. Android Kiosk / Lock Task Mode (Pinning)
        try {
            activity.startLockTask()
        } catch (_: Exception) {
            // Degrades gracefully on devices where screen pinning is disabled by policy
        }
    }

    /**
     * Disables kiosk lock task mode cleanly.
     */
    fun disableKioskSecurity(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (_: Exception) {
            // Safely ignore if lock task was not running
        }
    }

    /**
     * Hides system bars and navigation for a distraction-free locked UI.
     */
    fun applyImmersiveFullscreen(activity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
