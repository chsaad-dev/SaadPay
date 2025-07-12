package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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

        binding.savePinButton.setOnClickListener {
            val pin = binding.pinEditText.text.toString()
            val confirmPin = binding.confirmPinEditText.text.toString()

            if (pin.length != 4 || confirmPin.length != 4) {
                Toast.makeText(requireContext(), "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != confirmPin) {
                Toast.makeText(requireContext(), "PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            pinPreferenceManager.savePin(pin)
            Toast.makeText(requireContext(), "PIN saved successfully", Toast.LENGTH_SHORT).show()

            // Navigate back or pop this fragment
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
