package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.RegistrationViewModel
import com.example.safemvvm.viewmodels.RegistrationViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class SignUp : AppCompatActivity() {
    private lateinit var viewModel: RegistrationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val buttonSignUp = findViewById<Button>(R.id.btn_signUp)

        val repository = Repository()
        val viewModelFactory = RegistrationViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(RegistrationViewModel::class.java)
        //TODO remove tags in logs with names at end of the project
        viewModel.registerResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Created Successfully"){
                    Toast.makeText(this, "account created successfully", Toast.LENGTH_SHORT).show()
                    Log.d("Arwa success reg", responseMessage)
                    Intent(this,Login::class.java).also { startActivity(it) }
                }else {
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    Log.d(
                        "Arwa success reach but error in fields",
                        responseMessage.toString()
                    )

                }
            } else {
                Toast.makeText(
                    this,
                    "something went wrong please try again later",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("Arwa not success", response.errorBody().toString())
                Log.d("Arwa not success", "${response.code()}")
            }
        }

        buttonSignUp.setOnClickListener {


            val firstname = findViewById<TextInputEditText>(R.id.et_firstname).text.toString()
            val lastname = findViewById<TextInputEditText>(R.id.et_lastname).text.toString()
            val email = findViewById<TextInputEditText>(R.id.et_email).text.toString()
            val phoneNumber = findViewById<TextInputEditText>(R.id.et_phoneNumber).text.toString()
            val password = findViewById<TextInputEditText>(R.id.et_password).text.toString()
            val repeatedPassword = findViewById<TextInputEditText>(R.id.et_repeatPass).text.toString()

//            val pref = getSharedPreferences("localDB", MODE_PRIVATE)
//            val token = pref.getString("token", "Not retrieved")
//            if (token != null) {
//                Log.d("Arwa token", token)
//            }

            viewModel.register(User(firstname,lastname,password,repeatedPassword,phoneNumber,email))


        }

        val buttonSignIn = findViewById<Button>(R.id.btn_signIn)
        buttonSignIn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}