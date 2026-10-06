# CHECKLIST.md — Validasi Rubrik Responsi

Dicek manual sebelum submit. Centang kalau sudah terpenuhi & terbukti di kode.

## 1. Bahasa Pemrograman
- [ ] Minimal 1 `data class` dipakai (model Gempa/GempaResponse)
- [ ] Null safety dimanfaatkan (hindari `!!`, pakai `?:`, `?.`, non-null by default dari API)
- [ ] Minimal 1 lambda eksplisit terlihat (onClick, onQueryChange, dsb — otomatis ada di Compose)
- [ ] Extension function dipakai kalau ada kasus relevan (opsional, jangan dipaksa kalau tidak natural)

## 2. User Interface
- [ ] 100% Jetpack Compose, tidak ada XML layout manual (kecuali resource bawaan template: themes.xml, colors.xml default)
- [ ] Composable layout jelas terpisah per screen/komponen
- [ ] LazyColumn dipakai untuk list gempa
- [ ] Ada minimal 1 reusable composable (GempaListItem / SearchBar dipanggil >1 tempat atau didesain reusable)
- [ ] Material Design 3 (`MaterialTheme`, `Scaffold`, `TopAppBar` dari `androidx.compose.material3`)
- [ ] `Color.kt` dimodifikasi (bukan default generate Android Studio)
- [ ] `Theme.kt` punya Light ColorScheme & Dark ColorScheme custom
- [ ] `Type.kt` override minimal 2 text style
- [ ] `Scaffold` + `TopAppBar` dipakai di Home dan Detail

## 3. List & Data
- [ ] List pakai `LazyColumn` saja (bukan `Column` + `forEach` manual, bukan `RecyclerView`)
- [ ] Data benar dari API (bukan hardcode/dummy)
- [ ] Minimal info per item: Tanggal, Magnitudo, Wilayah
- [ ] Tidak ada Image/Icon gambar gempa di list (tidak import Coil/Glide sama sekali — cek `build.gradle.kts`)

## 4. State & Recomposition
- [ ] UI state-driven (`uiState` dari StateFlow, bukan var biasa di Composable)
- [ ] Search filter lokal berfungsi (tidak hit API lagi saat mengetik)
- [ ] Loading state tampil saat fetch
- [ ] Error state tampil saat fetch gagal (coba matikan WiFi buat tes manual)
- [ ] UI berubah otomatis sesuai state (recomposition, tidak perlu manual refresh)

## 5. Networking
- [ ] `INTERNET` permission ada di `AndroidManifest.xml`
- [ ] Retrofit + `converter-gson` dipakai, base URL `https://data.bmkg.go.id`
- [ ] Endpoint persis `/DataMKG/TEWS/gempaterkini.json`
- [ ] Data minimal terambil: Tanggal, Jam, Coordinates, Magnitude, Kedalaman, Wilayah, Potensi
- [ ] Dependency HANYA: retrofit, converter-gson, navigation-compose, lifecycle-viewmodel-compose (+ default template compose/material3) — cek `libs.versions.toml` & `build.gradle.kts`, tidak ada Hilt/Koin/Coil/Glide/OkHttp logging lib tambahan dsb

## 6. Architecture (MVVM)
- [ ] Ada layer: Composable (View) / ViewModel / Repository / API Service / Data Model — terpisah file/class, bukan campur jadi satu
- [ ] Sealed `UiState` (Loading/Success/Error) via `StateFlow`
- [ ] TIDAK ADA call Retrofit langsung di dalam Composable — grep `RetrofitInstance` atau `api.` di file `*Screen.kt`, harus nol hasil

## 7. Screens
- [ ] Maks 2 screen, pakai `navigation-compose` (`NavHost`, `composable(route)`)
- [ ] Home: judul app, search bar, list (Tanggal+Magnitudo+Wilayah), klik item → Detail
- [ ] Detail: Tanggal, Jam, Coordinates, Magnitudo, Kedalaman, Wilayah, Potensi + tombol back berfungsi

## 8. Submission
- [ ] Repo GitHub dibuat, code pushed
- [ ] `README.md` berisi: screenshot Home, screenshot Detail, daftar fitur, penjelasan architecture (MVVM diagram/teks), penjelasan API (endpoint + struktur), poin teknis (Kotlin features dipakai)
- [ ] APK debug di-build (`./gradlew assembleDebug`) dan disertakan/link download
- [ ] Video penjelasan kode direkam — fokus WALKTHROUGH CODE (baca & jelaskan logic), BUKAN demo pakai app
- [ ] Link GitHub + link video disubmit lewat form yang disediakan, sebelum deadline 24 jam setelah shift

## Cara Cek Cepat (grep manual)
```bash
# cek tidak ada call API di Composable
grep -rn "RetrofitInstance\|\.api\." app/src/main/java --include="*Screen.kt"

# cek dependency terlarang
grep -n "coil\|glide\|hilt\|koin" app/build.gradle.kts gradle/libs.versions.toml

# cek permission internet
grep -n "INTERNET" app/src/main/AndroidManifest.xml
```
