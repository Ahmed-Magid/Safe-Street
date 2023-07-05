package com.example.safemvvm.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R

import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EmergencyFired
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.HomeViewModel
import com.example.safemvvm.viewmodels.HomeViewModelFactory
import com.example.safemvvm.views.voicesample.VoiceParagraphs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel


    private lateinit var menuButton: ImageButton
    private lateinit var menuContainer: LinearLayout
    private lateinit var profileOption: TextView
    private lateinit var trustedContactsOption: TextView
    private lateinit var reportsOption: TextView
    private lateinit var logoutOption: TextView
    private lateinit var createTripOption: TextView

    private lateinit var carFaultImage: ImageView
    private lateinit var harasmentImage: ImageView
    private lateinit var fireImage: ImageView
    private lateinit var robberyImage: ImageView
    private lateinit var kidnappingImage: ImageView
    private lateinit var murderImage: ImageView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    lateinit var emergencyFired: EmergencyFired
    lateinit var emergencyType : EmergenciesEnum

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Find views
        menuButton = findViewById(R.id.menu_button)
        menuContainer = findViewById(R.id.menu_container)
        profileOption = findViewById(R.id.profile_option)
        trustedContactsOption = findViewById(R.id.trusted_contacts_option)
        reportsOption = findViewById(R.id.reports_option)
        logoutOption = findViewById(R.id.logout_option)
        createTripOption = findViewById(R.id.create_trip_option)

        carFaultImage = findViewById(R.id.iv_carFault)
        harasmentImage = findViewById(R.id.iv_harassment)
        fireImage = findViewById(R.id.iv_fire)
        robberyImage = findViewById(R.id.iv_robbery)
        kidnappingImage = findViewById(R.id.iv_kidnapping)
        murderImage = findViewById(R.id.iv_murder)


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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        viewModel.fireEmergencyResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Created Successfully") {
                    Log.d(
                        "emergency001",
                        responseMessage.toString()
                    )
                    Toast.makeText(this, "Emergency fired successfully", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    Log.d(
                        "emergency002",
                        responseMessage.toString()
                    )
                }
            } else {

                Log.d("emergency004", response.errorBody().toString())
                Log.d("emergency005", "${response.code()}")
                if(response.code()==403 || response.code()==410){
                    Log.d("emergency006", "code is 403 or 410")
                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this,
                        "something went wrong please try again later",
                        Toast.LENGTH_LONG
                    ).show()
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

        kidnappingImage.setOnClickListener {
            emergencyType = EmergenciesEnum.KIDNAPPING
            getCurrentLocation()
        }

        harasmentImage.setOnClickListener {
            emergencyType = EmergenciesEnum.HARASSMENT
            getCurrentLocation()
        }

        fireImage.setOnClickListener {
            emergencyType = EmergenciesEnum.FIRE
            getCurrentLocation()
        }

        carFaultImage.setOnClickListener {
            emergencyType = EmergenciesEnum.CAR_FAULT
            getCurrentLocation()
        }


        robberyImage.setOnClickListener {
            emergencyType = EmergenciesEnum.ROBBERY
            getCurrentLocation()
        }


        murderImage.setOnClickListener {
            emergencyType = EmergenciesEnum.MURDER
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
                    val token = localDB.getString("token","empty")
                    val userId = localDB.getInt("userId",-1)
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val time = System.currentTimeMillis()
                    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val formattedTime = dateFormat.format(time)
                    emergencyFired = EmergencyFired(location, formattedTime, emergencyType)
                    println(emergencyFired.type.toString())
                    viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, longitude, latitude, emergencyFired.type.toString()))
                   // Toast.makeText(this, "Latitude: $latitude, Longitude: $longitude, $formattedTime , $emergencyType ",Toast.LENGTH_LONG).show()
                    Log.d("TAG", "Latitude: $latitude, Longitude: $longitude, $formattedTime , $emergencyType ")

                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun toggleMenu() {
        if (menuContainer.visibility == View.VISIBLE) {

            menuContainer.visibility = View.GONE
        } else {
            menuContainer.visibility = View.VISIBLE
        }
    }
}