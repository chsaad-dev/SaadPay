package com.example.saadpay.presentation.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.saadpay.R
import com.example.saadpay.databinding.FragmentDashboardBinding
import com.example.saadpay.presentation.viewmodel.DashboardViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isAdded || _binding == null) return

        viewModel.startListeningToUser()

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            if (!isAdded || _binding == null) return@observe
            binding.welcomeTextView.text = "Welcome, $name"
        }

        viewModel.balance.observe(viewLifecycleOwner) { amount ->
            if (!isAdded || _binding == null) return@observe
            binding.balanceTextView.text = "Rs. %.2f".format(amount)
        }

        binding.loadMoneyButton.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener
            findNavController().navigate(R.id.action_dashboardFragment_to_loadMoneyFragment)
        }

        binding.sendMoneyButton.setOnClickListener {
            if (!isAdded || _binding == null) return@setOnClickListener
            findNavController().navigate(R.id.action_dashboardFragment_to_sendMoneyFragment)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if (!isAdded || _binding == null) return@observe
            Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
