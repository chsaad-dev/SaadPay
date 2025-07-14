package com.example.saadpay.presentation.ui.security

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.saadpay.R
import com.example.saadpay.utils.PinPreferenceManager

class PinRouterFragment : Fragment(R.layout.fragment_blank) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pinManager = PinPreferenceManager(requireContext())

        if (pinManager.isPinSet()) {
            // Navigate to PIN unlock if PIN is already set
            findNavController().navigate(R.id.action_pinRouterFragment_to_pinLockFragment)
        } else {
            // Navigate to set PIN if not set yet
            findNavController().navigate(R.id.action_pinRouterFragment_to_setPinFragment)
        }
    }
}
