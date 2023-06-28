package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.safemvvm.R
import com.example.safemvvm.models.User
import com.example.safemvvm.views.voicesample.VoiceParagraphs
import com.google.android.material.textfield.TextInputEditText

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val buttonSignUp = findViewById<Button>(R.id.btn_voice_next)

        buttonSignUp.setOnClickListener {


            val firstname = findViewById<TextInputEditText>(R.id.et_firstname).text.toString()
            val lastname = findViewById<TextInputEditText>(R.id.et_lastname).text.toString()
            val email = findViewById<TextInputEditText>(R.id.et_email).text.toString()
            val phoneNumber = findViewById<TextInputEditText>(R.id.et_phoneNumber).text.toString()
            val password = findViewById<TextInputEditText>(R.id.et_password).text.toString()
            val repeatedPassword = findViewById<TextInputEditText>(R.id.et_repeatPass).text.toString()
            val user = User(firstname,lastname,password,repeatedPassword,phoneNumber,email)
            Intent(this, VoiceParagraphs::class.java).also {
                it.putExtra("userInfo", user)
                startActivity(it)
            }
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