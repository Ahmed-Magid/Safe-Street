package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.safemvvm.R

class SignUpTrusted : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_trusted)

        val buttonContinue = findViewById<Button>(R.id.btn_continue)
        buttonContinue.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val buttonSkip = findViewById<Button>(R.id.btn_skip)
        buttonSkip.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}