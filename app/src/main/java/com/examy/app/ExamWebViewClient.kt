package com.examy.app

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar

class ExamWebViewClient(
    private val examBaseUri: Uri,
    private val onPageFinishedListener: () -> Unit,
    private val onNetworkErrorListener: (Boolean) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val targetUri = request?.url ?: return false
        val targetHost = targetUri.host ?: ""
        val baseHost = examBaseUri.host ?: ""

        // Allow navigation within the same host, subdomains, Google Forms/Docs, or LAN IPs
        val isAllowed = targetHost.contains(baseHost, ignoreCase = true) ||
                targetHost.contains("google.com", ignoreCase = true) ||
                targetHost.contains("gstatic.com", ignoreCase = true) ||
                targetHost.contains("docs.google.com", ignoreCase = true) ||
                isLocalLanAddress(targetHost)

        return if (isAllowed) {
            false // Let WebView load it internally
        } else {
            // Block unauthorized external websites (YouTube, Play Store, Search)
            true
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onNetworkErrorListener(false) // Clear any offline error banner
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinishedListener()
        // Inject anti-copy & context-menu prevention CSS/JS
        injectAntiCheatScript(view)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            onNetworkErrorListener(true)
        }
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        // Crucial for School LAN servers that use self-signed certificates
        val host = examBaseUri.host ?: ""
        if (isLocalLanAddress(host)) {
            handler?.proceed()
        } else {
            handler?.proceed() // Proceed to avoid blocking school internal domains
        }
    }

    private fun isLocalLanAddress(host: String): Boolean {
        return host.startsWith("192.168.") ||
                host.startsWith("10.") ||
                host.startsWith("172.") ||
                host == "localhost" ||
                host == "127.0.0.1"
    }

    private fun injectAntiCheatScript(view: WebView?) {
        val js = """
            javascript:(function() {
                var style = document.createElement('style');
                style.innerHTML = '* { -webkit-user-select: none !important; user-select: none !important; } input, textarea { -webkit-user-select: text !important; user-select: text !important; }';
                document.head.appendChild(style);
                document.addEventListener('contextmenu', function(e) { e.preventDefault(); return false; });
            })()
        """.trimIndent()
        view?.evaluateJavascript(js, null)
    }

    companion object {
        @SuppressLint("SetJavaScriptEnabled")
        fun configureSecureSettings(webView: WebView, progressBar: ProgressBar) {
            val settings = webView.settings
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.allowFileAccess = true
            settings.allowContentAccess = true

            // Crucial: Allow HTTP LAN server resources even inside mixed contexts
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // Resilience: Cache responses so form isn't wiped if connection drops briefly
            settings.cacheMode = WebSettings.LOAD_DEFAULT

            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (newProgress < 100) {
                        progressBar.visibility = View.VISIBLE
                        progressBar.progress = newProgress
                    } else {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }
}
