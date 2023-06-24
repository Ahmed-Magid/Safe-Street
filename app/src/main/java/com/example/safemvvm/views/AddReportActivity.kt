package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.example.safemvvm.R
import com.example.safemvvm.R.*

class AddReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_add_report)
        val emergencies = resources.getStringArray(array.Emergencies)
        val arrayAdapter = ArrayAdapter(this, layout.dropdown_item, emergencies)
        val autocompleteTV = findViewById<AutoCompleteTextView>(id.emergencyType)
        autocompleteTV.setAdapter(arrayAdapter)
        val buttonSubmitReport = findViewById<Button>(R.id.btn_submitReport)
        buttonSubmitReport.setOnClickListener {
            Toast.makeText(this, "Report Submitted", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}
