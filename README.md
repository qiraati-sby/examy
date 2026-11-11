# Examy - Secure & Resilient Exam Browser

[![Download Versi 3 (Terbaru)](https://img.shields.io/badge/Download_Versi_3-Examy--v3.apk-047857?style=for-the-badge&logo=android&logoColor=white)](https://github.com/qiraati-sby/examy/releases/download/v1.0.2/Examy-v3.apk)
[![Release Latest](https://img.shields.io/github/v/release/qiraati-sby/examy?style=for-the-badge&color=blue)](https://github.com/qiraati-sby/examy/releases/latest)
[![Build Status](https://img.shields.io/github/actions/workflow/status/qiraati-sby/examy/build-apk.yml?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/qiraati-sby/examy/actions)
[![Platform](https://img.shields.io/badge/Platform-Android_7.0+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#persyaratan-sistem)

---

**Examy** adalah aplikasi Android *Kiosk Exam Browser* (Exambro) generasi modern yang dirancang untuk memfasilitasi ujian berbasis komputer (CBT / LAN) maupun Google Form secara aman, andal, dan bebas dari kendala umum aplikasi pengunci ujian konvensional. 

Aplikasi ini mengombinasikan perlindungan tingkat sistem operasi Android (*LockTask Mode*), verifikasi volume minimal, pengamanan anti-kecurangan tiga lapis PIN, serta antarmuka yang ramah bagi guru maupun siswa madrasah/sekolah.

---

## 📥 Daftar Unduhan Rilis APK (Langsung Pasang Tanpa ZIP)

Anda dapat mengunduh file `.apk` resmi langsung ke HP Android tanpa perlu mengekstrak file ZIP:

| Versi | Status | Link Unduh Langsung | Catatan Utama |
| :--- | :--- | :--- | :--- |
| **Versi 3 (v1.0.2)** | 🟢 **Terbaru / Rekomendasi** | 📲 **[Unduh Examy-v3.apk](https://github.com/qiraati-sby/examy/releases/download/v1.0.2/Examy-v3.apk)** | **Operator Santri Edition**: Sistem PIN multi-lapis anti-gagal, in-app auto-update checker &amp; installer, jadwal tanggal &amp; jam terpadu, dan riwayat ujian otomatis. |
| **Versi 2 (v1.0.1)** | 🟡 Stabil Sebelumnya | 📲 **[Unduh Examy-v2.apk](https://github.com/qiraati-sby/examy/releases/download/v1.0.1/Examy-v2.apk)** | Ditambahkan Menu Guru (Exam Creator), 3 Lapis PIN, Syarat Volume 80%, dan Alarm Sirene Pelanggaran. |
| **Versi 1 (v1.0.0)** | ⚪ Rilis Dasar (Legacy) | 📲 **[Unduh Examy-v1.apk](https://github.com/qiraati-sby/examy/releases/download/v1.0.0/Examy-v1.apk)** | Versi awal: Kiosk dasar, input URL manual, dan pencegahan tangkapan layar. |

🌐 **[Kunjungi Halaman Rilis GitHub Resmi](https://github.com/qiraati-sby/examy/releases)**

---

## 🌟 Perbandingan Fitur Antar Versi

| Fitur / Kemampuan | Versi 1 (v1.0.0) | Versi 2 (v1.0.1) | **Versi 3 (v1.0.2 - Terbaru)** |
| :--- | :---: | :---: | :---: |
| **Kiosk Fullscreen LockTask &amp; FLAG_SECURE** | ✅ | ✅ | ✅ |
| **Dukungan CBT Server LAN &amp; Google Form** | ✅ | ✅ | ✅ |
| **Injeksi Anti-Copy Teks &amp; ContextMenu** | ✅ | ✅ | ✅ |
| **Menu Konfigurasi Guru (Buat Sesi Ujian)** | ❌ | ✅ | ✅ |
| **Generator Token &amp; Barcode QR (ZXing)** | ❌ | ✅ | ✅ |
| **Penegakan Wajib Volume Minimal 80%** | ❌ | ✅ | ✅ |
| **Sirene Alarm Pelanggaran + Getar Keras** | ❌ | ✅ | ✅ |
| **Grace Period 5 Detik (Anti-False Positive)** | ❌ | ✅ | ✅ |
| **Sistem Tiga Lapis PIN (Siswa, Pengawas, Keluar)** | ❌ | ✅ | ✅ |
| **Jadwal Terpadu (Tanggal Mulai s/d Tanggal Selesai)** | ❌ | ❌ | ✅ |
| **Riwayat Sesi Ujian (Lihat QR, Pakai Lagi, Hapus)** | ❌ | ❌ | ✅ |
| **Label Teks Lengkap Seluruh Menu &amp; Tombol** | ❌ | Parsial | ✅ |
| **Pilihan Tema Warna (Theme Switcher)** | ❌ | ❌ | ✅ |
| **Identitas Resmi "Operator Santri"** | ❌ | ❌ | ✅ |
| **Pengecek &amp; Pemasang Pembaruan Otomatis (Auto-Update)** | ❌ | ❌ | ✅ |

---

## 📖 Panduan Lengkap Penggunaan

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

## 📁 Struktur Direktori Proyek

```
examy/
├── .github/
│   └── workflows/
│       └── build-apk.yml            # CI/CD Otomasi kompilasi APK & rilis GitHub Actions
├── app/
│   ├── build.gradle.kts             # Konfigurasi dependensi modul Android
│   ├── src/main/
│   │   ├── AndroidManifest.xml      # Deklarasi izin kamera, audio, vibrator, kiosk mode
│   │   ├── java/com/examy/app/
│   │   │   ├── MainActivity.kt      # Halaman utama, identitas Operator Santri, scanner QR
│   │   │   ├── CreateExamActivity.kt# Menu guru, jadwal terpadu, riwayat generate, QR generator
│   │   │   ├── ExamActivity.kt      # Layar ujian terkunci, hitung mundur, penanganan sirene
│   │   │   ├── ExamConfig.kt        # Model data sesi ujian (tanggal, jam, token, 3 PIN)
│   │   │   ├── VolumeManager.kt     # Pengawas penegakan volume minimal 80%
│   │   │   ├── AlarmSoundManager.kt # Manajemen audio sirene & vibrator berkelanjutan
│   │   │   ├── ExamWebViewClient.kt # Proteksi anti-copy, bypass SSL LAN, error banner aman
│   │   │   └── SecurityManager.kt   # Kiosk Mode, FLAG_SECURE, & penyimpanan riwayat ujian
│   │   └── res/
│   │       ├── layout/              # Tata letak antarmuka XML (MainActivity, CreateExam, dsb)
│   │       ├── values/              # Warna, tema, dan string multibahasa
│   │       └── drawable/            # Ikon, tombol latar belakang, dan styling visual
├── logo.svg                         # Vektor logo resmi Examy
├── build.gradle.kts                 # Konfigurasi root build Gradle
├── settings.gradle.kts              # Konfigurasi repositori modul Gradle
└── README.md                        # Dokumentasi resmi proyek
```

---

## 🛠️ Kompilasi Mandiri (Build from Source)

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

## 📱 Persyaratan Sistem
- **Sistem Operasi Siswa:** Android 7.0 (Nougat / API level 24) hingga Android 15+.
- **Izin yang Digunakan:** Kamera (untuk pemindai QR Code), Akses Pengaturan Audio & Getar (untuk sirene pelanggaran), dan Layanan Kiosk Pinning (*startLockTask*).

---

## 👥 Pengembang & Hak Cipta
Dikembangkan dengan dedikasi untuk mendukung ekosistem pendidikan madrasah, pesantren, dan sekolah umum di Indonesia.

🇮🇩 **Operator Santri** — *Solusi CBT & Ujian Online yang Aman, Bersahabat, dan Tangguh.*
