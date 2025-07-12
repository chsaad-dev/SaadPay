package com.example.saadpay.domain.model

data class CardModel(
    val type: String,
    val userName: String,
    val cardNumber: String = List(4) { (1000..9999).random() }.joinToString(" "),
    val expiry: String = "12/28",
    val cvv: String = (100..999).random().toString(),
    var isVisible: Boolean = false
)
