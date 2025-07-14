package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.saadpay.R
import com.example.saadpay.databinding.FragmentSetPinBinding
import com.example.saadpay.utils.PinPreferenceManager

class SetPinFragment : Fragment() {

    private var _binding: FragmentSetPinBinding? = null
    private val binding get() = _binding!!

    private lateinit var pinPreferenceManager: PinPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinPreferenceManager = PinPreferenceManager(requireContext())

        setupPinBoxes()

        binding.savePinButton.setOnClickListener {
            val pin = getEnteredPin()
            val confirmPin = getConfirmedPin()

            if (pin.length != 4 || confirmPin.length != 4) {
                Toast.makeText(requireContext(), "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != confirmPin) {
                Toast.makeText(requireContext(), "PINs do not match", Toast.LENGTH_SHORT).show()
                clearPins()
                return@setOnClickListener
            }

            pinPreferenceManager.savePin(pin)
            pinPreferenceManager.setBiometricEnabled(true) // Biometric is now enabled for this user

            Toast.makeText(requireContext(), "PIN saved successfully", Toast.LENGTH_SHORT).show()

            findNavController().navigate(R.id.action_setPinFragment_to_dashboardFragment)

        }
    }

    private fun setupPinBoxes() {
        moveFocus(binding.pin1, binding.pin2)
        moveFocus(binding.pin2, binding.pin3)
        moveFocus(binding.pin3, binding.pin4)

        moveFocus(binding.confirmPin1, binding.confirmPin2)
        moveFocus(binding.confirmPin2, binding.confirmPin3)
        moveFocus(binding.confirmPin3, binding.confirmPin4)
    }

    private fun moveFocus(from: EditText, to: EditText) {
        from.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (from.text.length == 1) to.requestFocus()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getEnteredPin(): String {
        return binding.pin1.text.toString() +
                binding.pin2.text.toString() +
                binding.pin3.text.toString() +
                binding.pin4.text.toString()
    }

    private fun getConfirmedPin(): String {
        return binding.confirmPin1.text.toString() +
                binding.confirmPin2.text.toString() +
                binding.confirmPin3.text.toString() +
                binding.confirmPin4.text.toString()
    }

    private fun clearPins() {
        listOf(
            binding.pin1, binding.pin2, binding.pin3, binding.pin4,
            binding.confirmPin1, binding.confirmPin2, binding.confirmPin3, binding.confirmPin4
        ).forEach { it.text.clear() }

        binding.pin1.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
