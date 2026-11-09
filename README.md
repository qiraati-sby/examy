# Examy - Secure & Resilient Exam Browser

[![Download APK](https://img.shields.io/badge/Download-Examy.apk-047857?style=for-the-badge&logo=android&logoColor=white)](https://github.com/qiraati-sby/examy/releases/download/v1.0.0/Examy.apk)

📥 **[Unduh File Examy.apk (Rilis Terbaru Langsung)](https://github.com/qiraati-sby/examy/releases/download/v1.0.0/Examy.apk)**  
🌐 **[Halaman Rilis Publik](https://github.com/qiraati-sby/examy/releases/latest)**

---

**Examy** adalah aplikasi Android Exam Browser (Exambro / Kiosk Browser) yang dirancang khusus untuk memfasilitasi ujian online dan Computer Based Test (CBT) berbasis LAN maupun Google Form secara aman, ringan, dan bebas dari kendala umum aplikasi pengunci ujian konvensional.

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
2. Ketuk menu **"Menu Guru: Buat Sesi Ujian"**.
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
1. Siswa memastikan volume HP minimal **80%**. Jika kurang dari 80%, aplikasi menampilkan dialog peringatan dan tidak mengizinkan ujian dimulai.
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
├── .github/
│   └── workflows/
│       └── build-apk.yml            # Otomasi kompilasi APK di cloud (GitHub Actions)
├── app/
│   ├── build.gradle.kts             # Dependensi & konfigurasi modul Android
│   ├── src/main/
│   │   ├── AndroidManifest.xml      # Izin kamera, audio settings, vibrator, kiosk
│   │   ├── java/com/examy/app/
│   │   │   ├── MainActivity.kt      # Halaman utama, validasi volume 80%, QR scan, token input
│   │   │   ├── CreateExamActivity.kt# Menu guru konfigurasi ujian & generator token/QR
│   │   │   ├── ExamActivity.kt      # Layar ujian terkunci, hitung mundur, alarm sirene, PIN keluar
│   │   │   ├── ExamConfig.kt        # Model data konfigurasi ujian & enkoder token Base64
│   │   │   ├── VolumeManager.kt     # Pengawas volume perangkat (wajib minimal 80%)
│   │   │   ├── AlarmSoundManager.kt # Pengelola bunyi sirene alarm keras & getar berkelanjutan
│   │   │   ├── ExamWebViewClient.kt # Whitelist domain, anti-copy script, resilient error handler
│   │   │   └── SecurityManager.kt   # Logika Kiosk, FLAG_SECURE, & verifikasi 3-lapis PIN
│   │   └── res/                     # Layout, dialog, icon, warna, & string multibahasa
├── replica/                         # Catatan audit Clean-Room & matriks fitur lengkap
└── README.md
```

---

## Panduan Kompilasi Menjadi APK

### Cara 1: Kompilasi Otomatis via GitHub Actions (Paling Praktis)
Proyek ini sudah dilengkapi dengan alur kerja CI/CD di `.github/workflows/build-apk.yml`.
1. Push folder `C:\Users\LENOVO\Documents\Examy` ke repository GitHub Anda:
   ```bash
   git add .
   git commit -m "Update Examy: Menu Buat Ujian, Volume 80%, 3-Lapis PIN, & Alarm Sirene"
   git push origin main
   ```
2. Buka tab **Actions** di GitHub.
3. Workflow akan otomatis mengompilasi file **`Examy-Debug-APK`** dan **`Examy-Release-APK`** siap pasang di HP siswa!

### Cara 2: Buka di Android Studio
1. Buka aplikasi **Android Studio**.
2. Pilih **Open** dan arahkan ke folder `C:\Users\LENOVO\Documents\Examy`.
3. Tunggu proses Gradle Sync selesai.
4. Klik menu **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
