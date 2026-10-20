package com.example.prak_pemmob.ui.screen

import android.content.res.Configuration
import android.widget.Toast
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.prak_pemmob.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prak_pemmob.data.model.Category
import com.example.prak_pemmob.data.dummy.DummyData
import com.example.prak_pemmob.ui.viewmodel.ProductUiState
import com.example.prak_pemmob.ui.viewmodel.ProductViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prak_pemmob.ui.screen.productImageUrl
import com.example.prak_pemmob.data.model.Product
import kotlinx.coroutines.delay

@Composable
fun ProductItemCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = productImageUrl(product.img),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)).background(Color.White),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.dummy_product),
                    error = painterResource(R.drawable.dummy_product)
                )

                if (product.category != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(all = 4.dp)
                            .clip(RoundedCornerShape(size = 4.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(
                            text = product.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Rp ${product.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProduct() {
    ProductItemCard(
        product = DummyData.products[0],
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCategory() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CategoryItem(
            category = DummyData.categories[0],
            isSelected = true,
            onClick = {}
        )
    }
}

@Composable
fun DaftarProductScreen(
    navController: NavController? = null,
    viewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    when (val state = uiState) {
        ProductUiState.Loading -> LoadingScreen()
        is ProductUiState.Error -> ErrorScreen(state.message, viewModel::loadProducts)
        is ProductUiState.Success -> {
            val categories = state.categories
            if (selectedCategoryId == null && categories.isNotEmpty()) {
                selectedCategoryId = categories.first().id
            }
            val filteredProducts = state.products.filter { product ->
                (selectedCategoryId == null || product.category.id == selectedCategoryId) &&
                    product.name.contains(searchQuery, ignoreCase = true)
            }
            StatelessDaftarProduct(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isLoading = false,
                products = filteredProducts,
                onProductClick = { product ->
                    Toast.makeText(context, "Clicked: ${product.name}", Toast.LENGTH_SHORT).show()
                    navController?.navigate("detail/${product.id}")
                },
                onContactUsClick = { navController?.navigate("hubungi_kami") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatelessDaftarProduct(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean,
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onContactUsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(value = false) }

            TopAppBar(
                title = {
                    Text(text = "Daftar Produk")
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hubungi Kami") },
                            onClick = {
                                expanded = false
                                onContactUsClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email"
                                )
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Cari produk...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            Text(
                text = "Kategori Produk",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Daftar Produk",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Mencari data...")
                    }
                }
            } else {
                if (products.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Produk tidak ditemukan.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(count = 2),
                        contentPadding = PaddingValues(all = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(products) { product ->
                            ProductItemCard(product = product) {
                                onProductClick(product)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun DaftarProductScreenLightPreview() {
    DaftarProductScreen()
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun DaftarProductScreenDarkPreview() {
    DaftarProductScreen()
}
