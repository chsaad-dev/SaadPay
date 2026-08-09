package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saadpay.data.repository.FirestoreRepository
import com.example.saadpay.domain.model.Transaction
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> get() = _balance

    private val _recentTransactions = MutableLiveData<List<Transaction>>()
    val recentTransactions: LiveData<List<Transaction>> get() = _recentTransactions

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> get() = _error

    private var listenerRegistration: ListenerRegistration? = null

    fun startListeningToUser() {
        listenerRegistration = repository.listenToCurrentUser { user ->
            if (user != null) {
                _userName.value = user.name
                _balance.value = user.balance
            } else {
                _error.value = true
            }
        }
    }

    fun fetchRecentTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchTransactionsForCurrentUser { list ->
                _recentTransactions.postValue(list.take(3))
            }
        }
    }

    fun fetchCurrentUser() {
        repository.getCurrentUser { user, _ ->
            if (user != null) {
                _userName.value = user.name
                _balance.value = user.balance
            } else {
                _error.value = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeListener(listenerRegistration)
    }
}
