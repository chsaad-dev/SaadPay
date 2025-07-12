package com.example.saadpay.presentation.ui.sendmoney

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.databinding.ActivitySendMoneyBinding
import com.example.saadpay.presentation.viewmodel.SendMoneyViewModel

class SendMoneyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendMoneyBinding
    private val viewModel: SendMoneyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendMoneyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val amountText = binding.amountEditText.text.toString().trim()
            val amount = amountText.toDoubleOrNull()

            if (email.isNotEmpty() && amount != null && amount > 0) {
                viewModel.sendMoney(email, amount)
            } else {
                Toast.makeText(this, "Enter valid email and amount", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }


        viewModel.sendSuccess.observe(this) {
            if (it) {
                Toast.makeText(this, "Money sent successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Transaction failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
