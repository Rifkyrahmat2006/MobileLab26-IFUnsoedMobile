package com.example.prak_pemmob.data.repository

import com.example.prak_pemmob.data.model.Category
import com.example.prak_pemmob.data.model.Product
import com.example.prak_pemmob.data.model.toCategory
import com.example.prak_pemmob.data.model.toProduct
import com.example.prak_pemmob.network.ApiClient
import com.example.prak_pemmob.network.ApiInterface

class ProductRepository(
    private val api: ApiInterface = ApiClient.instance
) {
    suspend fun getProducts(): Pair<List<Category>, List<Product>> {
        val categories = api.getCategories().map { it.toCategory() }
        val byId = categories.associateBy { it.id }
        val products = api.getProducts().mapNotNull { product ->
            byId[product.category_id]?.let { product.toProduct(it) }
        }
        return categories to products
    }
}
