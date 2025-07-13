package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.saadpay.databinding.FragmentChangePinBinding
import com.example.saadpay.utils.PinPreferenceManager

class ChangePinFragment : Fragment() {

    private var _binding: FragmentChangePinBinding? = null
    private val binding get() = _binding!!

    private lateinit var pinManager: PinPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinManager = PinPreferenceManager(requireContext())

        binding.changePinBtn.setOnClickListener {
            val oldPin = binding.oldPinEditText.text.toString()
            val newPin = binding.newPinEditText.text.toString()
            val confirmPin = binding.confirmPinEditText.text.toString()

            when {
                oldPin != pinManager.getPin() -> {
                    Toast.makeText(requireContext(), "Incorrect Old PIN", Toast.LENGTH_SHORT).show()
                }
                newPin != confirmPin -> {
                    Toast.makeText(requireContext(), "New PINs do not match", Toast.LENGTH_SHORT).show()
                }
                newPin.length != 4 || confirmPin.length != 4 -> {
                    Toast.makeText(requireContext(), "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    pinManager.savePin(newPin)
                    Toast.makeText(requireContext(), "PIN changed successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
