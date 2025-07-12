package com.example.saadpay.presentation.ui.main.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.saadpay.data.repository.FirestoreRepository

class CardViewModelFactory(
    private val repository: FirestoreRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardViewModel(repository) as T
    }
}
