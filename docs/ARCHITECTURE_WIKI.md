---
title: GempaKini — Android MVVM + Jetpack Compose Case Study
tags: [android, kotlin, compose, mvvm, architecture, networking, state-management]
created: 2026-10-06
updated: 2026-10-06
author: Rifky Dwi Rahmat Prakoso
project: GempaKini (Responsi Mobile Programming)
---

# GempaKini — Android MVVM + Jetpack Compose Case Study

Dokumentasi design decision, architecture pattern, dan lessons learned dari membangun aplikasi Android Kotlin native untuk katalog & monitoring gempa BMKG.

---

## 1. Problem Statement

**Requirement:** Aplikasi mobile Android yang fetch data gempa real-time dari BMKG API, tampilkan dalam list + detail, dengan fitur search lokal. Batasan: hanya 4 library networking (retrofit, converter-gson, navigation-compose, lifecycle-viewmodel-compose), tanpa Hilt/Koin/Coil/Glide, 100% Jetpack Compose.

**Challenge:**
- MVVM ketat tanpa DI framework → manual singleton/viewModel()
- State-driven UI dengan StateFlow
- Search filter lokal tanpa re-fetch
- 2 screen navigation via navigation-compose, single ViewModel shared scope
- Custom Material Design 3 theme (3 override min: Color/Theme/Type)

---

## 2. Architecture Decision: MVVM

### Layers
```
Composable (UI)
    ↓
ViewModel (Business Logic)
    ↓
Repository (Data Aggregation)
    ↓
Service/API (Remote Data)
    ↓
Model (Data Representation)
```

### Why MVVM
- **Separation of concern**: UI tidak tahu implementation detail fetch/parse data
- **Testability**: ViewModel logic isolated dari Compose recomposition
- **State-driven**: UI adalah fungsi pure dari state (StateFlow<UiState>)
- **Lifecycle-aware**: viewModelScope automatic cancel saat screen destroy

---

## 3. State Management: Sealed Class + StateFlow

### UiState Desain
```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Gempa>) : UiState()
    data class Error(val message: String) : UiState()
}

// Di ViewModel:
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

### Keuntungan Pattern Ini
1. **Type-safe**: compiler enforce handle semua case (Loading/Success/Error)
2. **Immutable**: StateFlow `.asStateFlow()` expose immutable view
3. **Observable**: Composable collect state perubahan, recompose otomatis
4. **Testable**: mock UiState, verify logic tanpa Compose

### Implementasi di Composable
```kotlin
val uiState by viewModel.uiState.collectAsState()

when (val state = uiState) {
    is UiState.Loading -> LoadingView()
    is UiState.Error -> ErrorView(state.message)
    is UiState.Success -> GempaList(state.data)
}
```

**Penting:** `when` expression di Compose bukan memory leak — collectAsState() automatic unsubscribe saat Composable dispose.

---

## 4. Networking: Retrofit + Gson + Manual Singleton

### Why Manual Singleton (No Hilt)
Rubric melarang Hilt. Solusi minimal:

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

**`by lazy`** = instance dibuat hanya saat pertama kali diakses, singleton pattern tanpa synchronized() overhead di most case. Thread-safe di Kotlin.

### Repository Pattern
```kotlin
class GempaRepository(
    private val api: BmkgApiService = RetrofitInstance.api
) {
    suspend fun getGempaList(): List<Gempa> = 
        api.getGempaTerkini().Infogempa.gempa
}
```

**Design:** constructor parameter `api` punya default value `RetrofitInstance.api`, jadi:
- Production: `GempaRepository()` langsung pakai instance default
- Testing: `GempaRepository(mockApi)` inject mock tanpa Hilt

---

## 5. ViewModel: Fetch Init + Search Filter Lokal

### Init Block
```kotlin
class HomeViewModel(
    private val repository: GempaRepository = GempaRepository()
) : ViewModel() {
    private var allGempa: List<Gempa> = emptyList()
    
    init { fetchGempa() }
    
    fun fetchGempa() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = try {
                allGempa = repository.getGempaList()
                UiState.Success(filterByQuery(_searchQuery.value))
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Gagal mengambil data")
            }
        }
    }
}
```

**Keputusan:** fetch di `init` (bukan di Composable, bukan di `LaunchedEffect`):
- Fetch terjadi tepat 1x saat ViewModel dibuat (activity/screen created)
- Tidak ada race condition dengan recomposition
- ViewModel scope automatic cancel jika screen destroy sebelum fetch selesai

### Local Filter (O(n) tidak masalah kecil dataset)
```kotlin
fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
    if (allGempa.isNotEmpty()) {
        _uiState.value = UiState.Success(filterByQuery(query))
    }
}

