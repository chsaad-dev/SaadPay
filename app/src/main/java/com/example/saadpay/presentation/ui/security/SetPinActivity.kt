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

class SetPinActivity : AppCompatActivity() {

    private lateinit var pinPreferenceManager: PinPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_pin)

        pinPreferenceManager = PinPreferenceManager(this)

        val pinEditText = findViewById<EditText>(R.id.pinEditText)
        val confirmPinEditText = findViewById<EditText>(R.id.confirmPinEditText)
        val savePinButton = findViewById<Button>(R.id.savePinButton)

        savePinButton.setOnClickListener {
            val pin = pinEditText.text.toString()
            val confirmPin = confirmPinEditText.text.toString()

            if (pin.length != 4 || confirmPin.length != 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != confirmPin) {
                Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            pinPreferenceManager.savePin(pin)
            Toast.makeText(this, "PIN saved successfully", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
