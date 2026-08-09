package com.example.saadpay.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Transaction(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val receiverName: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0L,
    val type: String = "",
    val participants: List<String> = emptyList()
)
