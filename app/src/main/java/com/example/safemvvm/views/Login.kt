package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.LoginResponse
import com.example.safemvvm.models.LoginUser
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.LoginViewModel
import com.example.safemvvm.viewmodels.LoginViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

class Login : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val buttonSignIn = findViewById<Button>(R.id.btn_signIn)
        val repository = Repository()
        val viewModelFactory = LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(LoginViewModel::class.java)

        viewModel.loginResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Executed Successfully") {
                    Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                    val data = Gson().fromJson(response.body()?.data.toString(), LoginResponse::class.java)
                    val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
                    Log.d(
                        "Arwa success to login",
                        "${data.id}"
                    )
                    localDB.edit().apply {
                        putString("token", data.token)
                        putInt("userId",data.id)
                        apply()
                    }
                    Intent(this,HomeActivity::class.java).also { startActivity(it) }
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



        buttonSignIn.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.loginEmail).text.toString()
            val password = findViewById<TextInputEditText>(R.id.loginPassword).text.toString()

            viewModel.login(LoginUser(password, email))


        }
        val buttonSignUp = findViewById<Button>(R.id.btn_voice_next)
        buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}