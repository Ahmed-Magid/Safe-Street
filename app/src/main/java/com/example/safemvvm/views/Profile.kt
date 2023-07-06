package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.LoginResponse
import com.example.safemvvm.models.PersonalInfoResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.HomeViewModel
import com.example.safemvvm.viewmodels.HomeViewModelFactory
import com.example.safemvvm.viewmodels.LoginViewModel
import com.example.safemvvm.viewmodels.ProfileViewModel
import com.example.safemvvm.viewmodels.ProfileViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

class Profile : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val repository = Repository()
        val viewModelFactory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(ProfileViewModel::class.java)

        val name = findViewById<TextView>(R.id.profilename)
        val email = findViewById<TextView>(R.id.profileEmail)
        val phoneNumber = findViewById<TextView>(R.id.profilePhone)

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)

        viewModel.getPersonalInfo("Bearer $token",userId)

        viewModel.profileResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Executed Successfully"){
                    Log.d("Profile002","got profile success")
                    val data = Gson().fromJson(response.body()?.data.toString(), PersonalInfoResponse::class.java)
                    name.text = data.firstname +" "+ data.lastname
                    email.text = data.email
                    phoneNumber.text = data.phoneNumber

                  //TODO
                }else {
                    Log.d("Profile003","userId isn’t the same for the logged in account")
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            } else {
                Log.d("Profile008", response.errorBody().toString())
                if(response.code()==403 || response.code()==410){
                    Log.d("Profile006", "code is 403 or 410")
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
                    Log.d("Profile004", response.errorBody().toString())
                    Log.d("profile005", "${response.code()}")
                }

            }
        }

    }
}