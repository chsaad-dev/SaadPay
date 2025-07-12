package com.example.saadpay.presentation.ui.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.saadpay.databinding.FragmentDashboardBinding
import com.example.saadpay.presentation.ui.main.loadmoney.LoadMoneyActivity
import com.example.saadpay.presentation.ui.login.LoginActivity
import com.example.saadpay.presentation.ui.security.ChangePinFragment
import com.example.saadpay.presentation.ui.security.ForgotPinFragment
import com.example.saadpay.presentation.ui.main.sendmoney.SendMoneyFragment
import com.example.saadpay.presentation.ui.main.transaction.TransactionHistoryFragment
import com.example.saadpay.presentation.viewmodel.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth

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
            binding.balanceTextView.text = "Balance: Rs. %.2f".format(amount)
        }

        binding.loadMoneyButton.setOnClickListener {
            startActivity(Intent(requireContext(), LoadMoneyActivity::class.java))
        }

        binding.sendMoneyButton.setOnClickListener {
            startActivity(Intent(requireContext(), SendMoneyFragment::class.java))
        }

        binding.transactionHistoryButton.setOnClickListener {
            startActivity(Intent(requireContext(), TransactionHistoryFragment::class.java))
        }

        binding.changePinButton.setOnClickListener {
            startActivity(Intent(requireContext(), ChangePinFragment::class.java))
        }

        binding.forgotPinButton.setOnClickListener {
            startActivity(Intent(requireContext(), ForgotPinFragment::class.java))
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
