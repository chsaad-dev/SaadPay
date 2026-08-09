package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saadpay.data.model.User
import com.example.saadpay.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val repository: FirestoreRepository = FirestoreRepository()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _userSyncComplete = MutableLiveData<Boolean>()
    val userSyncComplete: LiveData<Boolean> get() = _userSyncComplete

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        _loginSuccess.value = true
                        syncUserData(user.uid, user.displayName ?: "Unknown", user.email ?: "")
                    } else {
                        auth.signOut()
                        _errorMessage.value = "Please verify your email before logging in."
                        _loginSuccess.value = false
                    }
                } else {
                    _errorMessage.value = task.exception?.message ?: "Login failed"
                    _loginSuccess.value = false
                }
            }
    }

    private fun syncUserData(uid: String, name: String, email: String) {
        val newUser = User(
            uid = uid,
            name = name,
            email = email,
            balance = 0.0
        )
        repository.saveUserIfNotExists(newUser) { saved ->
            _userSyncComplete.value = saved
        }
    }

    fun resendVerificationEmail(onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            user.sendEmailVerification()
                .addOnSuccessListener {
                    onResult(true, "Verification email sent.")
                }
                .addOnFailureListener { e ->
                    onResult(false, e.message ?: "Failed to send verification email.")
                }
        } else {
            onResult(false, "No user to verify.")
        }
    }
}
