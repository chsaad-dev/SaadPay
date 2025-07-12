package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saadpay.data.repository.FirestoreRepository
import com.example.saadpay.domain.model.Transaction

class TransactionViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    fun fetchTransactions() {
        repository.getTransactionHistory { list, err ->
            if (err == null) {
                _transactions.value = list
            } else {
                _error.value = true
            }
        }
    }
}
