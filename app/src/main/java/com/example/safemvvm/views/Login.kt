package com.example.safemvvm.views

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.LoginResponse
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.LoginViewModel
import com.example.safemvvm.viewmodels.LoginViewModelFactory
import com.google.android.material.textfield.TextInputEditText

class Login : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val buttonSignIn = findViewById<Button>(R.id.btn_signIn)
        val repository = Repository()
        val viewModelFactory = LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(LoginViewModel::class.java)
        buttonSignIn.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.loginEmail).text.toString()
            val password = findViewById<TextInputEditText>(R.id.loginPassword).text.toString()
            viewModel.login(LoginUser(password, email))
        }

        val buttonSignUp = findViewById<Button>(R.id.btn_voice_next)
        buttonSignUp.setOnClickListener {
            Navigator(this).to(SignUp::class.java).andClearTop()
        }
        observeResponses()
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.loginResponse,
            LoginResponse::class.java,
            {
                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                LocalDatabaseManager(this).token(it.token).id(it.id).saved(it.savedVoice)
                Navigator(this).to(HomeActivity::class.java).andClearStack()
            },
            {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
    }
}