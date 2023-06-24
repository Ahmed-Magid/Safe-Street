package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.example.safemvvm.R
import com.example.safemvvm.R.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_home)
        val buttonSignUp = findViewById<Button>(id.btn_createTrip)
        buttonSignUp.setOnClickListener {
            val intent = Intent(this, AddTripActivity::class.java)
            startActivity(intent)
        }
        val buttonViewTrustedContact = findViewById<Button>(id.btn_viewTrusted)
        buttonViewTrustedContact.setOnClickListener {
            val intent = Intent(this, ViewTrustedContacts::class.java)
            startActivity(intent)
        }
        val buttonAddReport = findViewById<Button>(id.btn_reportLocation)
        buttonAddReport.setOnClickListener {
            val intent = Intent(this, AddReportActivity::class.java)
            startActivity(intent)
        }
        val imageViewLogo = findViewById<ImageView>(id.logo)
        imageViewLogo.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
        val buttonCreatetrip = findViewById<Button>(R.id.btn_createTrip)
        buttonCreatetrip.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}