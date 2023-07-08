package com.example.safemvvm.views

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.RegistrationViewModel
import com.example.safemvvm.viewmodels.RegistrationViewModelFactory
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

        buttonSignUp.setOnClickListener {
            val firstname = findViewById<TextInputEditText>(R.id.et_firstname).text.toString()
            val lastname = findViewById<TextInputEditText>(R.id.et_lastname).text.toString()
            val email = findViewById<TextInputEditText>(R.id.et_email).text.toString()
            val phoneNumber = findViewById<TextInputEditText>(R.id.et_phoneNumber).text.toString()
            val password = findViewById<TextInputEditText>(R.id.et_password).text.toString()
            val repeatedPassword =
                findViewById<TextInputEditText>(R.id.et_repeatPass).text.toString()
            val user = User(firstname, lastname, password, repeatedPassword, phoneNumber, email)
            viewModel.register(user)
        }

        val buttonSignIn = findViewById<Button>(R.id.btn_signIn)
        buttonSignIn.setOnClickListener {
            Navigator(this).to(Login::class.java).andClearTop()
        }
        observeResponses()
    }


    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.registerResponse,
            Boolean::class.java,
            {
                Toast.makeText(this, "Please check your email for account activation.", Toast.LENGTH_SHORT).show()
                Navigator(this).to(Login::class.java).andClearTop()
            },
            {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
    }
}