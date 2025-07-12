package com.example.saadpay.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.saadpay.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                val uid = firebaseUser?.uid ?: return@addOnCompleteListener

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                firebaseUser.updateProfile(profileUpdates).addOnCompleteListener {
                    val user = User(uid, name, email, 0.0)
                    db.collection("users").document(uid)
                        .set(user)
                        .addOnSuccessListener { _registerSuccess.value = true }
                        .addOnFailureListener { _registerSuccess.value = false }
                }
            } else {
                _registerSuccess.value = false
            }
        }
    }
}
