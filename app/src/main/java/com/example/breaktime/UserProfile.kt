package com.example.breaktime

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val totalSpent: Double = 0.0
) {
    // Конструктор без аргументов для Firebase Database
    constructor() : this("", "", "", "", "", 0, 0, 0.0)
} 