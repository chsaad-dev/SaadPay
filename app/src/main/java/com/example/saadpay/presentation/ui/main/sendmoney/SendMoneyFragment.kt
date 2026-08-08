package com.example.saadpay.presentation.ui.main.sendmoney

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.saadpay.databinding.FragmentSendMoneyBinding
import com.example.saadpay.presentation.ui.main.loadmoney.InfoPagerAdapter
import com.example.saadpay.presentation.viewmodel.SendMoneyViewModel
import com.example.saadpay.utils.PinPreferenceManager

class SendMoneyFragment : Fragment() {

    private var _binding: FragmentSendMoneyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SendMoneyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendMoneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isAdded || _binding == null) return

        val tips = listOf(
            "💡 Always double-check the recipient’s email.\n\n🚀 Transfers are instant and cannot be reversed.\n\n🔄 Make sure you enter the correct amount.",
            "🔐 Never share your password or OTP.\n\n✅ Verify the email before hitting Send.\n\n🛑 Avoid sending to unknown users."
        )

        binding.tipsViewPager.adapter = InfoPagerAdapter(tips)
        binding.tipsViewPager.apply {
            offscreenPageLimit = 1
            setPageTransformer { page, position ->
                page.translationX = -32 * position
                page.scaleY = 1 - (0.1f * kotlin.math.abs(position))
            }
        }

        binding.sendButton.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener

            val email = binding.emailEditText.text.toString().trim()
            val amountText = binding.amountEditText.text.toString().trim()
            val amount = amountText.toDoubleOrNull()

            if (email.isNotEmpty() && amount != null && amount > 0) {
                authenticateAndSendMoney(email, amount)
            } else {
                Toast.makeText(requireContext(), "Enter valid email and amount", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            if (!isAdded || _binding == null) return@observe
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.sendSuccess.observe(viewLifecycleOwner) { success ->
            if (!isAdded || _binding == null) return@observe
            if (success) {
                Toast.makeText(requireContext(), "Money sent successfully", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Transaction failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticateAndSendMoney(email: String, amount: Double) {
        val pinManager = PinPreferenceManager(requireContext())
        if (!pinManager.isPinSet()) {
            Toast.makeText(requireContext(), "Please set a security PIN first before sending money", Toast.LENGTH_LONG).show()
            return
        }

        if (pinManager.isBiometricEnabled()) {
            val biometricManager = BiometricManager.from(requireContext())
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS) {
                val executor = ContextCompat.getMainExecutor(requireContext())
                val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        if (!isAdded || _binding == null) return
                        viewModel.sendMoney(email, amount)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (!isAdded || _binding == null) return
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            showPinConfirmationDialog(pinManager, email, amount)
                        } else {
                            Toast.makeText(requireContext(), "Biometric error: $errString", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        if (!isAdded || _binding == null) return
                        Toast.makeText(requireContext(), "Fingerprint not recognized", Toast.LENGTH_SHORT).show()
                    }
                })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Confirm Money Transfer")
                    .setSubtitle("Confirm sending Rs. %.2f to %s".format(amount, email))
                    .setNegativeButtonText("Use PIN")
                    .build()

                biometricPrompt.authenticate(promptInfo)
                return
            }
        }

        showPinConfirmationDialog(pinManager, email, amount)
    }

    private fun showPinConfirmationDialog(pinManager: PinPreferenceManager, email: String, amount: Double) {
        val pinInput = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter 4-digit PIN"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm PIN")
            .setMessage("Enter your 4-digit PIN to confirm sending Rs. %.2f to %s".format(amount, email))
            .setView(pinInput)
            .setPositiveButton("Confirm") { _, _ ->
                val enteredPin = pinInput.text.toString()
                if (enteredPin == pinManager.getPin()) {
                    viewModel.sendMoney(email, amount)
                } else {
                    Toast.makeText(requireContext(), "Incorrect PIN. Transfer cancelled.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(requireContext(), "Transfer cancelled.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
