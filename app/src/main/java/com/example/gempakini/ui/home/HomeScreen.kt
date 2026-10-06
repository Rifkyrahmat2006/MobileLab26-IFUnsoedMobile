package com.example.gempakini.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gempakini.data.model.Gempa
import com.example.gempakini.ui.theme.magnitudeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onGempaClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GempaKini", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            when (val state = uiState) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(state.message, onRetry = viewModel::fetchGempa)
                is UiState.Success -> GempaList(state.data, onGempaClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        placeholder = { Text("Ketik nama wilayah...") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(
            text = "Memuat data gempa...",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(onClick = onRetry) { Text("Coba Lagi") }
    }
}

@Composable
private fun GempaList(
    list: List<Gempa>,
    onGempaClick: (Int) -> Unit
) {
    if (list.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Tidak ada gempa ditemukan",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)) {
        items(list) { gempa ->
            val index = list.indexOf(gempa)
            GempaListItem(gempa = gempa, onClick = { onGempaClick(index) })
        }
    }
}

/** Reusable composable — dipanggil di LazyColumn Home. */
@Composable
fun GempaListItem(gempa: Gempa, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MagnitudeBadge(magnitude = gempa.Magnitude)
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(text = gempa.Wilayah, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = gempa.Tanggal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Reusable composable — badge bundar warna sesuai besar magnitudo. */
@Composable
fun MagnitudeBadge(magnitude: String) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(color = magnitude.magnitudeColor(), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = magnitude,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
