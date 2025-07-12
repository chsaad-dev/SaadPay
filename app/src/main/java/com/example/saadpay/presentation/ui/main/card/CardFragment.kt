package com.example.saadpay.presentation.ui.main.card

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.saadpay.databinding.FragmentCardsBinding
import com.example.saadpay.domain.model.CardModel
import com.google.firebase.auth.FirebaseAuth

class CardFragment : Fragment() {

    private var _binding: FragmentCardsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CardPagerAdapter
    private val cards = mutableListOf<CardModel>()

    private val autoHideHandler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "SaadPay User"

        cards.add(CardModel("Virtual", userName))
        cards.add(CardModel("Physical", userName))

        adapter = CardPagerAdapter(cards) { position, isVisible ->
            cards[position].isVisible = isVisible
            adapter.notifyItemChanged(position)

            if (isVisible) {
                hideRunnable?.let { autoHideHandler.removeCallbacks(it) }
                hideRunnable = Runnable {
                    cards[position].isVisible = false
                    adapter.notifyItemChanged(position)
                }
                autoHideHandler.postDelayed(hideRunnable!!, 10_000)
            }
        }

        binding.cardViewPager.adapter = adapter

        binding.switchWithdraw.setOnCheckedChangeListener { _, isChecked -> }
        binding.switchInternational.setOnCheckedChangeListener { _, isChecked -> }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        hideRunnable?.let { autoHideHandler.removeCallbacks(it) }
    }
}