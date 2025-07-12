package com.example.saadpay.presentation.ui.main.transaction

import com.example.saadpay.domain.model.Transaction

sealed class TransactionListItem {
    data class Header(val dateLabel: String) : TransactionListItem()
    data class Item(val transaction: Transaction) : TransactionListItem()
}
