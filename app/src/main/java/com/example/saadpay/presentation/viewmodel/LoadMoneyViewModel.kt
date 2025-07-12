package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saadpay.data.repository.FirestoreRepository

class LoadMoneyViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _loadSuccess = MutableLiveData<Boolean>()
    val loadSuccess: LiveData<Boolean> get() = _loadSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadMoney(amount: Double) {
        _isLoading.value = true
        repository.loadMoney(amount) { success ->
            _isLoading.value = false
            _loadSuccess.value = success
        }
    }
}
