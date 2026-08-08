package com.example.saadpay.presentation.ui.main.loadmoney

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saadpay.R

class InfoPagerAdapter(private val infoList: List<String>) :
    RecyclerView.Adapter<InfoPagerAdapter.InfoViewHolder>() {

    inner class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val infoText: TextView = itemView.findViewById(R.id.tipTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tip, parent, false)
        return InfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        holder.infoText.text = infoList[position]
    }

    override fun getItemCount(): Int = infoList.size
}
