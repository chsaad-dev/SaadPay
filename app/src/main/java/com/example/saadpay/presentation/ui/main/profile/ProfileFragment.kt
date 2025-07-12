package com.example.saadpay.presentation.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.saadpay.databinding.FragmentProfileBinding
import com.example.saadpay.presentation.ui.login.LoginActivity
import com.example.saadpay.presentation.ui.security.ChangePinFragment
import com.example.saadpay.presentation.ui.security.ForgotPinFragment
import com.example.saadpay.utils.PinPreferenceManager
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var pinPreferenceManager: PinPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinPreferenceManager = PinPreferenceManager(requireContext())

        val isEnabled = pinPreferenceManager.isBiometricEnabled()
        binding.biometricSwitch.isChecked = isEnabled
        updateStatusText(isEnabled)

        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            pinPreferenceManager.setBiometricEnabled(isChecked)
            updateStatusText(isChecked)
            Toast.makeText(
                requireContext(),
                "Fingerprint ${if (isChecked) "enabled" else "disabled"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.changePinCard.setOnClickListener {
            navigateTo(ChangePinFragment())
        }

        binding.forgotPinCard.setOnClickListener {
            navigateTo(ForgotPinFragment())
        }

        binding.logoutCard.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun updateStatusText(isEnabled: Boolean) {
        binding.fingerprintStatusTextView.text = if (isEnabled) {
            "Fingerprint is enabled ✅"
        } else {
            "Fingerprint is disabled ❌"
        }
    }

    private fun navigateTo(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(com.example.saadpay.R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