private fun filterByQuery(query: String): List<Gempa> =
    if (query.isBlank()) allGempa
    else allGempa.filter { it.Wilayah.contains(query, ignoreCase = true) }
```

**Why local filter:**
- API BMKG tidak support query param filter, hanya return full list setiap kali
- Filter di app instant (tidak wait network), better UX
- Kurangi network traffic, kurangi server load
- Edge case: jika list gempa ribuan → implement pagination atau debounce search

---

## 6. Navigation: Single ViewModel Shared Scope

### Desain: Satu ViewModel Dua Screen
```kotlin
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: HomeViewModel = viewModel()  // scoped to activity

    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(viewModel, onGempaClick = { index ->
                navController.navigate(Screen.Detail.createRoute(index))
            })
        }
        composable(Screen.Detail.route, arguments = listOf(...)) { entry ->
            val index = entry.arguments?.getInt("index") ?: 0
            DetailScreen(
                gempa = viewModel.getGempaAt(index),
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
```

**Why shared ViewModel:**
- `viewModel()` composable function default scope ke NavHostController's ViewModelStoreOwner = Activity
- Detail screen bisa akses list terfilter dari Home (via `viewModel.getGempaAt(index)`)
- Back button automatic preserve filter state (search query, filtered list)
- Hanya 1 fetch API per lifecycle, bukan multiple fetch per screen

### Pass Data via Index (Not Object)
```kotlin
// ❌ Jangan:
navController.navigate(Screen.Detail.createRoute(gempa))  // Serialize object overhead

// ✅ Do:
navController.navigate(Screen.Detail.createRoute(index))  // Pass int, grab from ViewModel
```

Alasan: Navigation argument di Compose tidak automatic serialize Parcelable. Paling clean: pass primitive (Int), query object dari ViewModel.

---

## 7. Kotlin Features Dimanfaatkan

### Data Class
```kotlin
data class Gempa(
    val Tanggal: String,
    val Jam: String,
    // ... 5 field lagi
)
```
Auto-generate `.equals()`, `.hashCode()`, `.toString()`, `.copy()`. Gson deserialize langsung ke instance.

### Null Safety
```kotlin
val uiState by viewModel.uiState.collectAsState()
// uiState tipe UiState (non-nullable)
// Tidak perlu null check

val message = (uiState as? UiState.Error)?.message ?: "Unknown"
// Smart cast, safe extraction
```

### Lambda
```kotlin
list.filter { it.Wilayah.contains(query, ignoreCase = true) }
onGempaClick = { index -> navController.navigate(...) }
```

### Extension Function (Opsional, tidak dipaksa di sini)
Bisa bikin misalnya:
```kotlin
fun List<Gempa>.filterByWilayah(query: String) = 
    filter { it.Wilayah.contains(query, ignoreCase = true) }
```
Tapi tidak natural di context ini, jadi skip.

---

## 8. Material Design 3 Customization

### Color.kt Strategy
```kotlin
val SeismicRed80 = Color(0xFFFFB4A9)    // Light theme primary
val SeismicRed40 = Color(0xFFB3261E)    // Dark theme primary
val OceanBlue80 = Color(0xFFA8CFFF)     // Secondary
// ... dst
```

Naming konvensi: `[ColorName][Brightness]`. Material3 spec: 80-series untuk light theme (terbang), 40-series untuk dark theme (gelap).

### Theme.kt — Disable Dynamic Color
```kotlin
@Composable
fun GempaKiniTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Why disable dynamic color:** default template Material3 pakai `dynamicDarkColorScheme(context)` di Android 12+, yang override palette kustom. Di sini explicit custom palet, jadi disable dynamic supaya konsisten semua device.

### Type.kt — Override 2 Styles (Rubric Min)
```kotlin
Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
)
```

Penting: define **one instance** `val Typography = Typography(...)`, pakai di `MaterialTheme(typography = Typography)`.

---

## 9. Composable Design: Reusable Components

### GempaListItem — Reusable Card
```kotlin
@Composable
fun GempaListItem(gempa: Gempa, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = gempa.Wilayah, style = MaterialTheme.typography.titleLarge)
            Text(text = "Tanggal: ${gempa.Tanggal}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Magnitudo: ${gempa.Magnitude}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```

**Why separate:**
- Reusable (bisa pakai di konteks lain: list, search result, favorit, dst)
- Preview mudah (`@Preview @Composable fun GempaListItemPreview()`)
- Logic terpisah dari parent (HOME screen tidak perlu tahu detail rendering item)

### SearchBar — Input Reusable
```kotlin
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        label = { Text("Cari wilayah") },
        placeholder = { Text("🔍 Ketik nama wilayah") },
        singleLine = true
    )
}
```

**Design:** `private fun` (scope ke file saja), tapi bisa di-extract ke file terpisah `SearchBar.kt` jika dipakai di multiple screens.

---

## 10. Error Handling Strategy

### Network Error
```kotlin
try {
    allGempa = repository.getGempaList()
    UiState.Success(...)
} catch (e: Exception) {
    UiState.Error(e.message ?: "Gagal mengambil data gempa")
}
```

**Catch `Exception` (broad):** include IOException (no internet), HttpException (API error), JsonSyntaxException (malformed JSON), timeout, dst.

### UI Representation
```kotlin
is UiState.Error -> ErrorView(state.message)

@Composable
private fun ErrorView(message: String) {
    Column(...) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        // Bisa tambah "Coba lagi" button → viewModel.fetchGempa()
    }
}
```

### What We Skip
- Retry otomatis: di-skip, user manual retry via tombol
- Fallback/offline cache: di-skip, tidak ada DB (YAGNI)
- Detailed error classification: di-skip, generic message cukup buat rubric

---

## 11. Testing Strategy (Theoretical, Tidak Diimplement di Sini)

### Unit Test ViewModel
```kotlin
@Test
fun fetchGempa_success_updateUiState() = runTest {
    val mockRepo = mock<GempaRepository>()
    coEvery { mockRepo.getGempaList() } returns listOf(
        Gempa("06 Okt", "14:30", ..., "Malang", ...)
    )
    
    val viewModel = HomeViewModel(mockRepo)
    viewModel.uiState.test {
        assertEquals(awaitItem(), UiState.Loading)
        assertEquals(awaitItem(), UiState.Success(...))
    }
}
```

Pola: mock Repository, test ViewModel logic tanpa Compose, gunakan `turbine` lib untuk StateFlow testing.

### Integration Test (Compose + ViewModel)
```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun homeScreen_displayList_onSuccess() {
    composeTestRule.setContent {
        GempaKiniTheme {
            HomeScreen(viewModel)
        }
    }
    
    composeTestRule.onNodeWithText("Kab. Malang").assertIsDisplayed()
}
```

---

## 12. Lessons Learned & Gotchas

### ❌ Trap 1: Fetch di LaunchedEffect di Composable
```kotlin
// JANGAN:
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    LaunchedEffect(Unit) {
        viewModel.fetchGempa()  // Fetch setiap recomposition!
    }
}
```
Alasan: Recomposition terjadi saat state berubah, state berubah karena fetch selesai → potential loop. **Solution:** fetch di ViewModel init, bukan di Composable.

### ❌ Trap 2: Tidak Handle Back Stack State
```kotlin
// JANGAN:
DetailScreen(gempa = viewModel.allGempa.first())  // Always first
```
DetailScreen harus terima index atau gempa spesifik dari navigation argument. Kalau hardcode, back dari detail ke home, list bisa kembali ke state awal (filter reset), bukan state saat user klik item.

### ❌ Trap 3: Icons Dependency
Material Icons (`Icons.Default.*`) butuh artifact `androidx.compose.material:material-icons-core` atau `material-icons-extended`, yang **tidak di-list** izin rubric. **Solution:** pakai Text symbol (emoji/karakter).

### ✅ Best Practice 1: ViewModel Factory (Only if Needed)
Jangan bikin Factory kalau constructor punya default param. Bikin hanya saat butuh inject dependency custom:
```kotlin
class HomeViewModelFactory(val customRepo: GempaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(customRepo) as T
}
```

### ✅ Best Practice 2: Immutable Model + Copy
```kotlin
data class Gempa(...) // auto .copy()

// Modify gempa tanpa mutate:
val updated = gempa.copy(Magnitude = "5.0")
```

### ✅ Best Practice 3: Sealed Class untuk State
Jangan pakai `Pair<Boolean, List<Gempa>>` atau multiple `MutableStateFlow`. Sealed class enforce type safety, readability.

---

## 13. Performance Notes

### Recomposition
- Composable function cek input param (stable types: String, Int, List<Gempa>, Callback) untuk skip recomposition
- `when (uiState)` memicu recomposition hanya saat uiState berubah (collect dari StateFlow)
- LazyColumn tidak render offscreen item (viewport-based)

### Memory
- ViewModel scoped ke Activity, persist across configuration change (rotation)
- allGempa (List<Gempa>) di-hold di ViewModel — jika list besar (>1000 item) consider pagination
- No local cache — data baru setiap app start

### Network
- Single fetch per app session (di init)
- Search filter lokal (tidak network call per keystroke)
- Retrofit + Gson + OkHttp default config (30s timeout, etc)

---

## 14. Upgrade Path (Skipped di Sini, Noted Untuk Masa Depan)

| Feature | Alasan Skip | How to Upgrade |
|---------|------------|--------|
| Local cache (Room) | YAGNI, in-memory cukup rubric | Add Room dependency, migrate allGempa ke DB, query dengan Dao |
| Pagination | List kecil (~50 item max) | Add paging3, LazyColumn + PagingDataAdapter |
| DI framework (Hilt) | Rubric larang | Replace manual singleton + Factory dengan @HiltViewModel, @Inject |
| Retry logic | Simple test, skip complexity | Add ExponentialBackoff, Retrofit retry interceptor |
| Offline mode | No requirement | Add DataStore, periodic cache, check internet first |
| Image gallery | Rubric larang | Add Coil/Glide, fetch gempa photo dari endpoint lain |
| Map/Geo visualization | Rubric larang | Add Google Maps API / OpenStreetMap library |

---

## 15. Submission Checklist

- [x] Kotlin features: data class, null safety, lambda
- [x] Compose 100%: Scaffold, TopAppBar, LazyColumn, Card, OutlinedTextField
- [x] Material Design 3: custom theme, 3 override color/type/shape
- [x] MVVM: Composable→ViewModel→Repository→API Service→Model
- [x] StateFlow UiState (Loading/Success/Error)
- [x] Retrofit + Gson + nav-compose + lifecycle-viewmodel-compose (4 lib only)
- [x] No Hilt/Koin/Coil/Glide/Room
- [x] INTERNET permission
- [x] No API call di Composable (grep 0 hasil)
- [x] Build successful: `./gradlew clean assembleDebug`
- [x] APK debug ready: `app/build/outputs/apk/debug/app-debug.apk`
- [x] README.md + docs (PRD, DESIGN, API_SPEC, CHECKLIST)
- [ ] Runtime verification (emulator/device)
- [ ] Video penjelasan kode

---

## References

- [Jetpack Compose Documentation](https://developer.android.com/develop/ui/compose)
- [Android MVVM Architecture](https://developer.android.com/architecture/ui-layer/stateholders)
- [Retrofit Guide](https://square.github.io/retrofit/)
- [Material Design 3 for Android](https://m3.material.io/)
- [Kotlin Coroutines + StateFlow](https://kotlinlang.org/docs/flow.html)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)

---

**Created:** 2026-10-06  
**Project:** GempaKini (Responsi Mobile Programming I)  
**Author:** Rifky Dwi Rahmat Prakoso
