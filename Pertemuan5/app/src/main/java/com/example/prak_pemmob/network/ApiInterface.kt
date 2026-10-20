package com.example.prak_pemmob.network

import com.example.prak_pemmob.data.model.ApiCategory
import com.example.prak_pemmob.data.model.ApiProduct
import retrofit2.http.GET

interface ApiInterface {
    @GET("data/categories.json")
    suspend fun getCategories(): List<ApiCategory>

    @GET("data/products.json")
    suspend fun getProducts(): List<ApiProduct>
}
