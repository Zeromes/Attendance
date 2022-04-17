package com.example.attendance.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.utils.dataClass.EventCardData

class EventCardAdapter(private val cardList: List<EventCardData>) : RecyclerView.Adapter<EventCardViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventCardViewHolder {
        return EventCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_event,parent,false))
    }

    override fun onBindViewHolder(holder: EventCardViewHolder, position: Int) {
        holder.cardEventName.text = cardList[position].name
        holder.cardEventCycle.text = cardList[position].cycle
        holder.cardEventDateOrWeekday.text = cardList[position].dateOrWeekday
        holder.cardEventTime.text = cardList[position].time

    }

    override fun getItemCount(): Int {
        return cardList.size
    }

}