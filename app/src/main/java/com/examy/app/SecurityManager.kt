package com.examy.app

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.json.JSONArray

object SecurityManager {

    private const val PREFS_NAME = "examy_secure_prefs"
    private const val KEY_PROCTOR_PIN = "proctor_pin"
    private const val KEY_EXIT_PIN = "exit_pin"
    private const val KEY_ACTIVE_CONFIG = "active_exam_config"
    private const val KEY_RECENT_URLS = "recent_urls"
    private const val KEY_EXAM_HISTORY = "exam_history_json_list"
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

    fun getExitPin(context: Context): String {
        return getPrefs(context).getString(KEY_EXIT_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setExitPin(context: Context, newPin: String) {
        getPrefs(context).edit().putString(KEY_EXIT_PIN, newPin).apply()
    }

    fun saveActiveExamConfig(context: Context, config: ExamConfig) {
        getPrefs(context).edit().putString(KEY_ACTIVE_CONFIG, config.toJson()).apply()
        // Also update standard pins
        setProctorPin(context, config.proctorPin)
        setExitPin(context, config.exitPin)
        // Auto-save to history as well
        saveExamToHistory(context, config)
    }

    fun getActiveExamConfig(context: Context): ExamConfig? {
        val raw = getPrefs(context).getString(KEY_ACTIVE_CONFIG, null) ?: return null
        return ExamConfig.fromJson(raw)
    }

    // --- Exam History Management ---
    fun getExamHistory(context: Context): List<ExamConfig> {
        val raw = getPrefs(context).getString(KEY_EXAM_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<ExamConfig>()
            for (i in 0 until array.length()) {
                val jsonStr = array.getString(i)
                ExamConfig.fromJson(jsonStr)?.let { list.add(it) }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveExamToHistory(context: Context, config: ExamConfig) {
        val list = getExamHistory(context).toMutableList()
        // Deduplicate by token or subject+url
        list.removeAll { it.token == config.token || (it.subjectName == config.subjectName && it.examUrl == config.examUrl && it.startDate == config.startDate) }
        list.add(0, config)
        val trimmed = list.take(20)

        val array = JSONArray()
        for (item in trimmed) {
            array.put(item.toJson())
        }
        getPrefs(context).edit().putString(KEY_EXAM_HISTORY, array.toString()).apply()
    }

    fun deleteExamFromHistory(context: Context, token: String) {
        val list = getExamHistory(context).toMutableList()
        list.removeAll { it.token == token }
        val array = JSONArray()
        for (item in list) {
            array.put(item.toJson())
        }
        getPrefs(context).edit().putString(KEY_EXAM_HISTORY, array.toString()).apply()
    }

    fun clearExamHistory(context: Context) {
        getPrefs(context).edit().remove(KEY_EXAM_HISTORY).apply()
    }

    fun verifyPin(context: Context, inputPin: String): Boolean {
        val currentPin = getProctorPin(context)
        return inputPin == currentPin
    }

    fun verifyProctorPin(context: Context, inputPin: String, config: ExamConfig?): Boolean {
        val expected = config?.proctorPin?.takeIf { it.isNotBlank() } ?: getProctorPin(context)
        return inputPin == expected
    }

    fun verifyExitPin(context: Context, inputPin: String, config: ExamConfig?): Boolean {
        val expected = config?.exitPin?.takeIf { it.isNotBlank() } ?: getExitPin(context)
        return inputPin == expected
    }

    fun verifyStudentPin(inputPin: String, config: ExamConfig): Boolean {
        if (config.studentPin.isBlank()) return true
        return inputPin == config.studentPin
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
