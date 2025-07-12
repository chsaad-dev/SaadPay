package com.example.saadpay.presentation.ui.main.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saadpay.R
import com.example.saadpay.domain.model.CardModel

class CardPagerAdapter(
    private val cardList: List<CardModel>,
    private val onEyeClicked: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<CardPagerAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardType: TextView = itemView.findViewById(R.id.cardType)
        private val cardNumber: TextView = itemView.findViewById(R.id.cardNumber)
        private val userName: TextView = itemView.findViewById(R.id.cardUserName)
        private val expiry: TextView = itemView.findViewById(R.id.cardExpiry)
        private val cvv: TextView = itemView.findViewById(R.id.cardCvv)
        private val eyeIcon: ImageView = itemView.findViewById(R.id.eyeIcon)

        fun bind(card: CardModel, position: Int) {
            cardType.text = card.type

            if (card.isVisible) {
                userName.text = card.userName
                cardNumber.text = card.cardNumber
                expiry.text = "Expiry: ${card.expiry}"
                cvv.text = "CVV: ${card.cvv}"
                eyeIcon.setImageResource(R.drawable.ic_eye_open)
            } else {
                userName.text = "SaadPay User"
                cardNumber.text = "•••• •••• •••• ••••"
                expiry.text = "Expiry: ••/••"
                cvv.text = "CVV: •••"
                eyeIcon.setImageResource(R.drawable.ic_eye_closed)
            }

            eyeIcon.setOnClickListener {
                onEyeClicked(position, !card.isVisible)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int = cardList.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cardList[position], position)
    }
}
