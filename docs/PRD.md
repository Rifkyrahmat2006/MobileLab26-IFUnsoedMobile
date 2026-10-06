# PRD — GempaKini

## 1. Latar Belakang
Indonesia di cincin api, gempa sering terjadi. User butuh cara cepat lihat & cari info gempa terkini dari HP. GempaKini ambil data real-time dari BMKG dan tampilkan dalam list yang bisa dicari.

## 2. Tujuan
- Tampilkan daftar gempa terkini dari API BMKG.
- User bisa cari gempa berdasarkan wilayah.
- User bisa lihat detail lengkap satu gempa.
- Tugas kuliah: terapkan Kotlin modern + Jetpack Compose + MVVM sesuai rubrik responsi Mobile Programming.

## 3. Target User
Mahasiswa/dosen penilai (demo akademik), siapa pun yang ingin pantau gempa terkini tanpa buka web BMKG.

## 4. Scope
### In scope
- 2 screen: Home (list + search) dan Detail.
- Fetch data sekali dari endpoint BMKG saat Home dibuka, filter search dilakukan lokal (bukan re-fetch).
- Loading / Error / Success state eksplisit.
- Tanpa gambar/icon gempa (dilarang lib image).

### Out of scope
- Peta/lokasi visual (tidak wajib, lib image dilarang jadi tidak ada icon).
- Notifikasi push, background service.
- Autentikasi user.
- Penyimpanan lokal/cache persist (DB/DataStore) — tidak diminta, tidak dibuat (YAGNI).

## 5. User Stories
1. Sebagai user, saya buka app dan langsung lihat daftar gempa terbaru tanpa setup apa pun.
2. Sebagai user, saya ketik nama wilayah di search bar dan list langsung terfilter.
3. Sebagai user, saat data masih dimuat saya lihat indikator loading, bukan list kosong.
4. Sebagai user, jika gagal ambil data (no internet/API error) saya lihat pesan error, bukan crash atau list kosong tanpa penjelasan.
5. Sebagai user, saya klik salah satu gempa di list dan masuk ke halaman detail (tanggal, jam, koordinat, magnitudo, kedalaman, wilayah, potensi).
6. Sebagai user, dari detail saya bisa tombol back ke list.

## 6. Functional Requirements
| ID | Requirement |
|----|-------------|
| FR-1 | App fetch JSON dari `https://data.bmkg.go.id/DataMKG/TEWS/gempaterkini.json` via Retrofit saat Home Screen pertama dibuka |
| FR-2 | List gempa ditampilkan dengan LazyColumn, minimal kolom: Tanggal, Magnitudo, Wilayah |
| FR-3 | Search bar filter list berdasarkan substring Wilayah, case-insensitive, real-time (tanpa network call) |
| FR-4 | State Loading ditampilkan selagi fetch berjalan |
| FR-5 | State Error ditampilkan jika fetch gagal, dengan pesan error |
| FR-6 | Klik item list navigasi ke Detail Screen dengan data gempa terkait |
| FR-7 | Detail Screen tampilkan: Tanggal, Jam, Coordinates, Magnitudo, Kedalaman, Wilayah, Potensi + tombol back |

## 7. Non-Functional Requirements
- Bahasa: Kotlin, wajib pakai data class, null safety, lambda; extension function jika relevan.
- UI: 100% Jetpack Compose, Material Design 3, Scaffold + TopAppBar, reusable composable.
- Architecture: MVVM ketat (Composable → ViewModel → Repository → API Service → Model), sealed `UiState` (Loading/Success/Error) via StateFlow.
- Dependency dibatasi: `retrofit`, `converter-gson`, `navigation-compose`, `lifecycle-viewmodel-compose` saja (+ bundle Compose/Material3 default template). Tidak boleh Hilt/Koin/Coil/Glide/dsb.
- Permission: `INTERNET` di AndroidManifest.
- Tidak ada call API langsung dari Composable — wajib lewat ViewModel.

## 8. Deliverable (sesuai soal)
- GitHub repository.
- README.md: screenshot 2 screen, daftar fitur, penjelasan architecture, API, poin teknis.
- APK debug.
- Video penjelasan kode (fokus code walkthrough, bukan demo pakai app).
- Submit link GitHub + video via form yang disediakan.

## 9. Kriteria Sukses
- Build jalan tanpa error, APK debug terinstall & fetch data sukses di device/emulator ber-internet.
- Semua FR di atas terverifikasi manual.
- Tidak ada pelanggaran batasan teknis (lib, image, call API di Composable, dst) — ini poin rubrik, bukan opsional.
