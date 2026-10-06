# Design.md — GempaKini

## 1. Package Structure
```
com.example.gempakini
├── MainActivity.kt
├── ui/
│   ├── theme/                  (Color.kt, Theme.kt, Type.kt — sudah ada, akan dimodifikasi)
│   ├── navigation/
│   │   └── NavGraph.kt         (navigation-compose: route Home / Detail)
│   ├── home/
│   │   ├── HomeScreen.kt       (Composable: Scaffold, TopAppBar, SearchBar, LazyColumn)
│   │   └── HomeViewModel.kt
│   └── detail/
│       ├── DetailScreen.kt     (Composable: Scaffold, TopAppBar back button)
│       └── (pakai HomeViewModel yang sama, lihat §4)
├── data/
│   ├── model/
│   │   ├── GempaResponse.kt    (Infogempa, GempaShout model)
│   │   └── GempaUi.kt          (opsional: model ringkas utk UI jika perlu)
│   ├── remote/
│   │   ├── BmkgApiService.kt   (Retrofit interface)
│   │   └── RetrofitInstance.kt (object Retrofit singleton)
│   └── repository/
│       └── GempaRepository.kt
└── AndroidManifest.xml         (+ permission INTERNET)
```

Alasan tidak pakai `di/` folder DI container terpisah: lib DI (Hilt/Koin) dilarang. Manual wiring cukup lewat `ViewModelFactory` sederhana atau constructor Repository di-construct langsung di `HomeViewModel` companion/factory.

## 2. Data Model (mapping JSON BMKG)

Endpoint: `GET https://data.bmkg.go.id/DataMKG/TEWS/gempaterkini.json`

Struktur JSON:
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
        "Dirasakan": "...",
        "Shakemap": "..."
      }
    ]
  }
}
```

`data/model/GempaResponse.kt`:
```kotlin
data class GempaResponse(
    val Infogempa: Infogempa
)

data class Infogempa(
    val gempa: List<Gempa>
)

data class Gempa(
    val Tanggal: String,
    val Jam: String,
    val DateTime: String,
    val Coordinates: String,
    val Lintang: String,
    val Bujur: String,
    val Magnitude: String,
    val Kedalaman: String,
    val Wilayah: String,
    val Potensi: String
)
```
Field persis match key JSON BMKG (huruf besar), biar converter-gson tidak perlu `@SerializedName`. Semua `String` (BMKG kirim semua sebagai string, termasuk Magnitude) — hindari parse error, null safety aman karena non-nullable + default dari API selalu ada field ini.

## 3. Networking Layer

`data/remote/BmkgApiService.kt`:
```kotlin
interface BmkgApiService {
    @GET("DataMKG/TEWS/gempaterkini.json")
    suspend fun getGempaTerkini(): GempaResponse
}
```

`data/remote/RetrofitInstance.kt`:
```kotlin
object RetrofitInstance {
    private const val BASE_URL = "https://data.bmkg.go.id/"

    val api: BmkgApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BmkgApiService::class.java)
    }
}
```
`object` + `by lazy` dipilih karena ini satu-satunya instance yang dibutuhkan, tanpa DI framework — paling sedikit kode yang bekerja.

## 4. Repository
```kotlin
class GempaRepository(private val api: BmkgApiService = RetrofitInstance.api) {
    suspend fun getGempaList(): List<Gempa> = api.getGempaTerkini().Infogempa.gempa
}
```

## 5. ViewModel + UiState (MVVM, pola P5 StateFlow)

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Gempa>) : UiState()
    data class Error(val message: String) : UiState()
}

class HomeViewModel(
    private val repository: GempaRepository = GempaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allGempa: List<Gempa> = emptyList()

    init { fetchGempa() }

    fun fetchGempa() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = try {
                allGempa = repository.getGempaList()
                UiState.Success(filterByQuery(_searchQuery.value))
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Gagal mengambil data gempa")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (_uiState.value is UiState.Success || allGempa.isNotEmpty()) {
            _uiState.value = UiState.Success(filterByQuery(query))
        }
    }

    private fun filterByQuery(query: String): List<Gempa> =
        if (query.isBlank()) allGempa
        else allGempa.filter { it.Wilayah.contains(query, ignoreCase = true) }
}
```
Satu `HomeViewModel` dipakai untuk Home (list+search) dan Detail (klik item lewat navigasi, Gempa dikirim via `NavType`/Json string atau index — lihat §6) — tidak perlu ViewModel kedua, scope share lewat `navigation-compose` backstack entry atau parent NavController.

