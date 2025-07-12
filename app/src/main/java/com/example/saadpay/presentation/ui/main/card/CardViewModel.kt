package com.example.saadpay.presentation.ui.main.card

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saadpay.data.repository.FirestoreRepository
import com.example.saadpay.domain.model.CardModel
import com.google.firebase.auth.FirebaseAuth

class CardViewModel(private val repository: FirestoreRepository) : ViewModel() {

    private val _card = MutableLiveData<CardModel?>()
    val card: LiveData<CardModel?> = _card

    fun loadUserCard() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        repository.getUserCard(userId) { fetchedCard ->
            _card.value = fetchedCard
        }
    }
}
