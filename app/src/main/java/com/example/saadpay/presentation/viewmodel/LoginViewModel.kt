package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        _loginSuccess.value = true
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
