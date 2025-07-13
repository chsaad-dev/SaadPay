package com.example.saadpay.presentation.ui.main.card

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val userName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                ?: firebaseUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: "SaadPay User"

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
            binding.cardViewPager.apply {
                offscreenPageLimit = 1
                setPageTransformer { page, position ->
                    page.translationX = -32 * position
                    page.scaleY = 1 - (0.1f * kotlin.math.abs(position))
                }
            }


        }

        binding.switchWithdraw.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Withdraw Enabled" else "Withdraw Disabled"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.switchInternational.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "International Transactions Enabled" else "International Transactions Disabled"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.switchInShop.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Enabled In-Shop Transactions" else "Disabled In-Shop Transactions"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.switchBlock.setOnCheckedChangeListener { _, isChecked ->
            val message = if (isChecked) "Your Card is Blocked Temporarily" else "Your Card is Unblocked"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        hideRunnable?.let { autoHideHandler.removeCallbacks(it) }
    }
}
