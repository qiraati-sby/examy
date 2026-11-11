package com.examy.app

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
            processExamEntry(scannedContent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        applyTheme(SecurityManager.getAppTheme(this))
        updateVolumeIndicator()
    }

    override fun onResume() {
        super.onResume()
        applyTheme(SecurityManager.getAppTheme(this))
        updateVolumeIndicator()
    }

    private fun setupUI() {
        // Theme Switcher Buttons
        binding.btnThemeClassic.setOnClickListener {
            SecurityManager.setAppTheme(this, SecurityManager.THEME_CLASSIC)
            applyTheme(SecurityManager.THEME_CLASSIC)
            Toast.makeText(this, "Tema Klasik aktif", Toast.LENGTH_SHORT).show()
        }

        binding.btnThemeSantri.setOnClickListener {
            SecurityManager.setAppTheme(this, SecurityManager.THEME_SANTRI)
            applyTheme(SecurityManager.THEME_SANTRI)
            Toast.makeText(this, "Tema Santri Lime aktif", Toast.LENGTH_SHORT).show()
        }

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

    private fun applyTheme(themeMode: String) {
        val isSantri = themeMode == SecurityManager.THEME_SANTRI

        if (isSantri) {
            // Theme Santri Lime (#621EF3, #BDED0F, #EAF7DF, #F62217)
            binding.rootMainLayout.setBackgroundColor(Color.parseColor("#EAF7DF"))

            binding.btnScanQr.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#621EF3"))
            binding.btnScanQr.setTextColor(Color.WHITE)
            binding.btnScanQr.iconTint = ColorStateList.valueOf(Color.WHITE)

            binding.btnCreateExam.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDED0F"))
            binding.btnCreateExam.setTextColor(Color.parseColor("#0F172A"))

            binding.btnEnterUrl.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            binding.btnEnterUrl.strokeColor = ColorStateList.valueOf(Color.parseColor("#621EF3"))
            binding.btnEnterUrl.setTextColor(Color.parseColor("#621EF3"))

            binding.btnExitApp.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            binding.btnExitApp.strokeColor = ColorStateList.valueOf(Color.parseColor("#F62217"))
            binding.btnExitApp.setTextColor(Color.parseColor("#F62217"))

            binding.viewOperatorUnderline.setBackgroundColor(Color.parseColor("#F62217"))

            // Capsule Indicator
            binding.btnThemeSantri.setBackgroundResource(R.drawable.bg_primary_button)
            binding.btnThemeSantri.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#BDED0F"))
            binding.btnThemeSantri.setTextColor(Color.parseColor("#0F172A"))

            binding.btnThemeClassic.background = null
            binding.btnThemeClassic.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        } else {
            // Theme Classic (#4F46E5, #EA580C, #F8FAFC, #DC2626)
            binding.rootMainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background))

            binding.btnScanQr.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            binding.btnScanQr.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary))
            binding.btnScanQr.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_on_primary))

            binding.btnCreateExam.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EA580C"))
            binding.btnCreateExam.setTextColor(Color.WHITE)

            binding.btnEnterUrl.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            binding.btnEnterUrl.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            binding.btnEnterUrl.setTextColor(ContextCompat.getColor(this, R.color.primary))

            binding.btnExitApp.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            binding.btnExitApp.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.danger))
            binding.btnExitApp.setTextColor(ContextCompat.getColor(this, R.color.danger))

            binding.viewOperatorUnderline.setBackgroundColor(ContextCompat.getColor(this, R.color.danger))

            // Capsule Indicator
            binding.btnThemeClassic.setBackgroundResource(R.drawable.bg_primary_button)
            binding.btnThemeClassic.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            binding.btnThemeClassic.setTextColor(Color.WHITE)

            binding.btnThemeSantri.background = null
            binding.btnThemeSantri.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
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
        val trimmedInput = input.trim()
        if (trimmedInput.isBlank()) return

        // Enforce Volume Requirement: App will not run unless volume >= 80%
        VolumeManager.enforceVolumeRequirement(this) {
            updateVolumeIndicator()

            // Check if input is a generated Token or JSON config
            val configFromToken = ExamConfig.fromTokenString(trimmedInput)
            val activeConfig = SecurityManager.getActiveExamConfig(this)
            val historyMatch = SecurityManager.getExamHistory(this).firstOrNull {
                it.token.equals(trimmedInput, ignoreCase = true)
            }

            val resolvedConfig = configFromToken
                ?: (if (activeConfig != null && trimmedInput.equals(activeConfig.token, ignoreCase = true)) activeConfig else null)
                ?: historyMatch

            if (resolvedConfig != null) {
                // Exam configuration detected: persist immediately to device storage
                SecurityManager.saveActiveExamConfig(this, resolvedConfig)
                // Prompt student for PIN Utama Siswa
                showStudentConfirmDialog(resolvedConfig)
            } else {
                // Check if input resembles a token code (e.g. EXAM-XXXX) but is not found locally
                if (trimmedInput.startsWith("EXAM-", ignoreCase = true) && !trimmedInput.contains(".")) {
                    Toast.makeText(
                        this,
                        "Kode token '$trimmedInput' belum tersimpan di HP ini. Gunakan tombol 'Pindai QR Code' dari HP pengawas.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@enforceVolumeRequirement
                }

                // Direct URL flow
                val sanitizedUrl = sanitizeUrl(trimmedInput)
                if (!isValidUrl(sanitizedUrl)) {
                    Toast.makeText(this, getString(R.string.toast_invalid_url), Toast.LENGTH_SHORT).show()
                    return@enforceVolumeRequirement
                }

                // If an active config exists matching this URL, check student pin
                if (activeConfig != null && (sanitizedUrl == activeConfig.examUrl || activeConfig.examUrl.contains(sanitizedUrl))) {
                    showStudentConfirmDialog(activeConfig)
                } else {
                    // Direct exam launch with fallback default proctor and exit PINs
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
                // Persist verified session on device
                SecurityManager.saveActiveExamConfig(this, config)
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

        // Ensure active config or default pins are saved locally
        if (config != null) {
            SecurityManager.saveActiveExamConfig(this, config)
        }

        val proctorPin = config?.proctorPin ?: SecurityManager.getProctorPin(this)
        val exitPin = config?.exitPin ?: SecurityManager.getExitPin(this)

        val intent = Intent(this, ExamActivity::class.java).apply {
            putExtra(ExamActivity.EXTRA_EXAM_URL, sanitizedUrl)
            if (config != null) {
                putExtra(ExamActivity.EXTRA_EXAM_CONFIG, config)
                putExtra(ExamConfig.EXTRA_EXAM_CONFIG_JSON, config.toJson())
            }
            putExtra(ExamConfig.EXTRA_PROCTOR_PIN, proctorPin)
            putExtra(ExamConfig.EXTRA_EXIT_PIN, exitPin)
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
