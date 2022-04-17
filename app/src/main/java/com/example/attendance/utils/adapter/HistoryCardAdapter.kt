package com.example.attendance.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.utils.dataClass.HistoryCardData

class HistoryCardAdapter(private val cardList: List<HistoryCardData>) : RecyclerView.Adapter<HistoryCardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryCardViewHolder {
        return HistoryCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history,parent,false))
    }

    override fun onBindViewHolder(holder: HistoryCardViewHolder, position: Int) {
        holder.cardHistoryName.text = cardList[position].eventName
        holder.cardHistoryDate.text = cardList[position].date
        holder.cardHistoryTime.text = cardList[position].time

    }

    override fun getItemCount(): Int {
        return cardList.size
    }

}