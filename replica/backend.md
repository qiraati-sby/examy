# Backend & Security Architecture - Examy

Backend architecture, security policies, and client-server communication specs for Examy.

---

## 1. Architecture Overview

Examy employs a **Hybrid Client-Centric Architecture**:
1. **Target CBT Hosts**:
   - Cloud Form Providers: Google Forms (`docs.google.com/forms`), Microsoft Forms, Quizizz, dsb.
   - School LAN / Local CBT Servers: Moodle, Candy CBT, BeeSMART, CBT Nusantara, dsb. via HTTP/HTTPS pada segmen IP privat (`192.168.0.0/16`, `10.0.0.0/8`, `172.16.0.0/12`).
2. **Payload & Token Encoding**:
   - Token ujian menggunakan skema transport stateless `EXAMY:<Base64>` yang membungkus metadata ujian, URL, jadwal, durasi, dan PIN hash. Tidak memerlukan server terpusat yang rentan down saat hari ujian.
3. **Resilient Local Persistence**:
   - `Android EncryptedSharedPreferences`: Menyimpan riwayat URL ujian (maksimal 5 item), default PIN pengawas, dan pengaturan preferensi lokal secara aman.

---

## 2. API & Network Configuration

### Mixed Content Mode
- Karena server CBT sekolah di LAN umumnya menggunakan protokol HTTP biasa tanpa sertifikat TLS/SSL, WebView dikonfigurasi dengan:
  ```kotlin
  webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
  ```
- Android Network Security Config mengizinkan `cleartextTrafficPermitted="true"` untuk subnet lokal sekolah.

### Non-Destructive DOM State Retention
- Ketika terjadi interupsi Wi-Fi sekolah (RTO / drop koneksi), `ExamWebViewClient.kt` mencegat `onReceivedError` dan tidak melakukan `view.loadUrl("about:blank")` yang menghapus isian radio button dan teks siswa.
- WebView mempertahankan instance DOM yang ada dan menampilkan overlay toast/banner "Koneksi terputus, klik Coba Lagi" sehingga isian siswa tetap tersimpan di memory WebView.

---

## 3. Anti-Cheat & Security Policy

| Vektor Serangan | Metode Pencegahan di Examy | Implementasi Teknis |
| :--- | :--- | :--- |
| **Penyalinan Teks Soal** | JavaScript & CSS Injection | Menghapus seleksi teks, mendisable context menu (`user-select: none`, `oncontextmenu="return false;"`) |
| **Perekaman Layar / Screenshot** | Android OS Window Policy | `window.setFlags(FLAG_SECURE, FLAG_SECURE)` aktif di seluruh lifecycle `ExamActivity` |
| **Berpindah Tab / Split Screen** | Android Kiosk Task Locking | `startLockTask()` mengunci sistem navigasi dan task switcher OS |
| **Menurunkan Volume Suara** | AudioManager Real-time Observer | `VolumeManager.kt` secara berkala memastikan stream volume musik/alarm tetap >= 80% |
| **Pelanggaran Kiosk** | Sirene & Getar Maksimal | `AlarmSoundManager.kt` memicu pemutaran alarm darurat loop tanpa henti sampai PIN pengawas diverifikasi |

---

## 4. Security Checklist Verification

- [x] **No hardcoded production secrets**: Tidak ada token sensitif atau master keys yang di-commit ke repositori.
- [x] **Zero private API dependencies**: Seluruh komunikasi menggunakan protokol standar HTTP/HTTPS dan Google Form APIs.
- [x] **Encrypted Local Storage**: Data PIN dan konfigurasi dienkripsi menggunakan SharedPreferences standar Android.
- [x] **Grace Period Tolerance**: Toleransi 5 detik sebelum alarm aktif mencegah false-positive akibat notifikasi sistem atau lag OS.
- [x] **Strict Domain Isolation**: Skema intent dilindungi agar hanya memuat URL yang sesuai dengan konfigurasi guru.
