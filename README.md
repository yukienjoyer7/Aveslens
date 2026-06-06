# AvesLens

Aplikasi jurnal pengamatan burung untuk Android dengan klasifikasi spesies berbasis AI.

## Fitur

- **Catat Pengamatan** — Foto, nama spesies, lokasi, dan catatan lapangan
- **Klasifikasi AI** — Identifikasi otomatis spesies burung dari foto menggunakan machine learning
- **Jurnal Pribadi** — Riwayat semua pengamatan dengan pencarian dan filter
- **Explorer** — Daftar spesies unik yang pernah ditemukan beserta jumlah sightings
- **Profil** — Statistik pengamatan dan pengaturan akun

## Teknologi

- Kotlin + Jetpack Compose (Material 3)
- Supabase (Auth, Database, Storage)
- Hilt (Dependency Injection)
- Coil (Image Loading)
- OkHttp (ML API calls)

## Setup

1. Clone repo ini
2. Buat file `local.properties` di root project dan tambahkan:
   ```
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   AVESLENS_MODEL_URL=your_ml_model_url
   ```
3. Build dan jalankan di Android Studio

> **Catatan:** File `local.properties` tidak di-commit ke git. Jangan bagikan API key kamu.

## Lisensi

MIT
