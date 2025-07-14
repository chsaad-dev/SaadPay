package com.example.saadpay.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.R
import com.example.saadpay.presentation.ui.login.LoginActivity
import com.example.saadpay.presentation.ui.main.MainActivity
import com.example.saadpay.presentation.ui.main.home.DashboardFragment
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    @Suppress("DEPRECATION")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            val intent = if (user != null) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 3000)

    }
}
