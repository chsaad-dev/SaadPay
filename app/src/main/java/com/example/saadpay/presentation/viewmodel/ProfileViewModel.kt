package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saadpay.data.model.User
import com.example.saadpay.data.repository.FirestoreRepository

class ProfileViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> get() = _userProfile

    fun fetchUserProfile() {
        repository.getCurrentUser { user, _ ->
            _userProfile.value = user
        }
    }
}
