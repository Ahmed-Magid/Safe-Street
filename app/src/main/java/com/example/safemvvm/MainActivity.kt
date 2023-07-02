package com.example.safemvvm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.MainViewModel
import com.example.safemvvm.viewmodels.MainViewModelFactory
import com.example.safemvvm.views.HomeActivity
import com.example.safemvvm.views.Login
import com.example.safemvvm.views.WhileInTrip
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(MainViewModel::class.java)

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)
        val buttonClick = findViewById<Button>(R.id.btn_start)

        viewModel.tokenCheckResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Executed Successfully"){
                    Log.d("002","correct token he is logged in")
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }else {
                    Log.d("003","userId isn’t the same for the logged in account")
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                }
            } else {
                Log.d("006", response.errorBody().toString())
                if(response.code()==403 || response.code()==410){
                    Log.d("Trusted006", "code is 403 or 410")
                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(
                        this,
                        "something went wrong please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("004", response.errorBody().toString())
                    Log.d("005", "${response.code()}")
                }

            }
        }

//        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)




        buttonClick.setOnClickListener {

            //val idBody = IdBody(localDB.getInt("userId",-1))

            if(userId == -1){
                Log.d("001","never logged in before")
                val intent = Intent(this, Login::class.java)
                startActivity(intent)

            }else{
                Log.d("007","id is not -1")
                Log.d("007",token.toString())
                Log.d("007", "$userId")
                //val idBody = IdBody(userId)
                if (token != null) {
                    viewModel.checkToken("Bearer $token",userId)
                }else{
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}