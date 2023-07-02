package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.EmergenciesViewModel
import com.example.safemvvm.viewmodels.EmergenciesViewModelFactory
import com.example.safemvvm.viewmodels.HomeViewModel
import com.example.safemvvm.viewmodels.HomeViewModelFactory
import com.google.gson.Gson

class Emergencies : AppCompatActivity() {
    private lateinit var viewModel: EmergenciesViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergencies)

        val repository = Repository()
        val viewModelFactory = EmergenciesViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(EmergenciesViewModel::class.java)

        val kidnappingButton = findViewById<Button>(R.id.btn_kidnapping)
        val harassmentButton = findViewById<Button>(R.id.btn_harassment)
        val fireButton = findViewById<Button>(R.id.btn_fire)
        val carFaultButton = findViewById<Button>(R.id.btn_carFault)

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)

        kidnappingButton.setOnClickListener {
            viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, 0.0, 0.0, "Murder"))
        }

        harassmentButton.setOnClickListener {
            viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, 0.0, 0.0, "Harassment"))
        }

        fireButton.setOnClickListener {
            viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, 0.0, 0.0, "Robbery"))
        }

        carFaultButton.setOnClickListener {
            viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, 0.0, 0.0, "Accident"))
        }

        viewModel.fireEmergencyResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Created Successfully") {
                    Log.d(
                        "emergency001",
                        responseMessage.toString()
                    )
                    Toast.makeText(this, "Emergency fired successfully", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    Log.d(
                        "emergency002",
                        responseMessage.toString()
                    )
                }
            } else {

                Log.d("emergency004", response.errorBody().toString())
                Log.d("emergency005", "${response.code()}")
                if(response.code()==403 || response.code()==410){
                    Log.d("emergency006", "code is 403 or 410")
                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
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