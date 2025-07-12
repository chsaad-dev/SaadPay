package com.example.saadpay.presentation.ui.loadmoney

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.databinding.ActivityLoadMoneyBinding
import com.example.saadpay.presentation.viewmodel.LoadMoneyViewModel

class LoadMoneyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadMoneyBinding
    private val viewModel: LoadMoneyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadMoneyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loadButton.setOnClickListener {
            val amountText = binding.amountEditText.text.toString().trim()
            val amount = amountText.toDoubleOrNull()

            if (!amountText.isNullOrEmpty() && amount != null && amount > 0) {
                viewModel.loadMoney(amount)
            } else {
                Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }


        viewModel.loadSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Money loaded successfully", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Failed to load money", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
