package com.example.safemvvm.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.HomeViewModel
import com.example.safemvvm.viewmodels.HomeViewModelFactory
import com.example.safemvvm.views.voicesample.VoiceParagraphs
import com.google.gson.Gson

class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var menuButton: ImageButton
    private lateinit var menuContainer: LinearLayout
    private lateinit var profileOption: TextView
    private lateinit var trustedContactsOption: TextView
    private lateinit var reportsOption: TextView
    private lateinit var logoutOption: TextView
    private lateinit var createTripOption: TextView
    lateinit var emergencyType : EmergenciesEnum
    private lateinit var carFault : ImageView
    private lateinit var fire : ImageView
    private lateinit var harassment : ImageView
    private lateinit var kidnapping : ImageView
    private lateinit var robbery : ImageView
    private lateinit var murder :ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        carFault = findViewById(R.id.iv_carFault)
        fire = findViewById(R.id.iv_fire)
        harassment = findViewById(R.id.iv_harassment)
        kidnapping = findViewById(R.id.iv_kidnapping)
        robbery = findViewById(R.id.iv_robbery)
        murder = findViewById(R.id.iv_murder)

        // Find views
        menuButton = findViewById(R.id.menu_button)
        menuContainer = findViewById(R.id.menu_container)
        profileOption = findViewById(R.id.profile_option)
        trustedContactsOption = findViewById(R.id.trusted_contacts_option)
        reportsOption = findViewById(R.id.reports_option)
        logoutOption = findViewById(R.id.logout_option)
        createTripOption = findViewById(R.id.create_trip_option)


        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)
        val savedVoice = localDB.getBoolean("saved",false)
        if(!savedVoice){
            val intent = Intent(this, VoiceParagraphs::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val repository = Repository()
        val viewModelFactory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(HomeViewModel::class.java)

        viewModel.getNumOfTrusted("Bearer $token",userId)

        viewModel.checkIngoingTrip("Bearer $token",userId)
        viewModel.numOfContactsResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Executed Successfully") {
                    val data = Gson().fromJson(response.body()?.data.toString(), Int::class.java)
                    Log.d("Arwa success to num of contacts","$data")
                    if(data == 0) {
                        Toast.makeText(this, "please add trusted contacts", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, ViewTrustedContacts::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }else {
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    Log.d(
                        "Arwa num of contacts auth error",
                        responseMessage.toString()
                    )
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            } else {
                Toast.makeText(
                    this,
                    "something went wrong please try again later",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("Arwa not success noc", response.errorBody().toString())
                Log.d("Arwa not success noc", "${response.code()}")
                if(response.code()==403 || response.code()==410){
                    Log.d("Profile006", "code is 403 or 410")
                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }


        viewModel.logoutResponse.observe(this) { response ->
            if (response.isSuccessful || response.code()==403 || response.code()==410) {
                Log.d("Home001","${response.code()}")
                localDB.edit().apply {
                    putString("token", "empty")
                    apply()
                }
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                Log.d("Home002","${response.code()}")
                Toast.makeText(
                    this,
                    "something went wrong please try again later",
                    Toast.LENGTH_LONG
                ).show()

            }
            logoutOption.isEnabled = true
        }

        viewModel.checkIngoingTripResponse.observe(this){ response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message
                if(responseMessage == "Executed Successfully"){
                    if(response.body()!!.data != null){
                        Log.d("MainActivity", "trip is ongoing")
                        val data = Gson().fromJson(response.body()?.data.toString(), TripResponse::class.java)
                        localDB.edit().apply {
                            putInt("tripId",data.id)
                            apply()
                        }
                        val timeInSeconds = (data.remainingTime).toInt()
                        Log.d("time", timeInSeconds.toString())
                        val intent = Intent(this, WhileInTrip::class.java)
                        intent.putExtra("time", timeInSeconds)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }else if(responseMessage == "Time ended Are you Ok?"){
                    val intent = Intent(this, CheckArrival::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }else{
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            } else{
                Log.d("MainActivity", "no success")
                if(response.code()==403 || response.code()==410){
                    Log.d("Profile006", "code is 403 or 410")
                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }

        logoutOption.setOnClickListener {

            if (token != null) {
                //mahmoud
                //loading icon for train model and logout
                //remove name from add trusted contact
                //add fire emergency button in home
                // change profile button image
                // Check arrival add No button to extend
                // disable buttons to stop multiple firing
                logoutOption.isEnabled = false
                viewModel.logout("Bearer $token", IdBody(userId))
                toggleMenu()
            }
        }

        menuButton.setOnClickListener {
            toggleMenu()
        }

        trustedContactsOption.setOnClickListener {
            val intent = Intent(this, ViewTrustedContacts::class.java)
            startActivity(intent)
            toggleMenu()
        }

        profileOption.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            toggleMenu()
        }

        createTripOption.setOnClickListener {
            val intent = Intent(this, CreateTripActivity::class.java)
            startActivity(intent)
            toggleMenu()
        }

        reportsOption.setOnClickListener {
            val intent = Intent(this, ReportLocationMap::class.java)
            startActivity(intent)
            toggleMenu()
        }

        carFault.setOnClickListener{
            emergencyType = EmergenciesEnum.CAR_FAULT
            val intent = Intent(this, CheckEmergency::class.java)
            intent.putExtra("emergencyType", emergencyType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        fire.setOnClickListener {
            emergencyType = EmergenciesEnum.FIRE
            val intent = Intent(this, CheckEmergency::class.java)
            intent.putExtra("emergencyType", emergencyType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        harassment.setOnClickListener{
            emergencyType = EmergenciesEnum.HARASSMENT
            val intent = Intent(this, CheckEmergency::class.java)
            intent.putExtra("emergencyType", emergencyType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        kidnapping.setOnClickListener {
            emergencyType = EmergenciesEnum.KIDNAPPING
            val intent = Intent(this, CheckEmergency::class.java)
            intent.putExtra("emergencyType", emergencyType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        robbery.setOnClickListener {
            emergencyType = EmergenciesEnum.ROBBERY
            val intent = Intent(this, CheckEmergency::class.java)
            intent.putExtra("emergencyType", emergencyType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        murder.setOnClickListener{
            emergencyType = EmergenciesEnum.MURDER
            val intent = Intent(this, CheckEmergency::class.java)
            intent.putExtra("emergencyType", emergencyType)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }


    private fun toggleMenu() {
        if (menuContainer.visibility == View.VISIBLE) {
            carFault.isClickable = true
            harassment.isClickable = true
            fire.isClickable = true
            robbery.isClickable = true
            kidnapping.isClickable = true
            murder.isClickable = true

            menuContainer.visibility = View.GONE
        } else {
            carFault.isClickable = false
            harassment.isClickable = false
            fire.isClickable = false
            robbery.isClickable = false
            kidnapping.isClickable = false
            murder.isClickable = false
            menuContainer.visibility = View.VISIBLE
        }
    }
}