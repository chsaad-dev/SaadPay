package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saadpay.data.repository.FirestoreRepository
import com.google.firebase.firestore.ListenerRegistration

class DashboardViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> get() = _balance

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

    fun fetchCurrentUser() {
        repository.getCurrentUser { user, e ->
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
