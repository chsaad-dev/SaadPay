package com.example.saadpay.presentation.ui.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.saadpay.R
import com.example.saadpay.databinding.FragmentProfileBinding
import com.example.saadpay.presentation.ui.login.LoginActivity
import com.example.saadpay.presentation.viewmodel.ProfileViewModel
import com.example.saadpay.utils.PinPreferenceManager
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var pinPreferenceManager: PinPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isAdded || _binding == null) return

        pinPreferenceManager = PinPreferenceManager(requireContext())

        binding.profileImageView.setImageResource(R.drawable.ic_person)

        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (!isAdded || _binding == null || user == null) return@observe
            binding.profileNameTextView.text = user.name.ifEmpty { "N/A" }
            binding.profileEmailTextView.text = user.email.ifEmpty { "N/A" }
        }
        viewModel.fetchUserProfile()

        val isEnabled = pinPreferenceManager.isBiometricEnabled()
        binding.biometricSwitch.isChecked = isEnabled
        updateStatusText(isEnabled)

        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isAdded || _binding == null) return@setOnCheckedChangeListener
            pinPreferenceManager.setBiometricEnabled(isChecked)
            updateStatusText(isChecked)
            Toast.makeText(
                requireContext(),
                "Fingerprint ${if (isChecked) "enabled" else "disabled"}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.changePinCard.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener
            findNavController().navigate(R.id.action_profileFragment_to_changePinFragment)
        }

        binding.forgotPinCard.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener
            findNavController().navigate(R.id.action_profileFragment_to_forgotPinFragment)
        }

        binding.helpSupportCard.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("saadw7751@gmail.com"))
                setPackage("com.google.android.gm")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gmail app not found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.termsPrivacyCard.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener
            findNavController().navigate(R.id.action_profileFragment_to_termsPrivacyFragment)
        }

        try {
            val versionName = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
            val versionCode = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).longVersionCode
            binding.appVersionTextView.text = "Version $versionName (Build $versionCode)"
        } catch (e: Exception) {
            if (isAdded) Toast.makeText(requireContext(), "Version info error", Toast.LENGTH_SHORT).show()
        }

        binding.logoutCard.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun updateStatusText(isEnabled: Boolean) {
        if (!isAdded || _binding == null) return
        binding.fingerprintStatusTextView.text =
            if (isEnabled) "Fingerprint is enabled ✅" else "Fingerprint is disabled ❌"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
