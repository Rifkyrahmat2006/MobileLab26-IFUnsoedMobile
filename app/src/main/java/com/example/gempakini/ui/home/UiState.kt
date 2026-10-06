package com.example.gempakini.ui.home

import com.example.gempakini.data.model.Gempa

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Gempa>) : UiState()
    data class Error(val message: String) : UiState()
}
