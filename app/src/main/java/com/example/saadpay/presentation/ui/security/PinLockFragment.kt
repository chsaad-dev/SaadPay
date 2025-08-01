package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
        if (!isAdded || _binding == null) return
        pinPreferenceManager = PinPreferenceManager(requireContext())

        val storedPin = pinPreferenceManager.getPin()
        if (storedPin == null) {
            if (!isAdded || _binding == null) return
            findNavController().navigate(R.id.action_pinLockFragment_to_setPinFragment)
            return
        }

        setupPinBoxes()

        if (pinPreferenceManager.isBiometricEnabled()) {
            setupBiometricAuth()
        }

        binding.retryBiometric.setOnClickListener {
            if (::biometricPrompt.isInitialized) {
                biometricPrompt.authenticate(promptInfo)
            }
        }

        binding.unlockButton.setOnClickListener {
            validatePinAndNavigate()
        }
    }

    private fun setupPinBoxes() {
        moveFocus(binding.pin1, binding.pin2)
        moveFocus(binding.pin2, binding.pin3)
        moveFocus(binding.pin3, binding.pin4)

        binding.pin4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isAdded || _binding == null) return
                if (getEnteredPin().length == 4) {
                    validatePinAndNavigate()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun moveFocus(from: EditText, to: EditText) {
        from.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isAdded || _binding == null) return
                if (from.text.length == 1) to.requestFocus()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getEnteredPin(): String {
        if (!isAdded || _binding == null) return ""
        return binding.pin1.text.toString() +
                binding.pin2.text.toString() +
                binding.pin3.text.toString() +
                binding.pin4.text.toString()
    }

    private fun clearPinBoxes() {
        if (!isAdded || _binding == null) return
        binding.pin1.text.clear()
        binding.pin2.text.clear()
        binding.pin3.text.clear()
        binding.pin4.text.clear()
        binding.pin1.requestFocus()
    }

    private fun validatePinAndNavigate() {
        if (!isAdded || _binding == null) return
        val enteredPin = getEnteredPin()
        val storedPin = pinPreferenceManager.getPin()

        if (enteredPin == storedPin) {
            Toast.makeText(requireContext(), "Unlocked!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_pinLockFragment_to_dashboardFragment)
        } else {
            Toast.makeText(requireContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show()
            clearPinBoxes()
        }
    }

    private fun setupBiometricAuth() {
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                if (!isAdded || _binding == null) return
                Toast.makeText(requireContext(), "Fingerprint recognized!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_pinLockFragment_to_dashboardFragment)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (!isAdded || _binding == null) return
                Toast.makeText(requireContext(), "Biometric error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                if (!isAdded || _binding == null) return
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
        } else {
            if (!isAdded || _binding == null) return
            binding.retryBiometric.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
