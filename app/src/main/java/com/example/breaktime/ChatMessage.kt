package com.example.breaktime

data class ChatMessage(
    val userId: String = "",
    val userName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", 0)
} 