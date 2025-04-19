package com.example.notifbus.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notifbus.databinding.ListKeluhanCustomerBinding
import com.example.notifbus.model.Keluhan
import com.example.notifbus.model.Ticket

class ListKeluhanAdapter:RecyclerView.Adapter<ListKeluhanAdapter.ViewHolder>() {

    private val mKeluhan = mutableListOf<Keluhan>()

    fun setData(keluhan: MutableList<Keluhan>){
        mKeluhan.clear()
        mKeluhan.addAll(keluhan)
        notifyDataSetChanged()
    }

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(keluhan: Keluhan)
    }

    inner class ViewHolder(val binding: ListKeluhanCustomerBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListKeluhanCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mKeluhan.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(mKeluhan[position]){
                binding.tvListNoTicket.text = this.noTicket
                binding.tvListNamaCustomer.text = this.fullName
                binding.tvListDari.text = this.dari + " - " + this.tujuan
                binding.tvListDateNotification.text = this.kCreateDate


                binding.imgEmailNew.visibility = View.VISIBLE
                binding.imgEmailOpen.visibility = View.GONE

                if (this.kRead == 1){
                    binding.imgEmailNew.visibility = View.GONE
                    binding.imgEmailOpen.visibility = View.VISIBLE
                }

                binding.cvListKeluhan.setOnClickListener { onItemClickCallback?.onItemClicked(this) }
            }
        }
    }
}