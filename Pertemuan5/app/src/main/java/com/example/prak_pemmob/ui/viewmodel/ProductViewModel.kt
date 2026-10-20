package com.example.prak_pemmob.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prak_pemmob.data.model.Category
import com.example.prak_pemmob.data.model.Product
import com.example.prak_pemmob.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductUiState {
    data object Loading : ProductUiState
    data class Success(
        val categories: List<Category>,
        val products: List<Product>
    ) : ProductUiState
    data class Error(val message: String) : ProductUiState
}

class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            _uiState.value = try {
                val (categories, products) = repository.getProducts()
                ProductUiState.Success(categories, products)
            } catch (e: Exception) {
                ProductUiState.Error(e.message ?: "Gagal memuat data")
            }
        }
    }
}
