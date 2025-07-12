package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.databinding.ActivityChangePinBinding
import com.example.saadpay.utils.PinPreferenceManager

class ChangePinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePinBinding
    private lateinit var pinManager: PinPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pinManager = PinPreferenceManager(this)

        binding.changePinBtn.setOnClickListener {
            val oldPin = binding.oldPinEditText.text.toString()
            val newPin = binding.newPinEditText.text.toString()
            val confirmPin = binding.confirmPinEditText.text.toString()

            if (oldPin != pinManager.getPin()) {
                Toast.makeText(this, "Old PIN incorrect", Toast.LENGTH_SHORT).show()
            } else if (newPin != confirmPin) {
                Toast.makeText(this, "New PINs do not match", Toast.LENGTH_SHORT).show()
            } else {
                pinManager.savePin(newPin)
                Toast.makeText(this, "PIN changed successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
