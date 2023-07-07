package com.example.safemvvm.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EmergencyFired
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.services.SpeechToTextService
import com.example.safemvvm.utils.AddressProvider
import com.example.safemvvm.viewmodels.EmergenciesViewModel
import com.example.safemvvm.viewmodels.EmergenciesViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Locale


class CheckEmergency : AppCompatActivity() {
    private lateinit var viewModel: EmergenciesViewModel
    private lateinit var alertTimeTextView: TextView
    private lateinit var countdownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 5000 // 5 seconds countdown timer
    private val countDownInterval: Long = 1000
    private lateinit var emergencyType : EmergenciesEnum
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    lateinit var emergencyFired: EmergencyFired
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_emergency)
        // Get the FusedLocationProviderClient instance
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val repository = Repository()
        val viewModelFactory = EmergenciesViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(EmergenciesViewModel::class.java)
        // Play a notification sound for 5 seconds when the activity is created
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, notificationSound)
        ringtone.play()

        val voiceServices = Intent(this, SpeechToTextService::class.java)
        stopService(voiceServices)
        println("Service Stopped")

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

        alertTimeTextView = findViewById(R.id.alertTime)

        // Create a new countdown timer
        countdownTimer = object : CountDownTimer(timeLeftInMillis, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountdownText()
            }

            override fun onFinish() {
                // Get the user's current location and system current time if the timer finishes
                emergencyType = intent.getSerializableExtra("emergencyType") as EmergenciesEnum
                getCurrentLocation()
                val intent = Intent(this@CheckEmergency, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
        countdownTimer.start()


        val yes = findViewById<Button>(R.id.yes)

        yes.setOnClickListener {
            emergencyType = intent.getSerializableExtra("emergencyType") as EmergenciesEnum
            countdownTimer.cancel()
            getCurrentLocation()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val no = findViewById<Button>(R.id.no)
        no.setOnClickListener {
            countdownTimer.cancel()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }


        //TODO: fix this

        /*viewModel.fireEmergencyResponse.observe(this) { response ->
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
        }*/
    }

    private fun updateCountdownText() {
        val seconds = (timeLeftInMillis / 1000).toInt()
        val countdownText = "Time left: $seconds seconds"
        alertTimeTextView.text = countdownText
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
                    //TODO: go to check emergency first then Send emergency to server
                    viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, longitude, latitude, emergencyFired.type.toString(), AddressProvider(this).getAddress(
                        LatLng(latitude, longitude), "ar"
                    ))
                    )
                     Toast.makeText(this, "Latitude: $latitude, Longitude: $longitude, $formattedTime , $emergencyType ",Toast.LENGTH_LONG).show()
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

}

