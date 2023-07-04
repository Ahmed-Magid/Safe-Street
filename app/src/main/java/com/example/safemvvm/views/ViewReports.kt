package com.example.safemvvm.views

import ViewReportsViewModel
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.adapters.ReportAdapter
import com.example.safemvvm.models.Location
import com.example.safemvvm.models.Report
import com.example.safemvvm.models.TrustedContact
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.RegistrationViewModel
import com.example.safemvvm.viewmodels.RegistrationViewModelFactory
import com.example.safemvvm.viewmodels.ViewReportsViewModelFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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


        viewModel.getLocationReportsResponse.observe(this) { response ->
                if (response.isSuccessful && response.body() != null) {
                    val responseMessage = response.body()?.message
                    if (responseMessage == "Executed Successfully") {
                        Log.d("LocationReports001", " ${response.body()}" )
                        val apiReports: List<Report> = Gson().fromJson(response.body()?.data.toString(), object : TypeToken<List<Report>>() {}.type)
                        reports.addAll(apiReports)
                        adapter.notifyDataSetChanged()

                    } else if (responseMessage == "Authentication Error") {
                       Log.d("LocationReports002", "${response.body()}" )
                       Toast.makeText(this, "Session Expired", Toast.LENGTH_LONG).show()
                        localDB.edit().apply {
                            putString("token", "empty")
                            apply()
                        }
                        val intent = Intent(
                            this,
                            Login::class.java
                        )
                        intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } else if (responseMessage == "No Reports Found") {
                        Toast.makeText(this, "No reports to show in this location", Toast.LENGTH_LONG).show()
                    }

                } else {
                    Log.d("LocationReports004", "${response.errorBody()}" )
                    Log.d("LocationReports005", "${response.body()}" )
                    Log.d("LocationReports006", "${response.code()}" )
                    if(response.code()==403 || response.code()==410){
                        Log.d("LocationReports006", "code is 403 or 410")
                        Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }else{
                        Toast.makeText(
                            this,
                            "something went wrong please try again later",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

    }
}