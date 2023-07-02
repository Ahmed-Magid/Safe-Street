package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.RegistrationViewModel
import com.example.safemvvm.viewmodels.RegistrationViewModelFactory
import com.example.safemvvm.views.voicesample.VoiceParagraphs
import com.google.android.material.textfield.TextInputEditText

class SignUp : AppCompatActivity() {
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val buttonSignUp = findViewById<Button>(R.id.btn_voice_next)
        val repository = Repository()
        val viewModelFactory = RegistrationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(RegistrationViewModel::class.java)
        //TODO remove tags in logs with names at end of the project
        viewModel.registerResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if (responseMessage == "Created Successfully") {
                    Toast.makeText(
                        this,
                        "Please check your email for account activation.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("Arwa success reg", responseMessage)
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                } else {
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
            val repeatedPassword =
                findViewById<TextInputEditText>(R.id.et_repeatPass).text.toString()
            val user = User(firstname, lastname, password, repeatedPassword, phoneNumber, email)
            /*Intent(this, VoiceParagraphs::class.java).also {
                it.putExtra("userInfo", user)
                startActivity(it)
            }*/
            viewModel.register(user)
        }

        val buttonSignIn = findViewById<Button>(R.id.btn_signIn)
        buttonSignIn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
    //TODO: add toast to tell user to confirm email before login
    //TODO: ask for voice sample after registration
}