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
    private const val KEY_APP_THEME = "app_theme_mode"

    // Fail-safe default PIN constants (matched with UI presets)
    const val DEFAULT_MASTER_PIN = "1234"
    const val DEFAULT_PROCTOR_PIN = "9999"
    const val DEFAULT_EXIT_PIN = "8888"
    const val DEFAULT_STUDENT_PIN = "123456"

    // Deprecated alias for backwards compatibility
    const val DEFAULT_PIN = DEFAULT_MASTER_PIN

    const val THEME_CLASSIC = "classic"
    const val THEME_SANTRI = "santri"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getProctorPin(context: Context): String {
        return getPrefs(context).getString(KEY_PROCTOR_PIN, DEFAULT_PROCTOR_PIN) ?: DEFAULT_PROCTOR_PIN
    }

    fun setProctorPin(context: Context, newPin: String) {
        val clean = newPin.trim()
        if (clean.isNotEmpty()) {
            getPrefs(context).edit().putString(KEY_PROCTOR_PIN, clean).apply()
        }
    }

    fun getExitPin(context: Context): String {
        return getPrefs(context).getString(KEY_EXIT_PIN, DEFAULT_EXIT_PIN) ?: DEFAULT_EXIT_PIN
    }

    fun setExitPin(context: Context, newPin: String) {
        val clean = newPin.trim()
        if (clean.isNotEmpty()) {
            getPrefs(context).edit().putString(KEY_EXIT_PIN, clean).apply()
        }
    }

    fun saveActiveExamConfig(context: Context, config: ExamConfig) {
        getPrefs(context).edit().putString(KEY_ACTIVE_CONFIG, config.toJson()).apply()
        // Save PINs to both dedicated keys so they can be read anywhere
        if (config.proctorPin.isNotBlank()) {
            setProctorPin(context, config.proctorPin)
        }
        if (config.exitPin.isNotBlank()) {
            setExitPin(context, config.exitPin)
        }
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
        val clean = inputPin.trim()
        return clean == currentPin || clean == DEFAULT_PROCTOR_PIN || clean == DEFAULT_MASTER_PIN
    }

    /**
     * Verifies Proctor PIN for unlocking alarms or emergency overrides.
     * Accepts:
     * 1. Configured proctorPin from active session
     * 2. Saved proctorPin from SharedPreferences
     * 3. Default proctorPin ("9999")
     * 4. Universal master emergency PIN ("1234")
     * 5. Exit PIN (if proctor enters the exit PIN during panic/siren)
     */
    fun verifyProctorPin(context: Context, inputPin: String, config: ExamConfig?): Boolean {
        val clean = inputPin.trim()
        if (clean.isEmpty()) return false

        val cfgProctor = config?.proctorPin?.trim()
        val savedProctor = getProctorPin(context).trim()
        val cfgExit = config?.exitPin?.trim()
        val savedExit = getExitPin(context).trim()

        return clean == cfgProctor ||
                clean == savedProctor ||
                clean == DEFAULT_PROCTOR_PIN ||
                clean == DEFAULT_MASTER_PIN ||
                clean == cfgExit ||
                clean == savedExit ||
                clean == DEFAULT_EXIT_PIN
    }

    /**
     * Verifies Exit PIN for normal exit by student/proctor.
     * Accepts:
     * 1. Configured exitPin from active session
     * 2. Saved exitPin from SharedPreferences
     * 3. Default exitPin ("8888")
     * 4. Configured proctorPin (Proctors can always exit on behalf of students)
     * 5. Saved proctorPin
     * 6. Default proctorPin ("9999")
     * 7. Universal master emergency PIN ("1234")
     */
    fun verifyExitPin(context: Context, inputPin: String, config: ExamConfig?): Boolean {
        val clean = inputPin.trim()
        if (clean.isEmpty()) return false

        val cfgExit = config?.exitPin?.trim()
        val savedExit = getExitPin(context).trim()
        val cfgProctor = config?.proctorPin?.trim()
        val savedProctor = getProctorPin(context).trim()

        return clean == cfgExit ||
                clean == savedExit ||
                clean == DEFAULT_EXIT_PIN ||
                clean == cfgProctor ||
                clean == savedProctor ||
                clean == DEFAULT_PROCTOR_PIN ||
                clean == DEFAULT_MASTER_PIN
    }

    /**
     * Verifies Student PIN before opening exam questions.
     */
    fun verifyStudentPin(inputPin: String, config: ExamConfig?): Boolean {
        if (config == null || config.studentPin.isBlank()) return true
        val clean = inputPin.trim()
        val expected = config.studentPin.trim()
        return clean == expected ||
                clean == DEFAULT_STUDENT_PIN ||
                clean == DEFAULT_MASTER_PIN
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

    fun getAppTheme(context: Context): String {
        return getPrefs(context).getString(KEY_APP_THEME, THEME_CLASSIC) ?: THEME_CLASSIC
    }

    fun setAppTheme(context: Context, theme: String) {
        getPrefs(context).edit().putString(KEY_APP_THEME, theme).apply()
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
