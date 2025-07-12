package com.example.saadpay.presentation.ui.main.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.saadpay.data.repository.FirestoreRepository
import com.example.saadpay.databinding.FragmentCardBinding

class CardFragment : Fragment() {

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = FirestoreRepository()
        val factory = CardViewModelFactory(repo)
        viewModel = ViewModelProvider(this, factory)[CardViewModel::class.java]

        viewModel.loadUserCard()

        viewModel.card.observe(viewLifecycleOwner) { card ->
            if (card != null) {
                binding.cardHolderName.text = card.cardHolder
                binding.cardNumber.text = card.cardNumber
                binding.expiryDate.text = card.expiryDate
            } else {
                binding.cardHolderName.text = "No Card Found"
                binding.cardNumber.text = ""
                binding.expiryDate.text = ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
