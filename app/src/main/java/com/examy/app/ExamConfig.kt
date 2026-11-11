package com.examy.app

import android.util.Base64
import org.json.JSONObject
import java.io.Serializable
import java.nio.charset.StandardCharsets
import kotlin.random.Random

/**
 * Data model for complete Exam Configuration.
 */
data class ExamConfig(
    val subjectName: String = "",
    val examUrl: String = "",
    val studentPin: String = "123456",
    val proctorPin: String = "9999",
    val exitPin: String = "8888",
    val startDate: String = "",
    val startTime: String = "",
    val endDate: String = "",
    val endTime: String = "",
    val durationMinutes: Int = 90,
    val token: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {

    fun toJson(): String {
        val obj = JSONObject()
        obj.put("subject", subjectName)
        obj.put("url", examUrl)
        obj.put("student_pin", studentPin)
        obj.put("proctor_pin", proctorPin)
        obj.put("exit_pin", exitPin)
        obj.put("start_date", startDate)
        obj.put("start_time", startTime)
        obj.put("end_date", endDate)
        obj.put("end_time", endTime)
        obj.put("duration", durationMinutes)
        obj.put("token", token)
        obj.put("created_at", createdAt)
        return obj.toString()
    }

    /**
     * Formats combined date, time, and duration for display.
     */
    fun getScheduleDisplay(): String {
        val startParts = listOf(startDate, startTime).filter { it.isNotBlank() }
        val startStr = startParts.joinToString(", ")

        val endParts = listOf(endDate, endTime).filter { it.isNotBlank() }
        val endStr = endParts.joinToString(", ")

        return when {
            startStr.isNotBlank() && endStr.isNotBlank() -> "$startStr s/d $endStr ($durationMinutes Menit)"
            startStr.isNotBlank() -> "$startStr ($durationMinutes Menit)"
            else -> "$durationMinutes Menit"
        }
    }

    /**
     * Converts to shareable token payload for QR Code or student paste.
     * Format: EXAMY:<Base64EncodedJson>
     */
    fun toTokenString(): String {
        val json = toJson()
        val base64 = Base64.encodeToString(json.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        return "EXAMY:$base64"
    }

    companion object {
        private const val serialVersionUID: Long = 1L
        const val EXTRA_EXAM_CONFIG_JSON = "extra_exam_config_json"
        const val EXTRA_PROCTOR_PIN = "extra_proctor_pin"
        const val EXTRA_EXIT_PIN = "extra_exit_pin"

        private const val TOKEN_PREFIX = "EXAMY:"

        fun generateRandomToken(): String {
            val num = Random.nextInt(1000, 9999)
            return "EXAM-$num"
        }

        fun fromJson(jsonStr: String): ExamConfig? {
            return try {
                val obj = JSONObject(jsonStr)
                ExamConfig(
                    subjectName = obj.optString("subject", ""),
                    examUrl = obj.optString("url", ""),
                    studentPin = obj.optString("student_pin", "123456"),
                    proctorPin = obj.optString("proctor_pin", "9999"),
                    exitPin = obj.optString("exit_pin", "8888"),
                    startDate = obj.optString("start_date", ""),
                    startTime = obj.optString("start_time", ""),
                    endDate = obj.optString("end_date", ""),
                    endTime = obj.optString("end_time", ""),
                    durationMinutes = obj.optInt("duration", 90),
                    token = obj.optString("token", ""),
                    createdAt = obj.optLong("created_at", System.currentTimeMillis())
                )
            } catch (_: Exception) {
                null
            }
        }

        fun fromTokenString(tokenStr: String): ExamConfig? {
            val trimmed = tokenStr.trim()
            if (trimmed.startsWith(TOKEN_PREFIX, ignoreCase = true)) {
                val base64Part = trimmed.substring(TOKEN_PREFIX.length).trim()
                return try {
                    val decodedBytes = Base64.decode(base64Part, Base64.NO_WRAP)
                    val jsonStr = String(decodedBytes, StandardCharsets.UTF_8)
                    fromJson(jsonStr)
                } catch (_: Exception) {
                    null
                }
            } else if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                return fromJson(trimmed)
            } else {
                // Try decoding as bare Base64 if applicable
                try {
                    val decodedBytes = Base64.decode(trimmed, Base64.NO_WRAP)
                    val jsonStr = String(decodedBytes, StandardCharsets.UTF_8)
                    if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                        return fromJson(jsonStr)
                    }
                } catch (_: Exception) {
                    // Not valid base64, ignore
                }
            }
            return null
        }
    }
}
