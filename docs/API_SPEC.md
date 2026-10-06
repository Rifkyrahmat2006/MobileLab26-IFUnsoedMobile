# API_SPEC.md — BMKG Gempa Terkini

## Endpoint
```
GET https://data.bmkg.go.id/DataMKG/TEWS/gempaterkini.json
```
- Base URL Retrofit: `https://data.bmkg.go.id/`
- Path: `DataMKG/TEWS/gempaterkini.json`
- Tanpa API key, tanpa header khusus.
- Method: GET, no params.

## Contoh Response (ringkas, field relevan)
```json
{
  "Infogempa": {
    "gempa": [
      {
        "Tanggal": "06 Okt 2026",
        "Jam": "14:30:00 WIB",
        "DateTime": "2026-10-06T07:30:00+00:00",
        "Coordinates": "-7.50,112.30",
        "Lintang": "7.5 LS",
        "Bujur": "112.3 BT",
        "Magnitude": "4.5",
        "Kedalaman": "10 km",
        "Wilayah": "Kab. Malang",
        "Potensi": "Tidak berpotensi tsunami",
        "Dirasakan": "-",
        "Shakemap": "20261006143000.mmi.jpg"
      }
    ]
  }
}
```

## Field yang dipakai app
| Field JSON | Dipakai di | Tipe |
|---|---|---|
| Tanggal | List + Detail | String |
| Jam | Detail | String |
| Coordinates | Detail | String |
| Magnitude | List + Detail | String |
| Kedalaman | Detail | String |
| Wilayah | List + Detail + Search filter | String |
| Potensi | Detail | String |

Field `Dirasakan`, `Shakemap`, `Lintang`, `Bujur` tidak diminta FR — tetap boleh dimasukkan ke data class (gson ignore field tak dipakai) tapi tidak wajib ditampilkan di UI.

## Catatan Teknis
- Cek dulu response asli (`curl` endpoint di atas) sebelum coding, karena BMKG kadang ubah struktur tanpa notice.
- Semua value JSON berupa String (termasuk Magnitude angka) — data class pakai `String`, bukan `Double`/`Float`, supaya tidak ada parse exception.
- Tidak butuh `@SerializedName` kalau nama property Kotlin ditulis exact sama dengan key JSON (case-sensitive, huruf besar di awal).
- Request ini HTTP GET biasa tanpa auth — kalau gagal, kemungkinan besar network/DNS/timeout, bukan auth error.

## Error Case yang Perlu Ditangani
| Kasus | Sumber | Handling |
|---|---|---|
| Tidak ada internet | `IOException` dari Retrofit/OkHttp | Tangkap di Repository/ViewModel, UiState.Error("Tidak ada koneksi internet") |
| BMKG down / response invalid | `HttpException` / `JsonSyntaxException` | UiState.Error("Gagal memuat data gempa") |
| List kosong (edge case jarang) | Response sukses tapi `gempa` kosong | Tampilkan "Tidak ada data gempa" di LazyColumn (empty state), bukan dianggap error |
