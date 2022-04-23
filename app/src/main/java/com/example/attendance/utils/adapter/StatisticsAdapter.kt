package com.example.attendance.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.utils.dataClass.HistoryCardData
import com.example.attendance.utils.dataClass.StatisticsData

class StatisticsAdapter (private val cardList: List<StatisticsData>) : RecyclerView.Adapter<StatisticsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        return StatisticsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_statistics,parent,false))
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        holder.recordName.text = cardList[position].name
        holder.recordTime.text = cardList[position].time

    }

    override fun getItemCount(): Int {
        return cardList.size
    }

}