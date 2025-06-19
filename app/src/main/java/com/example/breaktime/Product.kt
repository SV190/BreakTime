package com.example.breaktime

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = ""
) {
    // Конструктор без аргументов для Firebase Database
    constructor() : this("", "", "", 0.0, "", "")
} 