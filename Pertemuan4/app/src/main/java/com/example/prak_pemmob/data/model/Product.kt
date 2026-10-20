package com.example.prak_pemmob.data.model

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val img: String,
    val category: Category,
    val description: String? = null,
    val stock: Int = 0
)