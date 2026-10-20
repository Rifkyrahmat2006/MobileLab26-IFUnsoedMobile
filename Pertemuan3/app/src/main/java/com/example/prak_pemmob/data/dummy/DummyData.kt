package com.example.prak_pemmob.data.dummy

import com.example.prak_pemmob.data.model.Category
import com.example.prak_pemmob.data.model.Product

object DummyData {
    val categories = listOf(
        Category(id = 1, name = "Makanan", description = "Produk makanan khas daerah", products_count = 3),
        Category(id = 2, name = "Kerajinan", description = "Produk kerajinan tangan lokal", products_count = 2),
        Category(id = 3, name = "Alat Rumah Tangga", description = "Peralatan rumah tangga tradisional", products_count = 1)
    )

    val products = listOf(
        Product(id = 1, name = "Mendoan", price = 5000, img = "dummy_product", category = categories[0]),
        Product(id = 2, name = "Nopia", price = 15000, img = "dummy_product", category = categories[0]),
        Product(id = 3, name = "Getuk Goreng", price = 10000, img = "dummy_product", category = categories[0]),
        Product(id = 4, name = "Batik Banyumasan", price = 150000, img = "dummy_product", category = categories[1]),
        Product(id = 5, name = "Anyaman Bambu", price = 45000, img = "dummy_product", category = categories[1]),
        Product(id = 6, name = "Sapu Glagah", price = 20000, img = "dummy_product", category = categories[2])
    )
}