package com.examy.app

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

object VolumeManager {

    const val MIN_VOLUME_PERCENT = 80

    /**
     * Returns the current media volume as a percentage (0 - 100).
     */
    fun getMediaVolumePercentage(context: Context): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return 100
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (maxVol <= 0) return 100
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return ((currentVol.toFloat() / maxVol.toFloat()) * 100).toInt()
    }

    /**
     * Checks if current volume is at least 80%.
     */
    fun isVolumeSufficient(context: Context): Boolean {
        return getMediaVolumePercentage(context) >= MIN_VOLUME_PERCENT
    }

    /**
     * Programmatically adjusts volume to 85% (exceeding the 80% threshold).
     */
    fun setVolumeToSufficientLevel(context: Context): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return false
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVol = (maxVol * 0.85f).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Enforces the 80% volume rule strictly.
     * If volume is < 80%, blocks with a mandatory dialog until volume reaches >= 80%.
     */
    fun enforceVolumeRequirement(activity: Activity, onSufficient: () -> Unit) {
        val currentPct = getMediaVolumePercentage(activity)
        if (currentPct >= MIN_VOLUME_PERCENT) {
            onSufficient()
            return
        }

        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_volume_warning, null)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvVolumeStatus)
        val btnAutoRaise = dialogView.findViewById<Button>(R.id.btnAutoRaiseVolume)
        val btnCheckAgain = dialogView.findViewById<Button>(R.id.btnCheckVolumeAgain)

        tvStatus.text = "Volume saat ini: $currentPct% (Wajib minimal $MIN_VOLUME_PERCENT%)"

        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnAutoRaise.setOnClickListener {
            setVolumeToSufficientLevel(activity)
            val updatedPct = getMediaVolumePercentage(activity)
            tvStatus.text = "Volume saat ini: $updatedPct% (Wajib minimal $MIN_VOLUME_PERCENT%)"
            if (updatedPct >= MIN_VOLUME_PERCENT) {
                Toast.makeText(activity, "Volume sudah mencukupi ($updatedPct%)", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onSufficient()
            }
        }

        btnCheckAgain.setOnClickListener {
            val updatedPct = getMediaVolumePercentage(activity)
            tvStatus.text = "Volume saat ini: $updatedPct% (Wajib minimal $MIN_VOLUME_PERCENT%)"
            if (updatedPct >= MIN_VOLUME_PERCENT) {
                Toast.makeText(activity, "Volume memenuhi syarat ($updatedPct%)", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onSufficient()
            } else {
                Toast.makeText(
                    activity,
                    "Volume masih $updatedPct%! Tekan tombol volume fisik HP untuk menaikkan minimal $MIN_VOLUME_PERCENT%",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialog.show()
    }
}
