package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.R.*
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.HomeViewModel
import com.example.safemvvm.viewmodels.HomeViewModelFactory
import com.google.gson.Gson

class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_home)

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)

        val repository = Repository()
        val viewModelFactory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(HomeViewModel::class.java)

        viewModel.getNumOfTrusted("Bearer $token",userId)

        viewModel.numOfContactsResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Executed Successfully") {
                    val data = Gson().fromJson(response.body()?.data.toString(), Int::class.java)
                    Log.d("Arwa success to num of contacts","$data")
                    if(data == 0) {
                        Toast.makeText(this, "please add trusted contacts", Toast.LENGTH_LONG).show()
                        Intent(this,ViewTrustedContacts::class.java).also { startActivity(it) }
                    }
                }else {
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    Log.d(
                        "Arwa num of contacts auth error",
                        responseMessage.toString()
                    )

                }
            } else {
                Toast.makeText(
                    this,
                    "something went wrong please try again later",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("Arwa not success noc", response.errorBody().toString())
                Log.d("Arwa not success noc", "${response.code()}")
            }
        }

        viewModel.logoutResponse.observe(this) { response ->
            if (response.isSuccessful || response.code()==403 || response.code()==410) {
                Log.d("Home001","${response.code()}")
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            } else {
                Log.d("Home002","${response.code()}")
                Toast.makeText(
                    this,
                    "something went wrong please try again later",
                    Toast.LENGTH_LONG
                ).show()

            }
        }

        val buttonLogout = findViewById<Button>(id.btn_logout)
        buttonLogout.setOnClickListener {

            if (token != null) {
                viewModel.logout("Bearer $token", IdBody(userId))
            }
        }

        /*val buttonSignUp = findViewById<Button>(id.btn_createTrip)
        buttonSignUp.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivity(intent)
        }*/
        val buttonViewTrustedContact = findViewById<Button>(id.btn_viewTrusted)
        buttonViewTrustedContact.setOnClickListener {
            val intent = Intent(this, ViewTrustedContacts::class.java)
            startActivity(intent)
        }
        val imageViewLogo = findViewById<ImageView>(id.logo)
        imageViewLogo.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
        val buttonCreatetrip = findViewById<Button>(R.id.btn_createTrip)
        buttonCreatetrip.setOnClickListener {
            val intent = Intent(this, CreateTripActivity::class.java)
            startActivity(intent)
        }
        val buttonReportActivity = findViewById<Button>(R.id.btn_reportLocation)
        buttonReportActivity.setOnClickListener {
            val intent = Intent(this, ReportLocationMap::class.java)
            startActivity(intent)
        }
    }
}