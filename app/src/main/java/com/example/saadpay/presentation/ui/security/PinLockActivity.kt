package com.example.saadpay.presentation.ui.security

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saadpay.R
import com.example.saadpay.utils.PinPreferenceManager

class PinLockActivity : AppCompatActivity() {

    private lateinit var pinPreferenceManager: PinPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_lock)

        pinPreferenceManager = PinPreferenceManager(this)

        val pinEditText = findViewById<EditText>(R.id.pinEditText)
        val unlockButton = findViewById<Button>(R.id.unlockButton)

        unlockButton.setOnClickListener {
            val enteredPin = pinEditText.text.toString()
            val storedPin = pinPreferenceManager.getPin()

            if (storedPin == null) {
                Toast.makeText(this, "No PIN set. Please set PIN first.", Toast.LENGTH_SHORT).show()
                finish() // or redirect to SetPinActivity if you want
                return@setOnClickListener
            }

            if (enteredPin == storedPin) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                pinEditText.text.clear()
            }
        }
    }
}
