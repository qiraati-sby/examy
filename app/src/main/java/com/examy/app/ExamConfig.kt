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
    val studentPin: String = "",
    val proctorPin: String = "1234",
    val exitPin: String = "1234",
    val startTime: String = "",
    val endTime: String = "",
    val durationMinutes: Int = 90,
    val token: String = ""
) : Serializable {

    fun toJson(): String {
        val obj = JSONObject()
        obj.put("subject", subjectName)
        obj.put("url", examUrl)
        obj.put("student_pin", studentPin)
        obj.put("proctor_pin", proctorPin)
        obj.put("exit_pin", exitPin)
        obj.put("start_time", startTime)
        obj.put("end_time", endTime)
        obj.put("duration", durationMinutes)
        obj.put("token", token)
        return obj.toString()
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
                    studentPin = obj.optString("student_pin", ""),
                    proctorPin = obj.optString("proctor_pin", "1234"),
                    exitPin = obj.optString("exit_pin", "1234"),
                    startTime = obj.optString("start_time", ""),
                    endTime = obj.optString("end_time", ""),
                    durationMinutes = obj.optInt("duration", 90),
                    token = obj.optString("token", "")
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
            }
            return null
        }
    }
}
