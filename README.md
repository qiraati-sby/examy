# Examy - Secure & Resilient Exam Browser

[![Download Versi 3 (Terbaru)](https://img.shields.io/badge/Download_Versi_3-Examy--v3.apk-047857?style=for-the-badge&logo=android&logoColor=white)](https://github.com/qiraati-sby/examy/releases/download/v1.0.2/Examy-v3.apk)
[![Release Latest](https://img.shields.io/github/v/release/qiraati-sby/examy?style=for-the-badge&color=blue)](https://github.com/qiraati-sby/examy/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/qiraati-sby/examy/build-apk.yml?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/qiraati-sby/examy/actions)
[![Platform](https://img.shields.io/badge/Platform-Android_7.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#persyaratan-sistem)

---

**Examy** adalah aplikasi Android *Kiosk Exam Browser* (Exambro) generasi modern yang dirancang untuk memfasilitasi ujian berbasis komputer (CBT / LAN) maupun Google Form secara aman, andal, dan bebas dari kendala umum aplikasi pengunci ujian konvensional. 

Aplikasi ini mengombinasikan perlindungan tingkat sistem operasi Android (*LockTask Mode*), verifikasi volume minimal, pengamanan anti-kecurangan tiga lapis PIN, serta antarmuka yang ramah bagi guru maupun siswa madrasah/sekolah.

---

## ðŸ“¥ Daftar Unduhan Rilis APK (Langsung Pasang Tanpa ZIP)

Anda dapat mengunduh file `.apk` resmi langsung ke HP Android tanpa perlu mengekstrak file ZIP:

| Versi | Status | Link Unduh Langsung | Catatan Utama |
| :--- | :--- | :--- | :--- |
| **Versi 3 (v1.0.2)** | ðŸŸ¢ **Terbaru / Rekomendasi** | ðŸ“² **[Unduh Examy-v3.apk](https://github.com/qiraati-sby/examy/releases/download/v1.0.2/Examy-v3.apk)** | **Operator Santri Edition**: Jadwal tanggal &amp; jam terpadu, riwayat sesi ujian otomatis, label menu lengkap, bebas redundansi. |
| **Versi 2 (v1.0.1)** | ðŸŸ¡ Stabil Sebelumnya | ðŸ“² **[Unduh Examy-v2.apk](https://github.com/qiraati-sby/examy/releases/download/v1.0.1/Examy.apk)** | Ditambahkan Menu Guru (Exam Creator), 3 Lapis PIN, Syarat Volume 80%, dan Alarm Sirene Pelanggaran. |
| **Versi 1 (v1.0.0)** | âšª Rilis Dasar (Legacy) | ðŸ“² **[Unduh Examy-v1.apk](https://github.com/qiraati-sby/examy/releases/download/v1.0.0/Examy.apk)** | Versi awal: Kiosk dasar, input URL manual, dan pencegahan tangkapan layar. |

ðŸŒ **[Kunjungi Halaman Rilis GitHub Resmi](https://github.com/qiraati-sby/examy/releases)**

---

## ðŸŒŸ Perbandingan Fitur Antar Versi

| Fitur / Kemampuan | Versi 1 (v1.0.0) | Versi 2 (v1.0.1) | **Versi 3 (v1.0.2 - Terbaru)** |
| :--- | :---: | :---: | :---: |
| **Kiosk Fullscreen LockTask &amp; FLAG_SECURE** | âœ… | âœ… | âœ… |
| **Dukungan CBT Server LAN &amp; Google Form** | âœ… | âœ… | âœ… |
| **Injeksi Anti-Copy Teks &amp; ContextMenu** | âœ… | âœ… | âœ… |
| **Menu Konfigurasi Guru (Buat Sesi Ujian)** | âŒ | âœ… | âœ… |
| **Generator Token &amp; Barcode QR (ZXing)** | âŒ | âœ… | âœ… |
| **Penegakan Wajib Volume Minimal 80%** | âŒ | âœ… | âœ… |
| **Sirene Alarm Pelanggaran + Getar Keras** | âŒ | âœ… | âœ… |
| **Grace Period 5 Detik (Anti-False Positive)** | âŒ | âœ… | âœ… |
| **Sistem Tiga Lapis PIN (Siswa, Pengawas, Keluar)** | âŒ | âœ… | âœ… |
| **Jadwal Terpadu (Tanggal Mulai s/d Tanggal Selesai)** | âŒ | âŒ | âœ… |
| **Riwayat Sesi Ujian (Lihat QR, Pakai Lagi, Hapus)** | âŒ | âŒ | âœ… |
| **Label Teks Lengkap Seluruh Menu &amp; Tombol** | âŒ | Parsial | âœ… |
| **Identitas Resmi "Operator Santri"** | âŒ | âŒ | âœ… |

---

## ðŸ“– Panduan Lengkap Penggunaan

### 1. Peran Guru / Pengawas (Membuat &amp; Mengelola Sesi Ujian)
1. Buka aplikasi **Examy** pada ponsel pengawas/guru.
2. Ketuk menu **"Menu Guru: Buat Sesi Ujian"**.
3. Lengkapi formulir konfigurasi ujian:
   - **Informasi Ujian:** Masukkan nama mata pelajaran dan tautan formulir (Google Form atau IP LAN sekolah).
   - **Pengaturan Tiga Lapis PIN:**
     - *PIN Siswa*: Diberikan kepada siswa untuk membuka soal di awal.
     - *PIN Pengawas*: PIN rahasia untuk membuka kunci layar dan mematikan sirene jika siswa melanggar.
     - *PIN Keluar*: PIN resmi untuk mengakhiri sesi ujian saat siswa telah selesai.
   - **Jadwal &amp; Waktu Terpadu:**
     - Pilih **Tanggal Mulai** dan **Jam Mulai** melalui pemilih tanggal/jam interaktif.
     - Pilih **Tanggal Selesai** dan **Jam Selesai**.
     - Tentukan **Durasi Pengerjaan** (misal 90 menit).
4. Tekan tombol **"Generate Token &amp; QR Code"**.
5. Pop-up barcode QR Code dan kode token ujian instan akan ditampilkan. Guru dapat menampilkan QR Code ke proyektor atau membagikan tokennya ke siswa.
6. **Riwayat Sesi Ujian:** Semua sesi yang dibuat otomatis tersimpan di bagian *Riwayat Sesi Ujian*. Guru dapat menekan tombol **"Lihat QR &amp; Token"** kapan saja untuk menampilkan ulang barcode tanpa harus mengisi ulang data.

### 2. Peran Siswa (Mengerjakan Ujian)
1. **Pemeriksaan Volume HP:** Pastikan volume media HP siswa berada pada posisi minimal **80%**. Jika kurang, aplikasi memunculkan panduan untuk menaikkan volume sebelum ujian diizinkan dimulai.
2. **Masuk Sesi Ujian:** Siswa memilih salah satu cara:
   - Ketuk **Pindai QR Code** dan arahkan kamera ke barcode pengawas, atau
   - Tempelkan kode token / ketik URL ujian secara manual.
3. **Konfirmasi &amp; PIN Siswa:** Aplikasi menampilkan ringkasan ujian (Mata pelajaran, jadwal tanggal &amp; jam, durasi). Siswa memasukkan **PIN Siswa** lalu menekan **Mulai Ujian**.
4. **Kiosk Mode Terkunci:** Seluruh tombol navigasi, notifikasi, screenshot, dan perekam layar otomatis terkunci. Siswa mengerjakan soal dengan tenang tanpa gangguan.

### 3. Penanganan Pelanggaran (Grace Period &amp; Sirene Keras)
- Jika siswa mencoba menekan tombol kembali, membuka aplikasi lain, atau beralih jendela:
  1. Muncul hitung mundur **Grace Period 5 Detik**.
  2. Jika siswa kembali ke layar ujian dalam 5 detik, peringatan otomatis dibatalkan (mencegah diskualifikasi tidak sengaja akibat notifikasi baterai).
  3. Jika siswa tidak kembali setelah 5 detik, **Sirene Alarm Nyaring dan Getaran Maksimal** langsung berbunyi di HP siswa dan layar berubah merah (*PELANGGARAN TERDETEKSI*).
  4. Sirene dan kunci pelanggaran **hanya dapat dimatikan jika Pengawas Ruangan memasukkan PIN Pengawas**.

### 4. Mengakhiri Sesi Ujian
- Setelah selesai mengirimkan formulir (*Submit Google Form / CBT*), siswa melapor ke pengawas.
- Pengawas atau siswa mengetuk tombol **"Keluar"** di kanan atas layar ujian, lalu memasukkan **PIN Keluar**.
- Sesi terkunci dihentikan secara bersih dan aplikasi kembali ke halaman beranda.

---

## ðŸ“ Struktur Direktori Proyek

```
examy/
â”œâ”€â”€ .github/
â”‚   â””â”€â”€ workflows/
â”‚       â””â”€â”€ build-apk.yml            # CI/CD Otomasi kompilasi APK & rilis GitHub Actions
â”œâ”€â”€ app/
â”‚   â”œâ”€â”€ build.gradle.kts             # Konfigurasi dependensi modul Android
â”‚   â”œâ”€â”€ src/main/
â”‚   â”‚   â”œâ”€â”€ AndroidManifest.xml      # Deklarasi izin kamera, audio, vibrator, kiosk mode
â”‚   â”‚   â”œâ”€â”€ java/com/examy/app/
â”‚   â”‚   â”‚   â”œâ”€â”€ MainActivity.kt      # Halaman utama, identitas Operator Santri, scanner QR
â”‚   â”‚   â”‚   â”œâ”€â”€ CreateExamActivity.kt# Menu guru, jadwal terpadu, riwayat generate, QR generator
â”‚   â”‚   â”‚   â”œâ”€â”€ ExamActivity.kt      # Layar ujian terkunci, hitung mundur, penanganan sirene
â”‚   â”‚   â”‚   â”œâ”€â”€ ExamConfig.kt        # Model data sesi ujian (tanggal, jam, token, 3 PIN)
â”‚   â”‚   â”‚   â”œâ”€â”€ VolumeManager.kt     # Pengawas penegakan volume minimal 80%
â”‚   â”‚   â”‚   â”œâ”€â”€ AlarmSoundManager.kt # Manajemen audio sirene & vibrator berkelanjutan
â”‚   â”‚   â”‚   â”œâ”€â”€ ExamWebViewClient.kt # Proteksi anti-copy, bypass SSL LAN, error banner aman
â”‚   â”‚   â”‚   â””â”€â”€ SecurityManager.kt   # Kiosk Mode, FLAG_SECURE, & penyimpanan riwayat ujian
â”‚   â”‚   â””â”€â”€ res/
â”‚   â”‚       â”œâ”€â”€ layout/              # Tata letak antarmuka XML (MainActivity, CreateExam, dsb)
â”‚   â”‚       â”œâ”€â”€ values/              # Warna, tema, dan string multibahasa
â”‚   â”‚       â””â”€â”€ drawable/            # Ikon, tombol latar belakang, dan styling visual
â”œâ”€â”€ logo.svg                         # Vektor logo resmi Examy
â”œâ”€â”€ build.gradle.kts                 # Konfigurasi root build Gradle
â”œâ”€â”€ settings.gradle.kts              # Konfigurasi repositori modul Gradle
â””â”€â”€ README.md                        # Dokumentasi resmi proyek
```

---

## ðŸ› ï¸ Kompilasi Mandiri (Build from Source)

### Menggunakan GitHub Actions CI/CD (Otomatis)
Setiap kali ada pembaruan kode yang di-push ke branch `main`, GitHub Actions akan otomatis:
1. Memeriksa integritas dependensi dan mengompilasi APK debug & release.
2. Memublikasikan file APK langsung (*uncompressed `.apk`*) ke halaman GitHub Releases.

### Menggunakan Android Studio (Lokal)
1. Kloning atau buka direktori proyek ini di **Android Studio**.
2. Sinkronkan Gradle (*Sync Project with Gradle Files*).
3. Pilih menu **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
4. File output APK siap digunakan di `app/build/outputs/apk/debug/`.

---

## ðŸ“± Persyaratan Sistem
- **Sistem Operasi Siswa:** Android 7.0 (Nougat / API level 24) hingga Android 15+.
- **Izin yang Digunakan:** Kamera (untuk pemindai QR Code), Akses Pengaturan Audio & Getar (untuk sirene pelanggaran), dan Layanan Kiosk Pinning (*startLockTask*).

---

## ðŸ‘¥ Pengembang & Hak Cipta
Dikembangkan dengan dedikasi untuk mendukung ekosistem pendidikan madrasah, pesantren, dan sekolah umum di Indonesia.

ðŸ‡®ðŸ‡© **Operator Santri** â€” *Solusi CBT & Ujian Online yang Aman, Bersahabat, dan Tangguh.*
