# Examy - Secure & Resilient Exam Browser

**Examy** adalah aplikasi Android Exam Browser (Exambro / Kiosk Browser) yang dirancang khusus untuk memfasilitasi ujian online dan Computer Based Test (CBT) berbasis LAN maupun Google Form secara aman, ringan, dan bebas dari kendala umum aplikasi pengunci ujian konvensional.

---

## Fitur Utama & Keunggulan Dibandingkan Aplikasi Sejenis

| Fitur | Aplikasi Konvensional / Asli | **Examy (Aplikasi Ini)** |
| :--- | :--- | :--- |
| **Metode Kiosk Lockdown** | Mengandalkan *Accessibility Service* (sering mati di Android 12-15) | **Native `startLockTask()` & `WindowInsetsControllerCompat`** (API resmi Google, 100% stabil) |
| **Toleransi *False Positive*** | Langsung mendiskualifikasi/mengunci jika ada notifikasi baterai | **Grace Period 5 Detik**: Memberi waktu siswa kembali sebelum mengunci sesi ujian |
| **Ketahanan Jaringan LAN** | Halaman hilang / *ERR_CONNECTION_REFUSED* saat Wi-Fi drop sesaat | **Resilient Non-Destructive Error UI**: Isian formulir siswa tidak terhapus |
| **Penyalinan Jawaban** | Seringkali teks soal masih bisa di-blok | **Injeksi Anti-Copy & Anti-ContextMenu**: Seleksi teks dan klik kanan diblokir otomatis |
| **Kompatibilitas Server LAN** | Sering memblokir HTTP lokal karena mixed content | Mengaktifkan **`MIXED_CONTENT_ALWAYS_ALLOW`** untuk server LAN sekolah |
| **Input Fleksibel** | Manual ketik URL | **Scanner QR Code terintegrasi (ZXing)** + Riwayat URL Ujian Terakhir |
| **Akses Pengawas** | PIN statis / tidak fleksibel | **PIN Pengawas Dapat Dikonfigurasi** (Default: `1234`) |

---

## Struktur Direktori Proyek

```
Examy/
├── .github/
│   └── workflows/
│       └── build-apk.yml            # Otomasi kompilasi APK di cloud (GitHub Actions)
├── app/
│   ├── build.gradle.kts             # Dependensi & konfigurasi modul Android
│   ├── src/main/
│   │   ├── AndroidManifest.xml      # Izin kamera, internet, kiosk configuration
│   │   ├── java/com/examy/app/
│   │   │   ├── MainActivity.kt      # Halaman utama, input URL, QR scanner, settings
│   │   │   ├── ExamActivity.kt      # Layar fullscreen terkunci, webview, dialog proktor
│   │   │   ├── ExamWebViewClient.kt # Whitelist domain, anti-copy script, error handler
│   │   │   └── SecurityManager.kt   # Logika Kiosk, FLAG_SECURE, & enkripsi PIN
│   │   └── res/                     # Layout, icon, warna, & string multibahasa
├── replica/                         # Catatan audit Clean-Room & matriks fitur
└── README.md
```

---

## Panduan Kompilasi Menjadi APK

### Cara 1: Kompilasi Otomatis via GitHub Actions (Paling Praktis Tanpa Install SDK di Laptop)
Proyek ini sudah dilengkapi dengan alur kerja CI/CD di `.github/workflows/build-apk.yml`.
1. Buat repository baru di akun GitHub Anda (misal: `examy`).
2. Masukkan dan push folder `C:\Users\LENOVO\Documents\Examy` ke GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit Examy"
   git branch -M main
   git remote add origin https://github.com/<username-anda>/examy.git
   git push -u origin main
   ```
3. Buka tab **Actions** di repository GitHub Anda.
4. Workflow `Build Examy APK` akan berjalan otomatis dan menghasilkan file **`Examy-Debug-APK`** siap unduh dan langsung bisa diinstal di HP siswa!

### Cara 2: Buka di Android Studio
1. Buka aplikasi **Android Studio**.
2. Pilih **Open** dan arahkan ke folder `C:\Users\LENOVO\Documents\Examy`.
3. Tunggu Gradle Sync selesai.
4. Klik menu **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
5. File APK akan tersimpan di `app/build/outputs/apk/debug/app-debug.apk`.

---

## Panduan Pengawas (Proctor Guide)
- **PIN Bawaan (Default PIN):** `1234`
- **Mengubah PIN:** Pada layar utama sebelum ujian, ketuk ikon roda gigi (Pengaturan) di kanan atas, lalu masukkan PIN baru (minimal 4 angka).
- **Membuka Kunci / Keluar Ujian:** Di dalam sesi ujian, pengawas dapat mengetuk ikon gembok kecil di sudut kanan atas layar atau menekan tombol kembali (*Back*), lalu memasukkan PIN pengawas.

---

## Lisensi & Hak Cipta
Aplikasi ini dibangun menggunakan arsitektur bersih (*Clean-Room Implementation*) secara mandiri. Bebas digunakan, didistribusikan, dan dimodifikasi untuk kebutuhan sekolah atau institusi Anda tanpa royalti maupun perizinan pihak ketiga.
