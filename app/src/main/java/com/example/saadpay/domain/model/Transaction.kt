package com.example.saadpay.domain.model

data class Transaction(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0L,
    val type: String = ""
)
