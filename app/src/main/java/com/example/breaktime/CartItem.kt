package com.example.breaktime

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    // Вычисляемое свойство для totalPrice (не сохраняется в Firebase)
    fun getTotalPrice(): Double = product.price * quantity

    // Конструктор без аргументов для Firebase Database
    constructor() : this(Product(), 1)
} 