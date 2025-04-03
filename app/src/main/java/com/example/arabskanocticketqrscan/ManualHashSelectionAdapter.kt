package com.example.arabskanocticketqrscan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ManualHashSelectionAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ManualHashSelectionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thvTicket: TicketHashView = itemView.findViewById(R.id.thvTicket)
    }

    var ticketHashes: List<String>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (ticketHashes != null) {
            val curItem = ticketHashes!![position]
            holder.thvTicket.apply {
                ticketHash = curItem
                setOnClickListener {
                    onItemClick(ticketHash.toString())
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (ticketHashes != null) ticketHashes!!.size else 0
    }
}