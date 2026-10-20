package com.example.prak_pemmob.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.prak_pemmob.R
import com.example.prak_pemmob.data.model.Product
import com.example.prak_pemmob.ui.viewmodel.ProductUiState
import com.example.prak_pemmob.ui.viewmodel.ProductViewModel

@Composable
fun DetailProductScreen(
    productId: Int,
    navController: NavController?,
    viewModel: ProductViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var quantity by rememberSaveable { mutableIntStateOf(1) }

    when (val state = uiState) {
        ProductUiState.Loading -> LoadingScreen()
        is ProductUiState.Error -> ErrorScreen(state.message, viewModel::loadProducts)
        is ProductUiState.Success -> {
            val product = state.products.find { it.id == productId }
            if (product == null) {
                ErrorScreen("Produk tidak ditemukan", {})
            } else {
                StatelessDetailProduct(
                    product = product,
                    quantity = quantity,
                    onQuantityChange = { quantity = it },
                    onBackClick = { navController?.popBackStack() },
                    onAddToCartClick = {
                        Toast.makeText(context, "Dimasukkan: $quantity", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Coba Lagi") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatelessDetailProduct(
    product: Product,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onBackClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(R.drawable.back_icon), "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = productImageUrl(product.img),
                contentDescription = product.name,
                modifier = Modifier.fillMaxWidth().height(280.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(16.dp)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Rp ${product.price}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Text("Deskripsi", fontWeight = FontWeight.Bold)
                Text(product.description.orEmpty())
                Text("Stok: ${product.stock}")
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Jumlah Beli")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }, enabled = quantity > 1) { Text("-") }
                        Text(quantity.toString(), Modifier.padding(horizontal = 16.dp))
                        FilledTonalIconButton(onClick = { if (quantity < product.stock) onQuantityChange(quantity + 1) }, enabled = quantity < product.stock) { Text("+") }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddToCartClick, enabled = product.stock > 0, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Tambah ke Keranjang")
                }
            }
        }
    }
}

fun productImageUrl(image: String): String? =
    if (image.startsWith("http")) image else null
