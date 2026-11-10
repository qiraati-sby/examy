package com.examy.app

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
            processExamEntry(scannedContent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        updateVolumeIndicator()
    }

    override fun onResume() {
        super.onResume()
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

        // Button 1: Pindai QR Code
        binding.btnScanQr.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Arahkan kamera ke QR Code ujian dari pengawas")
                setBeepEnabled(true)
                setOrientationLocked(true)
                setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity::class.java)
            }
            barcodeLauncher.launch(options)
        }

        // Button 2: + Buat Ujian (Teacher Exam Creator)
        binding.btnCreateExam.setOnClickListener {
            startActivity(Intent(this, CreateExamActivity::class.java))
        }

        // Button 3: Input URL Ujian
        binding.btnEnterUrl.setOnClickListener {
            showUrlInputDialog()
        }

        // Button 4: Tutup Aplikasi
        binding.btnExitApp.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Tutup Aplikasi?")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi Examy?")
                .setPositiveButton("Keluar") { _, _ ->
                    finishAffinity()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun updateVolumeIndicator() {
        val pct = VolumeManager.getMediaVolumePercentage(this)
        val isSufficient = pct >= VolumeManager.MIN_VOLUME_PERCENT

        if (isSufficient) {
            binding.tvVolumePercentTitle.text = "Volume HP: $pct% (Memenuhi Syarat Minimal 80% [OK])"
            binding.tvVolumePercentTitle.setTextColor(ContextCompat.getColor(this, R.color.success))
            binding.btnAdjustVolume.visibility = View.GONE
        } else {
            binding.tvVolumePercentTitle.text = "Volume HP: $pct% (Kurang dari 80% [Perlu Dinaikkan])"
            binding.tvVolumePercentTitle.setTextColor(ContextCompat.getColor(this, R.color.danger))
            binding.btnAdjustVolume.visibility = View.VISIBLE
        }
    }

    private fun showUrlInputDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_url, null)
        val etUrl = dialogView.findViewById<EditText>(R.id.etDialogExamUrl)
        val btnPaste = dialogView.findViewById<Button>(R.id.btnDialogPasteUrl)
        val layoutRecent = dialogView.findViewById<View>(R.id.layoutDialogRecent)
        val chipGroupRecent = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chipGroupDialogRecent)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelUrlDialog)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitUrlDialog)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Paste functionality
        btnPaste.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val pasteData = clip.getItemAt(0).text?.toString()?.trim()
                if (!pasteData.isNullOrEmpty()) {
                    etUrl.setText(pasteData)
                    Toast.makeText(this, "Tautan berhasil ditempel!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Clipboard kosong atau bukan teks", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Clipboard kosong", Toast.LENGTH_SHORT).show()
            }
        }

        // Load recent URLs if any
        val recents = SecurityManager.getRecentUrls(this)
        if (recents.isNotEmpty()) {
            layoutRecent.visibility = View.VISIBLE
            chipGroupRecent.removeAllViews()
            for (url in recents) {
                val chip = Chip(this).apply {
                    text = url
                    isCheckable = false
                    isClickable = true
                    setOnClickListener {
                        etUrl.setText(url)
                    }
                }
                chipGroupRecent.addView(chip)
            }
        } else {
            layoutRecent.visibility = View.GONE
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            val rawInput = etUrl.text.toString().trim()
            if (rawInput.isBlank()) {
                Toast.makeText(this, "Masukkan URL atau link ujian terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            processExamEntry(rawInput)
        }

        dialog.show()
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
}
