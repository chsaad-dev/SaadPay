package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.saadpay.databinding.FragmentForgotPinBinding
import com.example.saadpay.utils.PinPreferenceManager
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
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        pinManager = PinPreferenceManager(requireContext())

        binding.resetPinBtn.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val newPin = binding.newPinEditText.text.toString()
            val confirmPin = binding.confirmPinEditText.text.toString()

            if (newPin != confirmPin) {
                Toast.makeText(requireContext(), "New PINs do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    pinManager.savePin(newPin)
                    Toast.makeText(requireContext(), "PIN reset successfully", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Auth failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
