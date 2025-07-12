package com.example.saadpay.presentation.ui.transactionhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.saadpay.data.model.Transaction
import com.example.saadpay.databinding.ItemTransactionBinding
import com.example.saadpay.databinding.ItemTransactionHeaderBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<TransactionListItem> = emptyList()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    inner class HeaderViewHolder(private val binding: ItemTransactionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.headerTextView.text = title
        }
    }

    inner class ItemViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(txn: Transaction) {
            binding.typeTextView.text = txn.type
            binding.amountTextView.text = "Rs. %.2f".format(txn.amount)
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            binding.dateTextView.text = sdf.format(Date(txn.timestamp))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
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
                ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TransactionListItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is TransactionListItem.Item -> (holder as ItemViewHolder).bind(item.transaction)
        }
    }

    fun submitGroupedList(transactions: List<TransactionListItem>) {
        items = transactions
        notifyDataSetChanged()
    }
}
