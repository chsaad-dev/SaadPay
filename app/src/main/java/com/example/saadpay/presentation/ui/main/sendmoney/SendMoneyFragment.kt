package com.example.saadpay.presentation.ui.main.sendmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.saadpay.databinding.FragmentSendMoneyBinding
import com.example.saadpay.presentation.ui.main.loadmoney.InfoPagerAdapter
import com.example.saadpay.presentation.viewmodel.SendMoneyViewModel

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
                viewModel.sendMoney(email, amount)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
