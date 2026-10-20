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

fun ApiProduct.toProduct(category: Category) = Product(
    id = id,
    name = name,
    price = price.toInt(),
    img = img,
    category = category,
    description = description,
    stock = stock
)

fun ApiCategory.toCategory() = Category(
    id = id,
    name = name,
    description = description,
    products_count = products_count
)
