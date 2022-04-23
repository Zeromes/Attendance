package com.example.attendance.utils.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R

class StatisticsViewHolder (_itemView: View) : RecyclerView.ViewHolder(_itemView) {
    val recordName : TextView = _itemView.findViewById(R.id.recordName)
    val recordTime : TextView = _itemView.findViewById(R.id.recordTime)

}