Factory manual (tanpa Hilt), hanya dibuat kalau butuh constructor param non-default:
```kotlin
class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel() as T
}
```
→ skipped kalau ViewModel pakai default param constructor saja; `viewModel()` composable function dari `lifecycle-viewmodel-compose` cukup, tidak perlu Factory manual sama sekali. Tambah factory hanya kalau nanti butuh inject dependency custom.

## 6. Navigation (navigation-compose)

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{index}") {
        fun createRoute(index: Int) = "detail/$index"
    }
}
```
Kirim index posisi di list terfilter saat ini (bukan serialize object Gempa) — paling simpel, ViewModel scoped ke NavGraph (`hiltViewModel()` tidak dipakai, cukup `viewModel()` default yang scoped ke Activity karena nav graph compose default share ViewModelStoreOwner = Activity, jadi HomeViewModel instance sama dipakai Detail).

```kotlin
NavHost(navController, startDestination = Screen.Home.route) {
    composable(Screen.Home.route) { HomeScreen(viewModel, navController) }
    composable(
        Screen.Detail.route,
        arguments = listOf(navArgument("index") { type = NavType.IntType })
    ) { backStackEntry ->
        val index = backStackEntry.arguments?.getInt("index") ?: 0
        DetailScreen(viewModel, index, navController)
    }
}
```

## 7. UI / Composable

### HomeScreen
- `Scaffold(topBar = { TopAppBar(title = { Text("GempaKini") }) })`
- `OutlinedTextField` sebagai search bar (reusable composable `SearchBar(query, onQueryChange)`)
- `LazyColumn` + `items(list) { GempaListItem(it) { onClick } }`
- `when (uiState) { Loading -> CircularProgressIndicator; Error -> ErrorMessage(msg); Success -> LazyColumn(...) }`

### GempaListItem (reusable composable)
Card/Row menampilkan Tanggal, Magnitude, Wilayah. Tanpa Image/Icon drawable gempa (larangan lib image; icon vector bawaan Material Icons boleh dipakai kalau perlu, bukan termasuk "lib image").

### DetailScreen
- `Scaffold(topBar = { TopAppBar(title = {...}, navigationIcon = { IconButton(onClick={navController.popBackStack()}) { Icon(Icons.Default.ArrowBack, ...) } }) })`
- Tampilkan 7 field dalam `Column` + `Text` style, pakai Typography dari Type.kt custom.

## 8. Theme Customization (rubrik wajib)
- `Color.kt`: definisikan palet custom (primary/secondary/tertiary light & dark) — bukan default generated.
- `Theme.kt`: `lightColorScheme(...)` dan `darkColorScheme(...)` custom pakai warna di atas, `GempaKiniTheme { }` wrapper dynamicColor disable biar scheme custom konsisten.
- `Type.kt`: minimal override 2 style, misal `titleLarge` dan `bodyMedium` custom fontWeight/letterSpacing dari default Material3 baseline.

## 9. Error Handling
- Network/API error di-catch di ViewModel, pesan generik ke user ("Gagal memuat data, periksa koneksi internet").
- Tidak ada retry otomatis — cukup tombol "Coba lagi" opsional yang panggil `fetchGempa()` ulang (nice-to-have, bukan wajib FR).

## 10. Yang sengaja tidak dibuat (skip list)
- DI framework → skipped: manual singleton/`viewModel()` cukup untuk scope project ini, upgrade ke Hilt kalau project tumbuh lebih besar.
- Cache/local DB (Room/DataStore) → skipped: requirement tidak minta persist, upgrade kalau butuh offline-first.
- Retry/backoff logic kompleks → skipped: cukup manual re-fetch, upgrade kalau butuh resiliency production.
- Image/map rendering → dilarang spek, tidak relevan ditambah.
