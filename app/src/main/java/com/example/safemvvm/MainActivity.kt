package com.example.safemvvm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.MainViewModel
import com.example.safemvvm.viewmodels.MainViewModelFactory
import com.example.safemvvm.views.HomeActivity
import com.example.safemvvm.views.Login

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var startButton: Button
    private lateinit var progressBar: ProgressBar
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        startButton = findViewById(R.id.start)
        progressBar = findViewById(R.id.loading_progress_bar)
        requestPermissions(REQUIRED_PERMISSIONS, 123)
        if (isGranted())
            startButton.visibility = View.GONE
        else
            progressBar.visibility = View.GONE

        startButton.setOnClickListener {
            requestPermissions(REQUIRED_PERMISSIONS, 123)
            if (isGranted()) {
                startButton.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                checkToken()
            } else
                Toast.makeText(this, "Please grand all permissions to start using Safe St.", Toast.LENGTH_LONG).show()
        }
        if (isGranted())
            checkToken()
        observeResponses()
    }

    private fun checkToken() {


        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", "empty")
        val userId = localDB.getInt("userId", -1)



        if (userId != -1 && token != "empty")
            viewModel.checkToken("Bearer $token", userId)
        else
            Navigator(this).to(Login::class.java).andClearStack()
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.tokenCheckResponse,
            Boolean::class.java,
            {
                Navigator(this).to(HomeActivity::class.java).andClearStack()
            },
            {
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
    }

    private fun isGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }
}