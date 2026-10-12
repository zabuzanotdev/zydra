<p align="center">
  <img src="https://img.shields.io/badge/status-dalam%20pengembangan-orange" alt="Status: dalam pengembangan">
  <img src="https://img.shields.io/badge/lisensi-GPL--3.0--or--later-blue" alt="Lisensi: GPL-3.0 atau lebih baru">
</p>

# Zydra

Aplikasi Android untuk **memulihkan password file terenkripsi** dengan serangan wordlist (kamus): ZIP, RAR, PDF, dan file `shadow` Unix. Versi ini adalah port Android native (Kotlin + Jetpack Compose) dari skrip Python [Zydra](https://github.com/hamedA2/Zydra).

> Gunakan hanya untuk file milik sendiri (misalnya lupa password), audit keamanan dengan izin, atau latihan CTF. Jangan memakai tool ini terhadap file milik orang lain tanpa izin.

## Fitur

- **ZIP**: mencoba kandidat password dari wordlist memakai Zip4j (termasuk arsip AES).
- **RAR**: lewat Junrar (fokus dukungan RAR v4).
- **PDF**: lewat PdfBox-Android, mencoba memuat dokumen dengan setiap kandidat password.
- **Shadow file Unix**: verifikasi hash kandidat memakai `MessageDigest` (terdeteksi dari nama file yang memuat "shadow").
- **UI Compose bertema matrix** (hijau di atas hitam): pilih file target, pilih wordlist `.txt`, mulai proses, lalu lihat progress, kecepatan (p/s), dan ETA. Hasil tampil sebagai "ACCESS GRANTED" beserta password yang ditemukan.
- Proses berjalan di coroutine sehingga UI tetap responsif, dan bisa dihentikan di tengah jalan.

Catatan status: serangan yang tersedia di UI saat ini adalah **wordlist**. Generator brute-force charset sudah ada di kode (`BruteForceGenerator.kt`) tetapi belum dihubungkan ke UI.

## Cara pakai

1. Siapkan wordlist berisi satu kandidat password per baris (file `.txt`).
2. Buka Zydra, pilih **TARGET ENCRYPTED FILE**.
3. Pilih **WORDLIST (.TXT)**.
4. Tekan **INITIATE DECRYPTION**.
5. Jika cocok, password muncul di panel hasil.

## Build

Persyaratan: JDK 17 dan Android SDK 34 (platform + build tools). Gradle wrapper disertakan (Gradle 8.4, diunduh otomatis saat build pertama).

Lewat CLI:

```bash
# Windows
gradlew.bat :app:assembleDebug

# macOS / Linux
./gradlew :app:assembleDebug
```

Atau buka folder proyek ini di Android Studio (proyek dibuat dengan AGP 8.1.0 dan Kotlin 1.8.10).

`local.properties` harus berisi `sdk.dir` menuju Android SDK Anda, dan file itu tidak ikut di repositori ini.

## Struktur

```
app/src/main/java/com/zydra/
├── MainActivity.kt
├── cracker/     ZipCracker (Zip4j), RarCracker (Junrar), PdfCracker (PdfBox),
│                ShadowCracker (MessageDigest), BruteForceGenerator, CrackerFactory
└── ui/          CrackerScreen (Compose), CrackerViewModel
```

## Lisensi & kredit

- Port Android dan modifikasi: **GPL-3.0-or-later**, Copyright (C) 2026 zabuzanotdev. Teks lengkap di [`LICENSE`](LICENSE).
- Proyek asli: [hamedA2/Zydra](https://github.com/hamedA2/Zydra) (Python, lisensi MIT, Copyright (c) 2019 hamedA2). Notice MIT dipertahankan di [`LICENSE-MIT-Zydra.txt`](LICENSE-MIT-Zydra.txt). MIT kompatibel dengan GPL-3.0, sehingga karya turunan ini didistribusikan di bawah GPL dengan kredit asli tetap disertakan.
