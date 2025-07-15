package com.example.saadpay.presentation.ui.main.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import android.graphics.Color

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.saadpay.R
import com.example.saadpay.databinding.ItemTransactionBinding
import com.example.saadpay.databinding.ItemTransactionHeaderBinding
import com.example.saadpay.domain.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    fun submitGroupedList(list: List<TransactionListItem>) {
        submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.Header -> TYPE_HEADER
            is TransactionListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemTransactionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            TYPE_ITEM -> {
                val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TransactionViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.Header -> (holder as HeaderViewHolder).bind(item.dateLabel)
            is TransactionListItem.Item -> (holder as TransactionViewHolder).bind(item.transaction)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemTransactionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.headerDateTextView.text = date
        }
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.amountTextView.text = "Rs. %.2f".format(transaction.amount)
            binding.dateTextView.text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val label: String
            val iconRes: Int
            val colorHex: String

            when {
                transaction.senderId == currentUserId && transaction.receiverId == currentUserId -> {
                    label = "Loaded from SaadPay"
                    iconRes = R.drawable.ic_wallet
                    colorHex = "#1976D2" // Blue
                }
                transaction.senderId == currentUserId -> {
                    label = "Sent to ${transaction.receiverName.ifBlank { "Unknown" }}"
                    iconRes = R.drawable.ic_send
                    colorHex = "#D32F2F" // Red
                }
                transaction.receiverId == currentUserId -> {
                    label = "Received from ${transaction.senderName.ifBlank { "Unknown" }}"
                    iconRes = R.drawable.ic_received
                    colorHex = "#388E3C" // Green
                }
                else -> {
                    label = "Transaction"
                    iconRes = R.drawable.ic_transaction
                    colorHex = "#555555"
                }
            }

            binding.typeTextView.text = label
            binding.amountTextView.setTextColor(Color.parseColor(colorHex))
            binding.typeTextView.setTextColor(Color.parseColor(colorHex))
            binding.transactionIcon.setImageResource(iconRes)
        }


        private fun getTypeLabel(txn: Transaction): String {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            return when {
                txn.receiverId == currentUserId && txn.senderId == currentUserId -> "Loaded"
                txn.receiverId == currentUserId -> "Received"
                txn.senderId == currentUserId -> "Sent"
                else -> "Transaction"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TransactionListItem>() {
        override fun areItemsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
            return when {
                oldItem is TransactionListItem.Header && newItem is TransactionListItem.Header ->
                    oldItem.dateLabel == newItem.dateLabel
                oldItem is TransactionListItem.Item && newItem is TransactionListItem.Item ->
                    oldItem.transaction.id == newItem.transaction.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
            return oldItem == newItem
        }
    }
}
