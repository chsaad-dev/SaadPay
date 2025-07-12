package com.example.saadpay.presentation.ui.transactionhistory

import com.example.saadpay.data.model.Transaction

sealed class TransactionListItem {
    data class Header(val title: String) : TransactionListItem()
    data class Item(val transaction: Transaction) : TransactionListItem()
}
