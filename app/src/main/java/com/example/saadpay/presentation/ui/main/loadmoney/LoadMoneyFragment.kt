package com.example.saadpay.presentation.ui.main.loadmoney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.saadpay.databinding.FragmentLoadMoneyBinding
import com.example.saadpay.presentation.viewmodel.LoadMoneyViewModel

class LoadMoneyFragment : Fragment() {

    private var _binding: FragmentLoadMoneyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoadMoneyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoadMoneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadButton.setOnClickListener {
            val amountText = binding.amountEditText.text.toString().trim()
            val amount = amountText.toDoubleOrNull()

            if (!amountText.isNullOrEmpty() && amount != null && amount > 0) {
                viewModel.loadMoney(amount)
            } else {
                Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.loadSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Money loaded successfully", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Failed to load money", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
