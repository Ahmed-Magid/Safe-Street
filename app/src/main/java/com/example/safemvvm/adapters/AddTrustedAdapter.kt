package com.example.safemvvm.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.models.TrustedContact

class AddTrustedAdapter(private var contacts: List<TrustedContact>) :
    RecyclerView.Adapter<AddTrustedAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName: TextView = itemView.findViewById(R.id.name)
        val contactEmail: TextView = itemView.findViewById(R.id.email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.trusted_contacts_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.contactName.text = contacts[position].name
        holder.contactEmail.text = contacts[position].email
    }

    override fun getItemCount(): Int {
        return contacts.size
    }
}