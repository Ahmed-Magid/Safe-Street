package com.example.safemvvm.views

import ViewReportsViewModel
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.Location
import com.example.safemvvm.models.Report
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.RegistrationViewModel
import com.example.safemvvm.viewmodels.RegistrationViewModelFactory
import com.example.safemvvm.viewmodels.ViewReportsViewModelFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ViewReports : AppCompatActivity() {
    private lateinit var viewModel: ViewReportsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_reports)

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
                println(response.body()!!.message)
                println(response.body()?.data.toString())
                val reports: List<Report> = Gson().fromJson(
                    response.body()?.data.toString(),
                    object : TypeToken<List<Report>>() {}.type
                )
                println(reports)
            }
        }

    }
}