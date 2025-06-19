package com.example.breaktime

import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val orderDate: Date = Date(),
    val deliveryAddress: String = "",
    val phoneNumber: String = ""
) {
    // Конструктор без аргументов для Firebase Database
    constructor() : this("", "", emptyList(), 0.0, OrderStatus.PENDING, Date(), "", "")
}

enum class OrderStatus {
    PENDING,    // Ожидает подтверждения
    CONFIRMED,  // Подтвержден
    PREPARING,  // Готовится
    DELIVERING, // Доставляется
    COMPLETED,  // Выполнен
    CANCELLED   // Отменен
} 