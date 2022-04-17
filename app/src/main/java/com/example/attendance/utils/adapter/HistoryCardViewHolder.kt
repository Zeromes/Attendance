package com.example.attendance.utils.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R

class HistoryCardViewHolder(_itemView: View) : RecyclerView.ViewHolder(_itemView) {
    val cardHistoryName : TextView = _itemView.findViewById(R.id.cardHistoryName)
    val cardHistoryDate : TextView = _itemView.findViewById(R.id.cardHistoryDate)
    val cardHistoryTime : TextView = _itemView.findViewById(R.id.cardHistoryTime)

}