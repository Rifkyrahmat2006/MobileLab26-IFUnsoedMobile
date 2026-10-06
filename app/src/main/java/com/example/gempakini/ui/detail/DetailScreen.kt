package com.example.gempakini.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gempakini.data.model.Gempa
import com.example.gempakini.ui.theme.magnitudeColor

private data class DetailField(val label: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    gempa: Gempa?,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Gempa", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (gempa == null) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
                Text("Data tidak ditemukan", style = MaterialTheme.typography.bodyMedium)
            }
            return@Scaffold
        }

        val fields = listOf(
            DetailField("Tanggal", gempa.Tanggal),
            DetailField("Jam", gempa.Jam),
            DetailField("Coordinates", gempa.Coordinates),
            DetailField("Kedalaman", gempa.Kedalaman),
            DetailField("Wilayah", gempa.Wilayah),
            DetailField("Potensi", gempa.Potensi)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item { MagnitudeHero(magnitude = gempa.Magnitude, wilayah = gempa.Wilayah) }
            items(fields) { field -> DetailRow(field) }
        }
    }
}

/** Hero card — tonjolkan magnitudo besar di atas. */
@Composable
private fun MagnitudeHero(magnitude: String, wilayah: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(color = magnitude.magnitudeColor(), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = magnitude, color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = wilayah,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(text = "Magnitudo", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Reusable composable — satu baris label-value, dipakai 6x. */
@Composable
private fun DetailRow(field: DetailField) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = field.label, style = MaterialTheme.typography.titleLarge)
            Text(
                text = field.value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
