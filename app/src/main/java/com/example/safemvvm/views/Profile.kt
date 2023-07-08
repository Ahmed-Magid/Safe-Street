package com.example.safemvvm.views

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.PersonalInfoResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.ProfileViewModel
import com.example.safemvvm.viewmodels.ProfileViewModelFactory

class Profile : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var phoneNumber: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val repository = Repository()
        val viewModelFactory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(ProfileViewModel::class.java)

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)

        viewModel.getPersonalInfo("Bearer $token",userId)
        name = findViewById(R.id.profilename)
        email = findViewById(R.id.profileEmail)
        phoneNumber = findViewById(R.id.profilePhone)
        observeResponses()
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.profileResponse,
            PersonalInfoResponse::class.java,
            {
                name.text = StringBuilder().append(it.firstname).append(" ").append(it.lastname).toString()
                email.text = it.email
                phoneNumber.text = it.phoneNumber
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
    }
}