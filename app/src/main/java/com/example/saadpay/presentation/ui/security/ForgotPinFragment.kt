package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.saadpay.databinding.FragmentForgotPinBinding
import com.example.saadpay.utils.PinPreferenceManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ForgotPinFragment : Fragment() {

    private var _binding: FragmentForgotPinBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var pinManager: PinPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isAdded || _binding == null) return

        auth = FirebaseAuth.getInstance()
        pinManager = PinPreferenceManager(requireContext())

        binding.resetPinBtn.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener

            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val newPin = binding.newPinEditText.text.toString()
            val confirmPin = binding.confirmPinEditText.text.toString()

            if (email.isEmpty() || password.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPin != confirmPin) {
                Toast.makeText(requireContext(), "New PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
                Toast.makeText(requireContext(), "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.resetPinBtn.isEnabled = false
            binding.resetPinBtn.text = "Resetting..."

            val user = auth.currentUser
            val credential = EmailAuthProvider.getCredential(email, password)

            if (user != null && user.email == email) {
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        if (!isAdded || _binding == null) return@addOnSuccessListener
                        pinManager.savePin(newPin)
                        Toast.makeText(requireContext(), "PIN reset successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    .addOnFailureListener {
                        if (!isAdded || _binding == null) return@addOnFailureListener
                        Toast.makeText(requireContext(), "Auth failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (!isAdded || _binding == null) return@addOnCompleteListener
                        binding.resetPinBtn.isEnabled = true
                        binding.resetPinBtn.text = "Reset PIN"
                    }
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        if (!isAdded || _binding == null) return@addOnSuccessListener
                        pinManager.savePin(newPin)
                        Toast.makeText(requireContext(), "PIN reset successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                    .addOnFailureListener {
                        if (!isAdded || _binding == null) return@addOnFailureListener
                        Toast.makeText(requireContext(), "Auth failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        if (!isAdded || _binding == null) return@addOnCompleteListener
                        binding.resetPinBtn.isEnabled = true
                        binding.resetPinBtn.text = "Reset PIN"
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
