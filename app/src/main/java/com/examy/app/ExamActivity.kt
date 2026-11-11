package com.examy.app

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.examy.app.databinding.ActivityExamBinding
import java.util.Locale

class ExamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamBinding
    private var examUrl: String = ""
    private var examConfig: ExamConfig? = null

    private var graceTimer: CountDownTimer? = null
    private var examCountdownTimer: CountDownTimer? = null
    private var isGracePeriodActive = false
    private var isViolationActive = false

    private val volumeHandler = Handler(Looper.getMainLooper())
    private val volumeCheckRunnable = object : Runnable {
        override fun run() {
            checkVolumeRequirement()
            volumeHandler.postDelayed(this, 2000L)
        }
    }

    companion object {
        const val EXTRA_EXAM_URL = "extra_exam_url"
        const val EXTRA_EXAM_CONFIG = "extra_exam_config"
        private const val GRACE_PERIOD_MILLIS = 5000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        examUrl = intent.getStringExtra(EXTRA_EXAM_URL) ?: ""

        // Multi-layer retrieval for ExamConfig (JSON string takes precedence to avoid serialization quirks)
        val jsonExtra = intent.getStringExtra(ExamConfig.EXTRA_EXAM_CONFIG_JSON)
        val parsedConfigFromJson = if (!jsonExtra.isNullOrBlank()) ExamConfig.fromJson(jsonExtra) else null

        val parsedConfigFromSerial = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(EXTRA_EXAM_CONFIG, ExamConfig::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(EXTRA_EXAM_CONFIG) as? ExamConfig
            }
        } catch (_: Exception) {
            null
        }

        examConfig = parsedConfigFromJson
            ?: parsedConfigFromSerial
            ?: SecurityManager.getActiveExamConfig(this)

        // Ensure PINs and session config are persisted locally
        examConfig?.let {
            SecurityManager.saveActiveExamConfig(this, it)
        }

        val directProctorPin = intent.getStringExtra(ExamConfig.EXTRA_PROCTOR_PIN)
        if (!directProctorPin.isNullOrBlank()) {
            SecurityManager.setProctorPin(this, directProctorPin)
        }

        val directExitPin = intent.getStringExtra(ExamConfig.EXTRA_EXIT_PIN)
        if (!directExitPin.isNullOrBlank()) {
            SecurityManager.setExitPin(this, directExitPin)
        }

        if (examUrl.isBlank()) {
            examUrl = examConfig?.examUrl ?: ""
        }

        if (examUrl.isBlank()) {
            Toast.makeText(this, "URL Ujian tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupSecurityAndKiosk()
        setupHeaderAndTimer()
        setupWebView()
        setupListeners()
        setupBackInterceptor()
    }

    private fun setupSecurityAndKiosk() {
        SecurityManager.enableKioskSecurity(this)
    }

    private fun setupHeaderAndTimer() {
        val config = examConfig
        if (config != null) {
            binding.tvExamSubjectTitle.text = config.subjectName.ifBlank { "Ujian CBT Online" }
            setupExamTimer(config.durationMinutes)
        } else {
            binding.tvExamSubjectTitle.text = "Sesi Ujian CBT"
            binding.layoutCountdown.visibility = View.GONE
        }
    }

    private fun setupExamTimer(durationMinutes: Int) {
        val totalMillis = durationMinutes * 60 * 1000L
        examCountdownTimer?.cancel()
        examCountdownTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / (1000 * 60 * 60)
                val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (millisUntilFinished % (1000 * 60)) / 1000
                binding.tvExamCountdownTimer.text =
                    String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                binding.tvExamCountdownTimer.text = "00:00:00"
                Toast.makeText(this@ExamActivity, "Waktu pengerjaan ujian telah selesai!", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun setupWebView() {
        val examUri = Uri.parse(examUrl)

        ExamWebViewClient.configureSecureSettings(
            binding.examWebView,
            binding.loadingProgressBar
        )

        binding.examWebView.webViewClient = ExamWebViewClient(
            examBaseUri = examUri,
            onPageFinishedListener = {
                // Page loaded successfully
                binding.bannerOffline.visibility = View.GONE
            },
            onNetworkErrorListener = { hasError ->
                binding.bannerOffline.visibility = if (hasError) View.VISIBLE else View.GONE
            }
        )

        binding.examWebView.loadUrl(examUrl)
    }

    private fun setupListeners() {
        // Floating / Header Exit Button (Student / Proctor Exit)
        binding.btnProctorUnlock.setOnClickListener {
            showExitDialog()
        }

        // Retry button on offline banner
        binding.btnRetryOffline.setOnClickListener {
            binding.bannerOffline.visibility = View.GONE
            binding.examWebView.reload()
        }

        // Resume button on grace overlay
        binding.btnResumeExam.setOnClickListener {
            cancelGracePeriod()
            SecurityManager.applyImmersiveFullscreen(this)
        }

        // Unlock violation button on locked overlay
        binding.btnUnlockViolation.setOnClickListener {
            handleProctorViolationUnlock()
        }

        // Raise volume button in exam overlay
        binding.btnExamRaiseVolume.setOnClickListener {
            VolumeManager.setVolumeToSufficientLevel(this)
            checkVolumeRequirement()
        }
    }

    private fun setupBackInterceptor() {
        // Prevent accidental exit on back gesture/button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isViolationActive) {
                    // Cannot dismiss violation overlay with back key
                    return
                }
                if (binding.examWebView.canGoBack()) {
                    binding.examWebView.goBack()
                } else {
                    showExitDialog()
                }
            }
        })
    }

    private fun checkVolumeRequirement() {
        if (isViolationActive) return

        val pct = VolumeManager.getMediaVolumePercentage(this)
        if (pct < VolumeManager.MIN_VOLUME_PERCENT) {
            binding.tvExamVolumeMessage.text =
                "Volume saat ini: $pct%. Aplikasi Examy mewajibkan volume minimal 80% agar alarm keamanan selalu siap berfungsi!"
            binding.overlayVolumeBlocked.visibility = View.VISIBLE
        } else {
            if (binding.overlayVolumeBlocked.visibility == View.VISIBLE) {
                binding.overlayVolumeBlocked.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SecurityManager.applyImmersiveFullscreen(this)
        volumeHandler.post(volumeCheckRunnable)
        if (!isViolationActive) {
            cancelGracePeriod()
        }
    }

    override fun onPause() {
        volumeHandler.removeCallbacks(volumeCheckRunnable)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            SecurityManager.applyImmersiveFullscreen(this)
            if (!isViolationActive) {
                cancelGracePeriod()
            }
        } else {
            // Window focus lost: start Grace Period (5 seconds)
            // If student fails to return in time, trigger the SIREN ALARM!
            if (!isViolationActive) {
                startGracePeriod()
            }
        }
    }

    private fun startGracePeriod() {
        if (isGracePeriodActive) return
        isGracePeriodActive = true
        binding.overlayGracePeriod.visibility = View.VISIBLE

        graceTimer?.cancel()
        graceTimer = object : CountDownTimer(GRACE_PERIOD_MILLIS, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000L) + 1
                binding.tvGraceCountdown.text =
                    "Kembali ke ujian dalam $secondsRemaining detik atau sirene alarm akan berbunyi!"
            }

            override fun onFinish() {
                // Grace period expired without returning: Trigger Violation & Siren Alarm!
                triggerViolation()
            }
        }.start()
    }

    private fun cancelGracePeriod() {
        isGracePeriodActive = false
        graceTimer?.cancel()
        binding.overlayGracePeriod.visibility = View.GONE
    }

    /**
     * Triggered when student commits a security violation (e.g. leaves app past grace period).
     * Sounds a loud alarm siren and locks the screen.
     */
    private fun triggerViolation() {
        if (isViolationActive) return
        isViolationActive = true
        isGracePeriodActive = false
        graceTimer?.cancel()
        binding.overlayGracePeriod.visibility = View.GONE

        // 1. Play loud siren alarm and vibrate continuously!
        AlarmSoundManager.startAlarm(this)

        // 2. Show red violation locked screen
        binding.overlayViolationLocked.visibility = View.VISIBLE
        binding.tvViolationPinError.visibility = View.GONE
        binding.etProctorViolationPin.text.clear()
        binding.etProctorViolationPin.requestFocus()
    }

    /**
     * Handles proctor entering the Proctor PIN (or Exit PIN) to silence siren and unlock exam.
     */
    private fun handleProctorViolationUnlock() {
        val inputPin = binding.etProctorViolationPin.text.toString().trim()
        if (SecurityManager.verifyProctorPin(this, inputPin, examConfig) ||
            SecurityManager.verifyExitPin(this, inputPin, examConfig)) {
            // Correct Proctor or Exit PIN: silence alarm and unlock
            AlarmSoundManager.stopAlarm()
            isViolationActive = false
            binding.overlayViolationLocked.visibility = View.GONE
            SecurityManager.applyImmersiveFullscreen(this)
            Toast.makeText(this, "Alarm dimatikan. Kunci ujian berhasil dibuka oleh pengawas.", Toast.LENGTH_SHORT).show()
        } else {
            // Incorrect PIN: Siren continues playing
            binding.tvViolationPinError.visibility = View.VISIBLE
            binding.etProctorViolationPin.text.clear()
        }
    }

    /**
     * Dialog for normal exit using PIN Keluar (or PIN Pengawas).
     */
    private fun showExitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exit_pin, null)
        val etPin = dialogView.findViewById<EditText>(R.id.etProctorPin)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tvExitDialogDesc)
        val tvError = dialogView.findViewById<TextView>(R.id.tvPinError)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelExit)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmExit)

        tvDesc.text = "Masukkan PIN Keluar atau PIN Pengawas untuk mengakhiri sesi ujian:"
        etPin.hint = "PIN Keluar"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
            SecurityManager.applyImmersiveFullscreen(this)
        }

        btnConfirm.setOnClickListener {
            val inputPin = etPin.text.toString().trim()
            if (SecurityManager.verifyExitPin(this, inputPin, examConfig) ||
                SecurityManager.verifyProctorPin(this, inputPin, examConfig)) {
                dialog.dismiss()
                SecurityManager.disableKioskSecurity(this)
                Toast.makeText(this, "Sesi ujian berakhir dengan aman.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                tvError.text = "PIN salah! Masukkan PIN Keluar atau PIN Pengawas."
                tvError.visibility = View.VISIBLE
                etPin.text.clear()
            }
        }

        dialog.show()
    }

    override fun onDestroy() {
        volumeHandler.removeCallbacks(volumeCheckRunnable)
        graceTimer?.cancel()
        examCountdownTimer?.cancel()
        AlarmSoundManager.stopAlarm()
        binding.examWebView.destroy()
        super.onDestroy()
    }
}
