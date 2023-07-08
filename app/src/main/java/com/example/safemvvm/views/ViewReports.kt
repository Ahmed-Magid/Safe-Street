package com.example.safemvvm.views

import ViewReportsViewModel
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.adapters.ReportAdapter
import com.example.safemvvm.models.Report
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.ViewReportsViewModelFactory

class ViewReports : AppCompatActivity() {
    private lateinit var viewModel: ViewReportsViewModel

    private lateinit var recyclerView: RecyclerView

    var reports: MutableList<Report> = mutableListOf()
    val adapter = ReportAdapter(reports)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_reports)


        recyclerView = findViewById(R.id.reportRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = adapter

        val repository = Repository()
        val viewModelFactory = ViewReportsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ViewReportsViewModel::class.java)

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val userId = localDB.getInt("userId", -1)



        val longitude = intent.getStringExtra("longitude")
        val latitude = intent.getStringExtra("latitude")
        println(longitude)
        println(latitude)
        viewModel.getLocationReports("Bearer $token", userId, longitude.toString(), latitude.toString())
        observeResponses()
    }
    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.getLocationReportsResponse,
            Array<Report>::class.java,
            {
                it.forEach { report -> report.reportText = report.reportText.replace("@", " ") }
                reports.addAll(it)
                adapter.notifyDataSetChanged()
            },
            {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
    }
}