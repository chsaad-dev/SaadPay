package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.saadpay.databinding.FragmentPinLockBinding
import com.example.saadpay.utils.PinPreferenceManager

class PinLockFragment : Fragment() {

    private var _binding: FragmentPinLockBinding? = null
    private val binding get() = _binding!!

    private lateinit var pinPreferenceManager: PinPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinPreferenceManager = PinPreferenceManager(requireContext())

        binding.unlockButton.setOnClickListener {
            val enteredPin = binding.pinEditText.text.toString()
            val storedPin = pinPreferenceManager.getPin()

            if (storedPin == null) {
                Toast.makeText(requireContext(), "No PIN set. Please set PIN first.", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
                return@setOnClickListener
            }

            if (enteredPin == storedPin) {
                Toast.makeText(requireContext(), "Unlocked!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show()
                binding.pinEditText.text.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
