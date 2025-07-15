package com.example.saadpay.presentation.ui.main.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.saadpay.R
import com.example.saadpay.databinding.FragmentProfileBinding
import com.example.saadpay.presentation.ui.login.LoginActivity
import com.example.saadpay.presentation.ui.security.ChangePinFragment
import com.example.saadpay.presentation.ui.security.ForgotPinFragment
import com.example.saadpay.presentation.ui.main.profile.TermsPrivacyFragment
import com.example.saadpay.utils.PinPreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var pinPreferenceManager: PinPreferenceManager
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        pinPreferenceManager = PinPreferenceManager(requireContext())

        // Set default profile picture (avatar)
        binding.profileImageView.setImageResource(R.drawable.ic_person)

        // Fetch user info from Firestore
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "N/A"
                        val email = document.getString("email") ?: "N/A"
                        binding.profileNameTextView.text = name
                        binding.profileEmailTextView.text = email
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load profile info", Toast.LENGTH_SHORT).show()
                }
        }

        // Biometric switch
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

        binding.helpSupportCard.setOnClickListener {
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
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, TermsPrivacyFragment())
                .addToBackStack(null)
                .commit()
        }

        val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionName = pInfo.versionName
        val versionCode = pInfo.longVersionCode

        binding.appVersionTextView.text = "Version $versionName (Build $versionCode)"

        binding.logoutCard.setOnClickListener {
            auth.signOut()
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
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
