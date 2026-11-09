package com.examy.app

import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
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

class ExamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExamBinding
    private var examUrl: String = ""
    private var graceTimer: CountDownTimer? = null
    private var isGracePeriodActive = false

    companion object {
        const val EXTRA_EXAM_URL = "extra_exam_url"
        private const val GRACE_PERIOD_MILLIS = 5000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        examUrl = intent.getStringExtra(EXTRA_EXAM_URL) ?: ""
        if (examUrl.isBlank()) {
            Toast.makeText(this, "URL Ujian tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupSecurityAndKiosk()
        setupWebView()
        setupListeners()
        setupBackInterceptor()
    }

    private fun setupSecurityAndKiosk() {
        SecurityManager.enableKioskSecurity(this)
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
        // Floating Proctor Unlock Button
        binding.btnProctorUnlock.setOnClickListener {
            showProctorExitDialog()
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
    }

    private fun setupBackInterceptor() {
        // Prevent accidental exit on back gesture/button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.examWebView.canGoBack()) {
                    binding.examWebView.goBack()
                } else {
                    showProctorExitDialog()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        SecurityManager.applyImmersiveFullscreen(this)
        cancelGracePeriod()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            SecurityManager.applyImmersiveFullscreen(this)
            cancelGracePeriod()
        } else {
            // Focus lost (e.g. system alert dialog, low battery warning, volume slider)
            // Start 5-second Grace Period to prevent FALSE POSITIVES!
            startGracePeriod()
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
                    "Kembali ke ujian dalam $secondsRemaining detik atau sesi akan dikunci!"
            }

            override fun onFinish() {
                binding.tvGraceCountdown.text =
                    "Sesi Terkunci! Hubungi Pengawas untuk membuka kembali."
                binding.btnResumeExam.text = "Masukkan PIN Pengawas"
                binding.btnResumeExam.setOnClickListener {
                    showProctorExitDialog()
                }
            }
        }.start()
    }

    private fun cancelGracePeriod() {
        isGracePeriodActive = false
        graceTimer?.cancel()
        binding.overlayGracePeriod.visibility = View.GONE
        binding.btnResumeExam.text = "Lanjutkan Ujian Sekarang"
        binding.btnResumeExam.setOnClickListener {
            cancelGracePeriod()
            SecurityManager.applyImmersiveFullscreen(this)
        }
    }

    private fun showProctorExitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exit_pin, null)
        val etPin = dialogView.findViewById<EditText>(R.id.etProctorPin)
        val tvError = dialogView.findViewById<TextView>(R.id.tvPinError)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelExit)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmExit)

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
            if (SecurityManager.verifyPin(this, inputPin)) {
                dialog.dismiss()
                SecurityManager.disableKioskSecurity(this)
                finish()
            } else {
                tvError.visibility = View.VISIBLE
                etPin.text.clear()
            }
        }

        dialog.show()
    }

    override fun onDestroy() {
        graceTimer?.cancel()
        binding.examWebView.destroy()
        super.onDestroy()
    }
}
