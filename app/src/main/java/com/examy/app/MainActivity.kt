package com.examy.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.examy.app.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Register ZXing barcode/QR scanner launcher
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val scannedContent = result.contents.trim()
            binding.etExamUrl.setText(scannedContent)
            processExamEntry(scannedContent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadRecentUrls()
        updateVolumeIndicator()
    }

    override fun onResume() {
        super.onResume()
        loadRecentUrls()
        updateVolumeIndicator()
    }

    private fun setupUI() {
        // Volume card & button click
        binding.cardVolumeStatus.setOnClickListener {
            VolumeManager.enforceVolumeRequirement(this) {
                updateVolumeIndicator()
            }
        }

        binding.btnAdjustVolume.setOnClickListener {
            VolumeManager.setVolumeToSufficientLevel(this)
            updateVolumeIndicator()
        }

        // Scan QR Card click
        binding.cardScanQr.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Arahkan kamera ke QR Code ujian dari pengawas")
                setBeepEnabled(true)
                setOrientationLocked(true)
                setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity::class.java)
            }
            barcodeLauncher.launch(options)
        }

        // Teacher Menu: Create & Configure Exam
        binding.cardCreateExam.setOnClickListener {
            startActivity(Intent(this, CreateExamActivity::class.java))
        }

        // Start Exam Button click
        binding.btnStartExam.setOnClickListener {
            val rawInput = binding.etExamUrl.text.toString().trim()
            if (rawInput.isBlank()) {
                Toast.makeText(this, "Masukkan URL atau scan QR ujian terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            processExamEntry(rawInput)
        }

        // Settings Button (Teacher / Proctor PIN settings)
        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        // Clear History
        binding.btnClearHistory.setOnClickListener {
            SecurityManager.clearRecentUrls(this)
            loadRecentUrls()
        }
    }

    private fun updateVolumeIndicator() {
        val pct = VolumeManager.getMediaVolumePercentage(this)
        val isSufficient = pct >= VolumeManager.MIN_VOLUME_PERCENT

        if (isSufficient) {
            binding.tvVolumePercentTitle.text = "Volume HP: $pct% (Memenuhi Syarat Minimal 80% âœ“)"
            binding.tvVolumePercentTitle.setTextColor(ContextCompat.getColor(this, R.color.success))
            binding.btnAdjustVolume.visibility = View.GONE
        } else {
            binding.tvVolumePercentTitle.text = "Volume HP: $pct% (Kurang dari 80% âœ—)"
            binding.tvVolumePercentTitle.setTextColor(ContextCompat.getColor(this, R.color.danger))
            binding.btnAdjustVolume.visibility = View.VISIBLE
        }
    }

    /**
     * Processes exam entry with strict volume check and token/PIN verification.
     */
    private fun processExamEntry(input: String) {
        // Enforce Volume Requirement: App will not run unless volume >= 80%
        VolumeManager.enforceVolumeRequirement(this) {
            updateVolumeIndicator()

            // Check if input is a generated Token or JSON config
            val configFromToken = ExamConfig.fromTokenString(input)
            val activeConfig = SecurityManager.getActiveExamConfig(this)

            val resolvedConfig = configFromToken ?: if (activeConfig != null && input == activeConfig.token) {
                activeConfig
            } else null

            if (resolvedConfig != null) {
                // Exam configuration detected: prompt student for PIN Utama Siswa
                showStudentConfirmDialog(resolvedConfig)
            } else {
                // Direct URL flow
                val sanitizedUrl = sanitizeUrl(input)
                if (!isValidUrl(sanitizedUrl)) {
                    Toast.makeText(this, getString(R.string.toast_invalid_url), Toast.LENGTH_SHORT).show()
                    return@enforceVolumeRequirement
                }

                // If an active config exists matching this URL, check student pin
                if (activeConfig != null && (sanitizedUrl == activeConfig.examUrl || activeConfig.examUrl.contains(sanitizedUrl))) {
                    showStudentConfirmDialog(activeConfig)
                } else {
                    launchDirectExam(sanitizedUrl, null)
                }
            }
        }
    }

    private fun showStudentConfirmDialog(config: ExamConfig) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_student_confirm_exam, null)
        val tvSubject = dialogView.findViewById<TextView>(R.id.tvStudentSubjectName)
        val tvSchedule = dialogView.findViewById<TextView>(R.id.tvStudentExamSchedule)
        val tvVolume = dialogView.findViewById<TextView>(R.id.tvStudentVolumeStatus)
        val etPin = dialogView.findViewById<EditText>(R.id.etStudentInputPin)
        val tvPinError = dialogView.findViewById<TextView>(R.id.tvStudentPinError)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelStudentConfirm)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitStudentConfirm)

        tvSubject.text = config.subjectName.ifBlank { "Ujian CBT Online" }
        tvSchedule.text = "Jadwal: ${config.getScheduleDisplay()}"

        val vol = VolumeManager.getMediaVolumePercentage(this)
        tvVolume.text = "Volume HP: $vol% (Memenuhi Syarat Minimal 80%)"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            // Re-verify volume before launching
            if (!VolumeManager.isVolumeSufficient(this)) {
                dialog.dismiss()
                VolumeManager.enforceVolumeRequirement(this) {
                    showStudentConfirmDialog(config)
                }
                return@setOnClickListener
            }

            val inputPin = etPin.text.toString().trim()
            if (SecurityManager.verifyStudentPin(inputPin, config)) {
                dialog.dismiss()
                launchDirectExam(config.examUrl, config)
            } else {
                tvPinError.visibility = View.VISIBLE
                etPin.text.clear()
            }
        }

        dialog.show()
    }

    private fun launchDirectExam(sanitizedUrl: String, config: ExamConfig?) {
        SecurityManager.addRecentUrl(this, sanitizedUrl)

        val intent = Intent(this, ExamActivity::class.java).apply {
            putExtra(ExamActivity.EXTRA_EXAM_URL, sanitizedUrl)
            if (config != null) {
                putExtra(ExamActivity.EXTRA_EXAM_CONFIG, config)
            }
        }
        startActivity(intent)
    }

    private fun sanitizeUrl(input: String): String {
        var url = input.trim()
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = if (url.startsWith("192.168.") || url.startsWith("10.") || url.startsWith("172.")) {
                "http://$url"
            } else {
                "https://$url"
            }
        }
        return url
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val parsed = Uri.parse(url)
            parsed.scheme != null && parsed.host != null
        } catch (_: Exception) {
            false
        }
    }

    private fun loadRecentUrls() {
        val recents = SecurityManager.getRecentUrls(this)
        binding.chipGroupRecent.removeAllViews()

        if (recents.isEmpty()) {
            binding.layoutRecentSection.visibility = View.GONE
        } else {
            binding.layoutRecentSection.visibility = View.VISIBLE
            for (url in recents) {
                val chip = Chip(this).apply {
                    text = url
                    isCheckable = false
                    isClickable = true
                    setOnClickListener {
                        binding.etExamUrl.setText(url)
                    }
                }
                binding.chipGroupRecent.addView(chip)
            }
        }
    }

    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
        val etNewPin = dialogView.findViewById<EditText>(R.id.etNewPin)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelSettings)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveSettings)

        val currentPin = SecurityManager.getProctorPin(this)
        etNewPin.setText(currentPin)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newPin = etNewPin.text.toString().trim()
            if (newPin.length < 4) {
                Toast.makeText(this, "PIN harus minimal 4 angka", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            SecurityManager.setProctorPin(this, newPin)
            Toast.makeText(this, getString(R.string.toast_pin_saved), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
