package com.example.prak_pemmob.data.model

data class ApiProduct(
    val id: Int,
    val category_id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val img: String
)

data class ApiCategory(
    val id: Int,
    val name: String,
    val description: String,
    val products_count: Int
)
