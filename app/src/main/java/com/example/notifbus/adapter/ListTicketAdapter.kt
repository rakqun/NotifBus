package com.example.notifbus.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notifbus.databinding.ListTicketBinding
import com.example.notifbus.model.Customer
import com.example.notifbus.model.Ticket

class ListTicketAdapter():RecyclerView.Adapter<ListTicketAdapter.ViewHolder>() {

    private val mTicket: MutableList<Ticket> = mutableListOf()

    fun setData(ticket: MutableList<Ticket>){
        mTicket.clear()
        mTicket.addAll(ticket)
        notifyDataSetChanged()
    }

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(ticket: Ticket)
    }

    inner class ViewHolder(val binding: ListTicketBinding):RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mTicket.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(mTicket[position]){
                binding.tvListNoTicket.text = this.ticketNo
                binding.tvNamaLengkapTicket.text = this.custName
                binding.tvNoHpTicket.text = this.custPhone
                binding.tvTujuaTicket.text = this.tDari + " - " + this.tTujuan
                binding.tvTglBerangkatTicket.text = this.tTglKeberangkatan

                binding.cvListTicket.setOnClickListener { onItemClickCallback?.onItemClicked(this) }
            }
        }
    }
}