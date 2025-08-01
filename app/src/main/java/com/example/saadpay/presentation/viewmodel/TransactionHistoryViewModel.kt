package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saadpay.domain.model.Transaction
import com.example.saadpay.data.repository.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionHistoryViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchTransactionsForCurrentUser { list ->
                _transactions.postValue(list)
            }
        }
    }
}
