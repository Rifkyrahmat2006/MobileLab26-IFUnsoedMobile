# ARCHITECTURE.md — GempaKini

## 1. Pola: MVVM (wajib, sesuai soal "pola P5")
```
Composable (View)
     │  observe StateFlow, kirim event (search query, retry)
     ▼
ViewModel (StateFlow<UiState>)
     │  panggil suspend fun
     ▼
Repository
     │  panggil Retrofit service
     ▼
ApiService (Retrofit interface)
     │  HTTP GET
     ▼
BMKG REST API → JSON → Gson → Data Model
```
Composable **tidak pernah** panggil Retrofit/Repository langsung — hanya ViewModel.

## 2. Package Structure
```
com.example.gempakini
├── data
│   ├── model/
│   │   ├── GempaResponse.kt     // root: Infogempa
│   │   ├── Infogempa.kt
│   │   └── Gempa.kt             // Tanggal, Jam, Coordinates, Magnitude, Kedalaman, Wilayah, Potensi
│   ├── remote/
│   │   ├── BmkgApiService.kt    // Retrofit interface, GET gempaterkini.json
│   │   └── RetrofitInstance.kt  // object singleton build Retrofit
│   └── repository/
│       └── GempaRepository.kt   // wrap API call, expose suspend fun getGempaTerkini()
├── ui
│   ├── theme/                   // Color.kt, Theme.kt, Type.kt (sudah ada dari template)
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── components/ (GempaListItem.kt, SearchBar.kt, MagnitudeBadge.kt)
│   └── detail/
│       └── DetailScreen.kt
├── navigation/
│   └── NavGraph.kt              // navigation-compose: "home" -> "detail/{index}"
└── MainActivity.kt
```

## 3. Data Model (struktur JSON BMKG)
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
    val Coordinates: String,
    val Magnitude: String,
    val Kedalaman: String,
    val Wilayah: String,
    val Potensi: String
)
```
Catatan: field API BMKG semuanya string (termasuk Magnitude) — tidak perlu custom deserializer, cukup `converter-gson` default. Null safety dipakai saat parsing (`?.` / `?:` fallback "-" kalau field kosong).

## 4. Networking Layer
```kotlin
interface BmkgApiService {
    @GET("DataMKG/TEWS/gempaterkini.json")
    suspend fun getGempaTerkini(): GempaResponse
}

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
`AndroidManifest.xml` wajib tambah:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 5. Repository
```kotlin
class GempaRepository(private val api: BmkgApiService = RetrofitInstance.api) {
    suspend fun getGempaTerkini(): List<Gempa> = api.getGempaTerkini().Infogempa.gempa
}
```

## 6. UiState (sealed class, wajib StateFlow)
```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Gempa>) : UiState()
    data class Error(val message: String) : UiState()
}
```

## 7. ViewModel
```kotlin
class HomeViewModel(private val repository: GempaRepository = GempaRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var allGempa: List<Gempa> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init { fetchGempa() }

    fun fetchGempa() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = try {
                allGempa = repository.getGempaTerkini()
                UiState.Success(allGempa)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Gagal memuat data")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        val filtered = allGempa.filter { it.Wilayah.contains(query, ignoreCase = true) }
        _uiState.value = UiState.Success(filtered)
    }
}
```
Tidak pakai DI framework (Hilt/Koin dilarang) → default parameter constructor sebagai "manual DI" sederhana. Kalau butuh `ViewModelFactory` eksplisit untuk alasan lain, tambah di `navigation-compose` graph.

## 8. Navigation (navigation-compose)
```kotlin
NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen(viewModel, onItemClick = { index ->
        navController.navigate("detail/$index")
    }) }
    composable("detail/{index}", arguments = listOf(navArgument("index") { type = NavType.IntType })) { backStackEntry ->
        val index = backStackEntry.arguments?.getInt("index") ?: 0
        DetailScreen(gempa = viewModel.getGempaByIndex(index))
    }
}
```
Pakai index/id ke list yang sudah ada di ViewModel — hindari serialize object besar lewat navigation argument (lib tambahan untuk itu dilarang juga).

## 9. Dependency (libs.versions.toml — tambahan dari template)
Hanya tambahkan 4 ini di luar bawaan template Compose:
```toml
retrofit = "2.11.0"
retrofit-gson = "2.11.0"
navigation-compose = "2.8.4"
lifecycle-viewmodel-compose = "2.8.7"
```
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("androidx.navigation:navigation-compose:2.8.4")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
```

## 10. Ekstension function (opsional, syarat "jika diperlukan")
```kotlin
fun String.toMagnitudeFloatOrNull(): Float? = this.toFloatOrNull()
```
Dipakai di `MagnitudeBadge` buat tentuin warna badge (>=5.0 → oranye/merah).
