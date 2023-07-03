package com.example.safemvvm.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EmergencyFired
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.ExtendTripBody
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.CheckArrivalViewModel
import com.example.safemvvm.viewmodels.CheckArrivalViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class CheckArrival : AppCompatActivity() {

    private lateinit var alertTimeTextView: TextView
    private lateinit var yesButton: MaterialButton
    private lateinit var noButton: MaterialButton
    private lateinit var emergencyType: EmergenciesEnum
    private lateinit var emergencyFired: EmergencyFired
    private lateinit var countdownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 10000 // 10 seconds countdown timer
    private val countDownInterval: Long = 1000
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: CheckArrivalViewModel
    private val MINUTES_TO_ADD = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_arrival)

        // Play a notification sound for 5 seconds when the activity is created
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, notificationSound)
        ringtone.play()

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val customerId = localDB.getInt("userId", -1)
        val tripId = localDB.getInt("tripId", -1)

        val durationInMillis = 10000L // 10 seconds
        val intervalInMillis = 500L // 0.5 seconds
        val timer = object : CountDownTimer(durationInMillis, intervalInMillis) {
            override fun onTick(millisUntilFinished: Long) {
                ringtone.play()
            }

            override fun onFinish() {
                ringtone.stop()
            }
        }
        timer.start()

        // Get references to the UI elements
        alertTimeTextView = findViewById(R.id.alertTime)
        yesButton = findViewById(R.id.yes)
        noButton = findViewById(R.id.no)

        // Create a new countdown timer
        countdownTimer = object : CountDownTimer(timeLeftInMillis, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountdownText()
            }

            override fun onFinish() {
                // Get the user's current location and system current time if the timer finishes
                emergencyType = EmergenciesEnum.UserDidntArrive
                getCurrentLocation()
                viewModel.endTrip("Bearer $token" , EndTripBody(tripId, customerId))
            }
        }

        // Start the countdown timer
        countdownTimer.start()

        // Get the FusedLocationProviderClient instance
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val repository = Repository()
        val viewModelFactory = CheckArrivalViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(CheckArrivalViewModel::class.java)


        viewModel.endTripResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                Log.d("checkArrival", "checkArrival: ${response.body()}  success" )
                countdownTimer.cancel()
                localDB.edit().apply {
                    putInt("tripId",tripId)
                    apply()
                }
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else if(response.message().toString().contains("Trip not Found") ){
                Log.d("checkArrival", "checkArrival:Trip not Found" )
                Toast.makeText(this, "Trip not found", Toast.LENGTH_LONG).show()
                localDB.edit().apply {
                    putInt("tripId",tripId)
                    apply()
                }
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else {
                Log.d("endTrip", "endTrip: ${response.errorBody()} testing" )
                //Long Toast
                Toast.makeText(this, "Error: ${response.errorBody()}", Toast.LENGTH_LONG).show()
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
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
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

        viewModel.extendTripResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message
                if (responseMessage == "Executed Successfully") {
                    Log.d("extendTrip001", "extendTrip: ${response.body()}  success" )
                    val data = Gson().fromJson(response.body()?.data.toString(), TripResponse::class.java)
                    val intent = Intent(this, WhileInTrip::class.java)
                    intent.putExtra("time", data.remainingTime.toInt())
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else if (responseMessage == "Authentication Error") {
                    Log.d("extendTrip002", "extendTrip: ${response.body()}" )
                    Toast.makeText(this, "Session Expired", Toast.LENGTH_LONG).show()
                    localDB.edit().apply {
                        putString("token", "empty")
                        apply()
                    }
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    Log.d("extendTrip003", "extendTrip: ${response.body()}" )
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }

            } else {
                Log.d("extendTrip004", "extendTrip: ${response.errorBody()}" )
                Log.d("extendTrip005", "extendTrip: ${response.body()}" )
            }
        }


        // Set a click listener for the "Yes" button
        //countdownTimer.cancel()
        yesButton.setOnClickListener {
            countdownTimer.cancel()
            viewModel.endTrip("Bearer $token" , EndTripBody(tripId, customerId))
        }

        noButton.setOnClickListener {
            countdownTimer.cancel()
            viewModel.extendTrip("Bearer $token", ExtendTripBody(tripId, customerId, MINUTES_TO_ADD))
        }

    }




    private fun updateCountdownText() {
        val seconds = (timeLeftInMillis / 1000).toInt()
        val countdownText = "Time left: $seconds seconds"
        alertTimeTextView.text = countdownText
    }

    private fun getCurrentLocation() {
        // Check if the app has the location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        // Get the user's current location using the FusedLocationProviderClient
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Format the current time
                    val time = System.currentTimeMillis()
                    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val formattedTime = dateFormat.format(time)
                    val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
                    val token = localDB.getString("token", null)
                    val customerId = localDB.getInt("userId", -1)

                    // Create a new EmergencyFired object with the location, time, and emergency type
                    emergencyFired = EmergencyFired(location, formattedTime, emergencyType)

                    // Display a toast message with the location and time information
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val message = "Latitude: $latitude, Longitude: $longitude, $formattedTime, $emergencyType"
                    viewModel.fireEmergency("Bearer $token", EmergencyBody(customerId, longitude, latitude, emergencyType.toString()))
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                    // Log the location and time information
                    Log.d("TAG", message)
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
}