package com.example.saadpay.presentation.ui.main.card

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saadpay.R
import com.example.saadpay.domain.model.CardModel

class CardAdapter(private val cardList: List<CardModel>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private var visibleCardPosition: Int? = null
    private var hideHandler: Handler? = null
    private var hideRunnable: Runnable? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        holder.bind(card, position)
    }

    override fun getItemCount(): Int = cardList.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardType: TextView = itemView.findViewById(R.id.cardType)
        private val userName: TextView = itemView.findViewById(R.id.cardUserName) // ✅ fixed
        private val cardNumber: TextView = itemView.findViewById(R.id.cardNumber)
        private val expiry: TextView = itemView.findViewById(R.id.cardExpiry) // ✅ fixed
        private val cvv: TextView = itemView.findViewById(R.id.cardCvv)       // ✅ fixed
        private val eyeIcon: ImageView = itemView.findViewById(R.id.eyeIcon)

        fun bind(card: CardModel, position: Int) {
            cardType.text = card.type
            userName.text = card.userName

            val isVisible = visibleCardPosition == position

            if (isVisible) {
                cardNumber.text = card.cardNumber
                expiry.text = "Expiry: ${card.expiry}"
                cvv.text = "CVV: ${card.cvv}"
                eyeIcon.setImageResource(R.drawable.ic_eye_open)
            } else {
                cardNumber.text = "•••• •••• •••• ••••"
                expiry.text = "Expiry: ••/••"
                cvv.text = "CVV: •••"
                eyeIcon.setImageResource(R.drawable.ic_eye_closed)
            }

            eyeIcon.setOnClickListener {
                if (visibleCardPosition == position) {
                    visibleCardPosition = null
                    hideRunnable?.let { hideHandler?.removeCallbacks(it) }
                    notifyItemChanged(position)
                } else {
                    val previousVisible = visibleCardPosition
                    visibleCardPosition = position
                    notifyItemChanged(previousVisible ?: -1)
                    notifyItemChanged(position)

                    hideRunnable?.let { hideHandler?.removeCallbacks(it) }
                    hideHandler = Handler(Looper.getMainLooper())
                    hideRunnable = Runnable {
                        visibleCardPosition = null
                        notifyItemChanged(position)
                    }
                    hideHandler?.postDelayed(hideRunnable!!, 10_000)
                }
            }
        }
    }
}
