package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saadpay.data.repository.FirestoreRepository

class SendMoneyViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _sendSuccess = MutableLiveData<Boolean>()
    val sendSuccess: LiveData<Boolean> get() = _sendSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun sendMoney(receiverEmail: String, amount: Double) {
        _isLoading.value = true
        repository.sendMoney(receiverEmail, amount) { success, _ ->
            _isLoading.value = false
            _sendSuccess.value = success
        }
    }
}
