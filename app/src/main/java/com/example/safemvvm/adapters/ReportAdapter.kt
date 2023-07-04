package com.example.safemvvm.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.models.Report

class ReportAdapter(private var reports: List<Report>) :
    RecyclerView.Adapter<ReportAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var reporterName: TextView
        var reportCategory: TextView
        var reportText: TextView

        init {
            reporterName = itemView.findViewById(R.id.reporterName)
            reportCategory = itemView.findViewById(R.id.reportCategory)
            reportText = itemView.findViewById(R.id.report_text)
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.report_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.reporterName.text = reports[position].firstName +" " + reports[position].lastName
        holder.reportCategory.text = holder.reportCategory.text.toString() +" "+  reports[position].category
        holder.reportText.text = holder.reportText.text.toString() +" "+  reports[position].reportText

    }

    override fun getItemCount(): Int {
        return reports.size
    }


}