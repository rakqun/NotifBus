package com.example.notifbus.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notifbus.databinding.ListCustomerBinding
import com.example.notifbus.model.Customer

class CustomerAdapter():RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {
    private val mCustomer = mutableListOf<Customer>()

    fun setData(customer: MutableList<Customer>){
        mCustomer.clear()
        mCustomer.addAll(customer)
        notifyDataSetChanged()
    }

    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(customer: Customer)
    }

    inner class ViewHolder(val binding : ListCustomerBinding):RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListCustomerBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mCustomer.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(mCustomer[position]){

                if (this.gender == "Perempuan"){
                    binding.imgCustomerWomen.visibility = View.VISIBLE
                    binding.imgCustomerBoy.visibility = View.GONE
                }else{
                    binding.imgCustomerWomen.visibility = View.GONE
                    binding.imgCustomerBoy.visibility = View.VISIBLE
                }

                binding.tvNamaLengkapTicket.text = this.name
                binding.tvEmailCustomer.text = this.email
                binding.tvNoHpTicket.text = this.noHp

                binding.layoutListCustomer.setOnClickListener { onItemClickCallback?.onItemClicked(this) }
            }
        }
    }
}