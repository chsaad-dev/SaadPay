package com.example.saadpay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.presentation.ui.dashboard.DashboardActivity
import com.example.saadpay.presentation.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is already logged in, go to Dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            // User not logged in, go to Login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Prevent user from returning to MainActivity
    }
}
