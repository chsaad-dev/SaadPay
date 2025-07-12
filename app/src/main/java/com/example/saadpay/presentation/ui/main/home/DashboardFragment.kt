package com.example.saadpay.presentation.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.saadpay.databinding.FragmentDashboardBinding
import com.example.saadpay.presentation.ui.main.loadmoney.LoadMoneyFragment
import com.example.saadpay.presentation.ui.main.sendmoney.SendMoneyFragment
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
        super.onViewCreated(view, savedInstanceState)

        viewModel.startListeningToUser()

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.welcomeTextView.text = "Welcome, $name"
        }

        viewModel.balance.observe(viewLifecycleOwner) { amount ->
            binding.balanceTextView.text = "Rs. %.2f".format(amount)
        }

        binding.loadMoneyButton.setOnClickListener {
            navigateTo(LoadMoneyFragment())
        }

        binding.sendMoneyButton.setOnClickListener {
            navigateTo(SendMoneyFragment())
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
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
