package com.example.safemvvm.views

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.R.array
import com.example.safemvvm.R.id
import com.example.safemvvm.R.layout
import com.example.safemvvm.models.Report
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.AddReportViewModel
import com.example.safemvvm.viewmodels.AddReportViewModelFactory
import com.google.android.gms.maps.model.LatLng

class AddReportActivity : AppCompatActivity() {
    private lateinit var viewModel: AddReportViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_add_report)
        val emergencies = resources.getStringArray(array.Emergencies)
        val arrayAdapter = ArrayAdapter(this, layout.dropdown_item, emergencies)
        val autocompleteTV = findViewById<AutoCompleteTextView>(id.emergencyType)
        autocompleteTV.setAdapter(arrayAdapter)
        val buttonSubmitReport = findViewById<Button>(R.id.btn_submitReport)
        val repository = Repository()
        val viewModelFactory = AddReportViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(AddReportViewModel::class.java)
        val location = intent.getParcelableExtra<LatLng>("location")
        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val userId = localDB.getInt("userId", -1)
        buttonSubmitReport.setOnClickListener {
            viewModel.addReport("Bearer $token", Report(userId, findViewById<EditText>(R.id.reportDetails).text.toString(), autocompleteTV.text.toString(), location?.longitude.toString(), location?.latitude.toString()))
        }
        observeResponses()
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.addReportResponse,
            Any::class.java,
            {
                Toast.makeText(this, "Report Submitted", Toast.LENGTH_SHORT).show()
                Navigator(this).to(HomeActivity::class.java).andClearStack()
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
    }
}
