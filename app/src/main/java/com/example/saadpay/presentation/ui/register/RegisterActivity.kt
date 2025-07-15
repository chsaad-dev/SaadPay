package com.example.saadpay.presentation.ui.register

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.databinding.ActivityRegisterBinding
import com.example.saadpay.presentation.ui.login.LoginActivity
import com.example.saadpay.presentation.ui.main.profile.TermsPrivacyFragment
import com.example.saadpay.presentation.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTermsText()

        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.length < 6) {
                Toast.makeText(this, "Fill all fields (password ≥ 6 chars)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!binding.termsCheckbox.isChecked) {
                Toast.makeText(this, "Please accept the Terms & Privacy Policy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.registerUser(name, email, password)
        }

        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewModel.registerSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Verification email sent. Please check your Inbox or Spam Folder.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTermsText() {
        val fullText = "I accept the Terms & Privacy Policy"
        val termsStart = fullText.indexOf("Terms")
        val termsEnd = fullText.length

        val spannable = SpannableString(fullText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, TermsPrivacyFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        spannable.setSpan(clickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.termsTextView.text = spannable
        binding.termsTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}
