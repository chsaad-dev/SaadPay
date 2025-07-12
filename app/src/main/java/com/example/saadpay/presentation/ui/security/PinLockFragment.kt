package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.saadpay.R
import com.example.saadpay.databinding.FragmentPinLockBinding
import com.example.saadpay.utils.PinPreferenceManager
import java.util.concurrent.Executor

class PinLockFragment : Fragment() {

    private var _binding: FragmentPinLockBinding? = null
    private val binding get() = _binding!!

    private lateinit var pinPreferenceManager: PinPreferenceManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPinLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pinPreferenceManager = PinPreferenceManager(requireContext())

        if (pinPreferenceManager.isBiometricEnabled()) {
            setupBiometricAuth()
        }

        binding.unlockButton.setOnClickListener {
            val enteredPin = binding.pinEditText.text.toString()
            val storedPin = pinPreferenceManager.getPin()

            if (storedPin == null) {
                Toast.makeText(requireContext(), "No PIN set. Please set PIN first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (enteredPin == storedPin) {
                Toast.makeText(requireContext(), "Unlocked!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_pinLockFragment_to_dashboardFragment)
            } else {
                Toast.makeText(requireContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show()
                binding.pinEditText.text.clear()
            }
        }
    }

    private fun setupBiometricAuth() {
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(requireContext(), "Fingerprint recognized!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_pinLockFragment_to_dashboardFragment)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(requireContext(), "Biometric error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(requireContext(), "Fingerprint not recognized", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Unlock")
            .setSubtitle("Use your fingerprint to unlock")
            .setNegativeButtonText("Use PIN instead")
            .build()

        val biometricManager = BiometricManager.from(requireContext())
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
