package com.example.attendance.utils.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R

class EventCardViewHolder(_itemView: View) : RecyclerView.ViewHolder(_itemView) {
    val cardEventName : TextView = _itemView.findViewById(R.id.cardEventName)
    val cardEventCycle : TextView = _itemView.findViewById(R.id.cardEventCycle)
    val cardEventDateOrWeekday : TextView = _itemView.findViewById(R.id.cardEventDateOrWeekday)
    val cardEventTime : TextView = _itemView.findViewById(R.id.cardEventTime)


}
