package com.examy.app

import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.examy.app.databinding.ActivityCreateExamBinding
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.Calendar
import java.util.Locale

class CreateExamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateExamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadExistingConfigIfAny()
    }

    private fun setupUI() {
        binding.btnBackCreateExam.setOnClickListener {
            finish()
        }

        // Time Picker for Start Time
        binding.etStartTime.setOnClickListener {
            showTimePicker { hour, minute ->
                binding.etStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
            }
        }

        // Time Picker for End Time
        binding.etEndTime.setOnClickListener {
            showTimePicker { hour, minute ->
                binding.etEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
            }
        }

        // Preset Google Form Link
        binding.btnPresetGoogleForm.setOnClickListener {
            binding.etExamUrl.setText("https://docs.google.com/forms/d/e/1FAIpQLSc_sample_cbt_exam/viewform")
            Toast.makeText(this, "Contoh tautan Google Form disematkan", Toast.LENGTH_SHORT).show()
        }

        // Generate Token & QR Button
        binding.btnGenerateExamToken.setOnClickListener {
            validateAndGenerateExam()
        }
    }

    private fun loadExistingConfigIfAny() {
        val existing = SecurityManager.getActiveExamConfig(this)
        if (existing != null) {
            binding.etSubjectName.setText(existing.subjectName)
            binding.etExamUrl.setText(existing.examUrl)
            binding.etStudentPin.setText(existing.studentPin)
            binding.etProctorPin.setText(existing.proctorPin)
            binding.etExitPin.setText(existing.exitPin)
            binding.etStartTime.setText(existing.startTime)
            binding.etEndTime.setText(existing.endTime)
            binding.etDuration.setText(existing.durationMinutes.toString())
        } else {
            // Default sensible templates
            binding.etProctorPin.setText("9999")
            binding.etExitPin.setText("8888")
            binding.etStudentPin.setText("123456")
            binding.etStartTime.setText("08:00")
            binding.etEndTime.setText("10:00")
            binding.etDuration.setText("90")
        }
    }

    private fun showTimePicker(onTimeSelected: (hour: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, hour, minute ->
            onTimeSelected(hour, minute)
        }, currentHour, currentMinute, true).show()
    }

    private fun validateAndGenerateExam() {
        val subject = binding.etSubjectName.text.toString().trim()
        val rawUrl = binding.etExamUrl.text.toString().trim()
        val studentPin = binding.etStudentPin.text.toString().trim()
        val proctorPin = binding.etProctorPin.text.toString().trim()
        val exitPin = binding.etExitPin.text.toString().trim()
        val startTime = binding.etStartTime.text.toString().trim()
        val endTime = binding.etEndTime.text.toString().trim()
        val durationStr = binding.etDuration.text.toString().trim()

        if (subject.isBlank()) {
            binding.etSubjectName.error = "Nama mata pelajaran wajib diisi"
            binding.etSubjectName.requestFocus()
            return
        }

        if (rawUrl.isBlank()) {
            binding.etExamUrl.error = "Link ujian / Google Form wajib diisi"
            binding.etExamUrl.requestFocus()
            return
        }

        var examUrl = rawUrl
        if (!examUrl.startsWith("http://", ignoreCase = true) && !examUrl.startsWith("https://", ignoreCase = true)) {
            examUrl = "https://$examUrl"
        }

        if (studentPin.length < 4) {
            binding.etStudentPin.error = "PIN Siswa minimal 4 angka"
            binding.etStudentPin.requestFocus()
            return
        }

        if (proctorPin.length < 4) {
            binding.etProctorPin.error = "PIN Pengawas minimal 4 angka"
            binding.etProctorPin.requestFocus()
            return
        }

        if (exitPin.length < 4) {
            binding.etExitPin.error = "PIN Keluar minimal 4 angka"
            binding.etExitPin.requestFocus()
            return
        }

        val duration = durationStr.toIntOrNull() ?: 90

        val tokenCode = ExamConfig.generateRandomToken()

        val config = ExamConfig(
            subjectName = subject,
            examUrl = examUrl,
            studentPin = studentPin,
            proctorPin = proctorPin,
            exitPin = exitPin,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = duration,
            token = tokenCode
        )

        // Save active configuration
        SecurityManager.saveActiveExamConfig(this, config)

        val fullTokenString = config.toTokenString()

        // Generate QR Code bitmap via ZXing BarcodeEncoder
        val qrBitmap: Bitmap? = try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(fullTokenString, BarcodeFormat.QR_CODE, 600, 600)
        } catch (_: Exception) {
            null
        }

        showGeneratedTokenDialog(config, fullTokenString, qrBitmap)
    }

    private fun showGeneratedTokenDialog(config: ExamConfig, tokenPayload: String, qrBitmap: Bitmap?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_generated_token, null)
        val tvSubject = dialogView.findViewById<TextView>(R.id.tvDialogSubjectSummary)
        val ivQr = dialogView.findViewById<ImageView>(R.id.ivGeneratedQr)
        val tvTokenCode = dialogView.findViewById<TextView>(R.id.tvGeneratedTokenCode)
        val btnCopy = dialogView.findViewById<Button>(R.id.btnCopyToken)
        val btnShare = dialogView.findViewById<Button>(R.id.btnShareToken)
        val btnDone = dialogView.findViewById<Button>(R.id.btnDoneTokenDialog)

        tvSubject.text = "${config.subjectName} (${config.durationMinutes} Menit • PIN Siswa: ${config.studentPin})"
        tvTokenCode.text = config.token

        if (qrBitmap != null) {
            ivQr.setImageBitmap(qrBitmap)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Examy Token", tokenPayload)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Token konfigurasi ujian berhasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
        }

        btnShare.setOnClickListener {
            val shareText = """
                📌 *SESI UJIAN EXAMY*
                Mata Pelajaran: ${config.subjectName}
                Jadwal: ${config.startTime} - ${config.endTime} (${config.durationMinutes} Menit)
                PIN Siswa: *${config.studentPin}*
                
                Kode Token:
                ${config.token}
                
                Token Konfigurasi Lengkap:
                $tokenPayload
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Token Ujian - ${config.subjectName}")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Bagikan Token Ujian via"))
        }

        btnDone.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "Konfigurasi ujian telah tersimpan aktif!", Toast.LENGTH_SHORT).show()
            finish()
        }

        dialog.show()
    }
}
