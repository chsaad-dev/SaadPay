package com.example.saadpay.presentation.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.data.model.User
import com.example.saadpay.data.repository.FirestoreRepository
import com.example.saadpay.databinding.ActivityLoginBinding
import com.example.saadpay.presentation.ui.main.MainActivity
import com.example.saadpay.presentation.ui.register.RegisterActivity
import com.example.saadpay.presentation.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginUser(email, password)
            } else {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val uid = user.uid
                    val name = user.displayName ?: "Unknown"
                    val email = user.email ?: ""

                    val newUser = User(
                        uid = uid,
                        name = name,
                        email = email,
                        balance = 0.0
                    )

                    FirestoreRepository().saveUserIfNotExists(newUser) { saved ->
                        if (saved) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to sync user data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        viewModel.errorMessage.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()

                if (it.contains("verify your email", ignoreCase = true)) {
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Email Not Verified")
                        .setMessage("Would you like to resend the verification email?")
                        .setPositiveButton("Resend") { _, _ ->
                            viewModel.resendVerificationEmail { success, message ->
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }

    }
}
