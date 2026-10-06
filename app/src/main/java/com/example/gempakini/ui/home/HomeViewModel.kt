package com.example.gempakini.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gempakini.data.model.Gempa
import com.example.gempakini.data.repository.GempaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Satu ViewModel dipakai bersama Home & Detail screen (shared lewat
 * ViewModelStoreOwner default NavHost = Activity), sesuai Design.md §5-6.
 */
class HomeViewModel(
    private val repository: GempaRepository = GempaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allGempa: List<Gempa> = emptyList()

    init {
        fetchGempa()
    }

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
        if (allGempa.isNotEmpty() || _uiState.value is UiState.Success) {
            _uiState.value = UiState.Success(filterByQuery(query))
        }
    }

    /** Dipakai DetailScreen ambil item by index dari list TERFILTER saat ini. */
    fun getGempaAt(index: Int): Gempa? = (_uiState.value as? UiState.Success)?.data?.getOrNull(index)

    private fun filterByQuery(query: String): List<Gempa> =
        if (query.isBlank()) allGempa
        else allGempa.filter { it.Wilayah.contains(query, ignoreCase = true) }
}
