package com.examy.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.examy.app.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Register ZXing barcode/QR scanner launcher
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val scannedUrl = result.contents.trim()
            binding.etExamUrl.setText(scannedUrl)
            launchExam(scannedUrl)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadRecentUrls()
    }

    override fun onResume() {
        super.onResume()
        loadRecentUrls()
    }

    private fun setupUI() {
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

        // Start Exam Button click
        binding.btnStartExam.setOnClickListener {
            val rawUrl = binding.etExamUrl.text.toString().trim()
            if (rawUrl.isBlank()) {
                Toast.makeText(this, "Masukkan URL atau scan QR ujian terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            launchExam(rawUrl)
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

    private fun launchExam(rawUrl: String) {
        val sanitizedUrl = sanitizeUrl(rawUrl)
        if (!isValidUrl(sanitizedUrl)) {
            Toast.makeText(this, getString(R.string.toast_invalid_url), Toast.LENGTH_SHORT).show()
            return
        }

        SecurityManager.addRecentUrl(this, sanitizedUrl)

        val intent = Intent(this, ExamActivity::class.java).apply {
            putExtra(ExamActivity.EXTRA_EXAM_URL, sanitizedUrl)
        }
        startActivity(intent)
    }

    private fun sanitizeUrl(input: String): String {
        var url = input.trim()
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            // Default to http for typical LAN IPs (e.g. 192.168.x.x), https for domain names
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
