# Examy - Secure & Resilient Exam Browser

[![Download APK](https://img.shields.io/badge/Download-Examy.apk-047857?style=for-the-badge&logo=android&logoColor=white)](https://github.com/qiraati-sby/examy/releases/download/v1.0.1/Examy.apk)
[![Release Version](https://img.shields.io/github/v/release/qiraati-sby/examy?style=for-the-badge&color=blue)](https://github.com/qiraati-sby/examy/releases/latest)

ðŸ“¥ **[Unduh File Examy.apk Langsung (Versi 2 - Menu Guru)](https://github.com/qiraati-sby/examy/releases/download/v1.0.1/Examy.apk)**  
ðŸŒ **[Halaman Rilis Publik](https://github.com/qiraati-sby/examy/releases/latest)**

---

**Examy** adalah aplikasi Android Exam Browser (Exambro / Kiosk Browser) yang dirancang khusus untuk memfasilitasi ujian online dan Computer Based Test (CBT) berbasis LAN maupun Google Form secara aman, stabil, dan bebas dari kendala umum aplikasi pengunci ujian konvensional.

---

## Fitur Utama & Keunggulan

| Fitur | Aplikasi Konvensional | **Examy (Aplikasi Ini)** |
| :--- | :--- | :--- |
| **Menu Konfigurasi Ujian (Guru)** | Tidak ada / harus lewat web admin rumit | **Menu Buat Ujian Bawaan**: Input mapel, link Google Form/LAN, jadwal, durasi, dan generate token/QR |
| **Sistem Tiga Lapis PIN** | Hanya 1 PIN statis | **PIN Siswa** (mulai), **PIN Pengawas** (buka pelanggaran & sirene), dan **PIN Keluar** (selesai normal) |
| **Penegakan Volume Minimal 80%** | Tidak ada pengawasan volume | **Syarat Volume Wajib &gt;= 80%**: Aplikasi tidak dapat dimulai dan tidak dapat jalan jika volume &lt; 80% |
| **Sirene Alarm Pelanggaran** | Hanya pesan toast atau langsung close | **Sirene Alarm Kencang + Getar**: Otomatis berbunyi nyaring di HP siswa jika melanggar sampai dibuka PIN Pengawas |
| **Metode Kiosk Lockdown** | Mengandalkan *Accessibility Service* (sering mati di Android 12-15) | **Native `startLockTask()` & `WindowInsetsControllerCompat`** (API resmi Google, 100% stabil) |
| **Toleransi *False Positive*** | Langsung mendiskualifikasi jika ada notifikasi baterai | **Grace Period 5 Detik**: Memberi waktu siswa kembali sebelum sirene dan penguncian aktif |
| **Ketahanan Jaringan LAN** | Halaman hilang / error saat Wi-Fi drop sesaat | **Resilient Non-Destructive Error UI**: Isian formulir siswa tidak terhapus |
| **Penyalinan Jawaban** | Teks soal sering masih bisa diblok | **Injeksi Anti-Copy & Anti-ContextMenu**: Seleksi teks dan klik kanan diblokir otomatis |
| **Input Fleksibel** | Manual ketik URL | **Scanner QR Code bawaan (ZXing)** + Token Generator instan |

---

## Alur Penggunaan Aplikasi

### A. Untuk Guru / Pengawas (Membuat Ujian)
1. Buka aplikasi Examy di HP Anda.
2. Ketuk tombol **"Menu Guru: Buat Sesi Ujian"**.
3. Isi konfigurasi ujian:
   - **Nama Mata Pelajaran** (Cth: *Matematika Wajib - Kelas XII*)
   - **Link Ujian / Google Form** (Tersedia tombol pintas *Gunakan Contoh Link Google Form*)
   - **PIN Utama Siswa** (PIN yang harus dimasukkan siswa di awal sebelum soal terbuka)
   - **PIN Pengawas / Pelanggaran** (PIN rahasia pengawas untuk mematikan sirene jika siswa melanggar)
   - **PIN Keluar** (PIN yang dimasukkan ketika siswa sudah selesai dan izin keluar)
   - **Waktu Mulai & Waktu Selesai** (Dilengkapi *TimePicker* interaktif)
   - **Durasi Pengerjaan** (Dalam hitungan menit, misal 90 menit)
4. Ketuk tombol **"Generate Token & QR Code"**.
5. Sistem akan membuat QR Code dan kode token ujian yang dapat disalin atau dibagikan ke siswa atau proyektor ruangan!

### B. Untuk Siswa (Mengerjakan Ujian)
1. Siswa memastikan volume HP minimal **80%**. Jika kurang dari 80%, aplikasi menampilkan dialog peringatan dan mewajibkan volume dinaikkan.
2. Siswa memindai QR Code pengawas atau menempelkan Token / URL ujian.
3. Aplikasi menampilkan ringkasan ujian (Nama mapel, durasi, status volume) dan meminta **PIN Siswa**.
4. Masukkan PIN Siswa dan ketuk **Mulai Ujian**. Layar akan terkunci penuh (*fullscreen kiosk mode*).

### C. Penanganan Pelanggaran (Siren Alarm)
- Jika siswa mencoba keluar aplikasi, menekan tombol kembali berkali-kali, atau berpindah jendela:
  1. Muncul hitung mundur *Grace Period* 5 detik.
  2. Jika dalam 5 detik siswa tidak kembali ke layar ujian, **Sirene Alarm Nyaring dan Getaran akan berbunyi di volume maksimal!**
  3. Layar berubah menjadi layar merah terkunci (*PELANGGARAN TERDETEKSI*).
  4. Sirene alarm hanya dapat dimatikan jika Pengawas Ruangan memasukkan **PIN Pengawas**.

### D. Mengakhiri Ujian Secara Sah
- Setelah siswa selesai mengirim jawaban formulir ujian, siswa atau pengawas mengetuk ikon gembok di kanan atas atau menekan tombol kembali.
- Masukkan **PIN Keluar** untuk menutup sesi ujian secara bersih.

---

## Struktur Direktori Proyek

```
Examy/
â”œâ”€â”€ .github/
â”‚   â””â”€â”€ workflows/
â”‚       â””â”€â”€ build-apk.yml            # Otomasi kompilasi APK & publikasi rilis di cloud (GitHub Actions)
â”œâ”€â”€ app/
â”‚   â”œâ”€â”€ build.gradle.kts             # Dependensi & konfigurasi modul Android
â”‚   â”œâ”€â”€ src/main/
â”‚   â”‚   â”œâ”€â”€ AndroidManifest.xml      # Izin kamera, audio settings, vibrator, kiosk
â”‚   â”‚   â”œâ”€â”€ java/com/examy/app/
â”‚   â”‚   â”‚   â”œâ”€â”€ MainActivity.kt      # Halaman utama, validasi volume 80%, QR scan, token input
â”‚   â”‚   â”‚   â”œâ”€â”€ CreateExamActivity.kt# Menu guru konfigurasi ujian & generator token/QR
â”‚   â”‚   â”‚   â”œâ”€â”€ ExamActivity.kt      # Layar ujian terkunci, hitung mundur, alarm sirene, PIN keluar
â”‚   â”‚   â”‚   â”œâ”€â”€ ExamConfig.kt        # Model data konfigurasi ujian & enkoder token Base64
â”‚   â”‚   â”‚   â”œâ”€â”€ VolumeManager.kt     # Pengawas volume perangkat (wajib minimal 80%)
â”‚   â”‚   â”‚   â”œâ”€â”€ AlarmSoundManager.kt # Pengelola bunyi sirene alarm keras & getar berkelanjutan
â”‚   â”‚   â”‚   â”œâ”€â”€ ExamWebViewClient.kt # Whitelist domain, anti-copy script, resilient error handler
â”‚   â”‚   â”‚   â””â”€â”€ SecurityManager.kt   # Logika Kiosk, FLAG_SECURE, & verifikasi 3-lapis PIN
â”‚   â”‚   â””â”€â”€ res/                     # Layout XML, dialog, icon, warna, & string multibahasa
â”œâ”€â”€ replica/                         # Catatan audit Clean-Room & matriks fitur lengkap
â””â”€â”€ README.md
```

---

## Kompilasi Otomatis (CI/CD GitHub Actions)

Proyek ini telah dikonfigurasi penuh dengan GitHub Actions di `.github/workflows/build-apk.yml`:
- Setiap kali ada push ke branch `main`, GitHub Actions akan:
  1. Mengompilasi source code menjadi file APK Android.
  2. Otomatis membuat/memperbarui **GitHub Release** dengan file **`Examy.apk` langsung (tanpa ZIP)**.
  3. Menyediakan artefak debug & release.
