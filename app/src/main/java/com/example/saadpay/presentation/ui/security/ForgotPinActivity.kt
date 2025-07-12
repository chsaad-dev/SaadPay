package com.example.saadpay.presentation.ui.security

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.databinding.ActivityForgotPinBinding
import com.example.saadpay.utils.PinPreferenceManager
import com.google.firebase.auth.FirebaseAuth

class ForgotPinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPinBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var pinManager: PinPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        pinManager = PinPreferenceManager(this)

        binding.resetPinBtn.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val newPin = binding.newPinEditText.text.toString()
            val confirmPin = binding.confirmPinEditText.text.toString()

            if (newPin != confirmPin) {
                Toast.makeText(this, "New PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    pinManager.savePin(newPin)
                    Toast.makeText(this, "PIN reset successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PinLockActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Auth failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
