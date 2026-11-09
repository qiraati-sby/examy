# Build Log - Examy

Detailed screen-by-screen clean-room build tracking for the Examy kiosk browser application.

---

## Screen Inventory & Implementation Status

| ID | Screen / Activity | Route / Intent | Purpose | Key Components | Definition of Done | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **S01** | `MainActivity` | `android.intent.action.MAIN` | Siswa entry point, validasi volume >=80%, input URL/Token, QR Scanner | VolumeStatusCard, UrlTokenInput, ScanQrButton, RecentChips, ConfirmModal | [x] All states handled, no crash, 100% accessible | **COMPLETE** |
| **S02** | `CreateExamActivity` | Explicit Intent dari Menu Guru | Konfigurasi ujian oleh guru/pengawas, generator token & QR | FormInputs, QuickGoogleFormButton, TimePickers, 3TierPinInputs, QrModal | [x] Interactive dialogs, Base64 encoding, ZXing QR generation | **COMPLETE** |
| **S03** | `ExamActivity` | Intent dengan `ExamConfig` payload | Sesi ujian terlindungi (Kiosk Mode, WebView, Siren Alarm) | KioskController, ResilientWebView, CountdownHeader, SirenOverlay, PinModal | [x] Strict Kiosk, FLAG_SECURE, non-destructive network error UI, alarm | **COMPLETE** |
| **S04** | `SettingsDialog` | Floating Dialog dari `MainActivity` | Pengaturan PIN default pengawas & preferensi aplikasi | Encrypted SharedPreferences, PinInput, SaveButton | [x] Safe persistent storage, input validation | **COMPLETE** |

---

## Detailed Screen Build Verification

### 1. S01 - MainActivity (Siswa Launcher)
- **States Handled**:
  - `Empty`: Input URL kosong -> Validasi prompt muncul sebelum proses scan/submit.
  - `Low Volume (< 80%)`: Menampilkan dialog peringatan volume wajib >= 80% dengan indikator progres real-time. Tombol "Mulai Ujian" dinonaktifkan sampai volume dinaikkan.
  - `Valid Volume (>= 80%)`: Indikator hijau aktif, tombol "Mulai Ujian" dan "Pindai QR" siap digunakan.
  - `Token Encoded`: Mendeteksi string prefix `EXAMY:`, otomatis mengekstrak Base64 JSON dan menampilkan dialog konfirmasi ujian (Mapel, Durasi, Jadwal) serta meminta PIN Siswa.
  - `Direct URL / LAN IP`: Mendeteksi format IP lokal (`192.168.x.x` / `10.x.x.x`) atau Google Form dan menambahkan skema `http://` / `https://` otomatis.
- **Commit**: `build: S01 MainActivity with real-time volume enforcement and QR scanner`

### 2. S02 - CreateExamActivity (Teacher Console)
- **States Handled**:
  - `Default/Initial`: Formulir bersih dengan tombol "Gunakan Contoh Google Form" untuk pengisian cepat.
  - `Time Picker Interaction`: Dialog `TimePickerDialog` native untuk menentukan jam mulai dan jam selesai ujian secara presisi.
  - `3-Tier PIN Configuration`: Input PIN Siswa, PIN Pengawas (default 1234), dan PIN Keluar (default 1234).
  - `QR & Token Modal`: Menghasilkan QR Code bitmap 512x512 via ZXing Embedded dan token teks ringkas `EXAMY:<Base64>` yang dilengkapi tombol salin ke clipboard.
- **Commit**: `build: S02 CreateExamActivity teacher exam configuration and token generator`

### 3. S03 - ExamActivity (Kiosk Lockdown & Siren Engine)
- **States Handled**:
  - `Kiosk Active`: Mengaktifkan `startLockTask()`, menyembunyikan status bar dan navigation bar (`WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`), serta memasang `FLAG_SECURE` untuk memblokir screenshot dan screen recording.
  - `WebView Loading / Progress`: Horizontal progress bar di bagian atas.
  - `Network Drop / LAN Error`: Mencegah WebView menampilkan halaman error bawaan Google Chrome ("ERR_NAME_NOT_RESOLVED") yang merusak form isian siswa. Menggantinya dengan non-destructive retry banner sehingga jawaban form siswa tetap aman tersimpan di DOM/form cache.
  - `Grace Period (5 Detik)`: Muncul saat siswa berpindah aplikasi atau kehilangan fokus layar. Memberikan jeda 5 detik bagi siswa untuk kembali.
  - `Violation Triggered (Sirene Aktif)`: Layar seketika berubah merah, memutar ringtone alarm terus-menerus di level audio maksimal serta pola getar 1000ms. Hanya bisa dimatikan dengan **PIN Pengawas**.
  - `Normal Exit`: Tombol gembok di header memunculkan dialog **PIN Keluar**. Jika PIN cocok, sesi kiosk diakhiri (`stopLockTask()`) dan kembali ke menu utama.
- **Commit**: `build: S03 ExamActivity resilient kiosk lockdown with siren and dual PIN unlock`

---

## Definition of Done Verification
- [x] Every state from the recon map handled (empty, loading, filled, error, kiosk locked).
- [x] Responsive layout across portrait, tablet, and widescreen orientations.
- [x] Clean-room implementation: Zero lines copied from proprietary sources.
- [x] All typography and color references mapped to `replica/design/tokens.json`.
- [x] No memory leaks on AudioManager or RingtoneManager lifecycle.
