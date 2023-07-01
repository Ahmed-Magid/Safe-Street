package com.example.safemvvm.adapters

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.models.TrustedContact
import com.example.safemvvm.viewmodels.TrustedContactViewModel
import com.example.safemvvm.views.Login
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AddTrustedAdapter(private var contacts: List<TrustedContact>,private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AddTrustedAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var contactName: TextView
        var contactEmail: TextView
        var deleteButton: Button

        init {
            contactName= itemView.findViewById(R.id.name)
            contactEmail= itemView.findViewById(R.id.email)
            deleteButton= itemView.findViewById(R.id.btn_deleteContact)

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(position)
                }
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.trusted_contacts_item, parent, false)
        return MyViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.contactName.text = contacts[position].firstName +" " + contacts[position].lastName
        holder.contactEmail.text = contacts[position].email
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
    }


}