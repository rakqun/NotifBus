package com.example.notifbus.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notifbus.databinding.ListPilihPenumpangBinding
import com.example.notifbus.model.Customer

class ListPenumpangAdapter(private val mCustomer: MutableList<Customer>):RecyclerView.Adapter<ListPenumpangAdapter.ViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(customer: Customer)
    }

    inner class ViewHolder(val binding: ListPilihPenumpangBinding):RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListPilihPenumpangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mCustomer.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(mCustomer[position]){
                binding.tvNamaPenumpang.text = this.name
                binding.tvNamaPenumpang.setOnClickListener { onItemClickCallback?.onItemClicked(this) }
            }
        }
    }
}