package com.example.safemvvm.views

import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.EmergencyFired
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.Locale

class CheckArrival : AppCompatActivity() {

    private lateinit var alertTimeTextView: TextView
    private lateinit var yesButton: MaterialButton
    private lateinit var emergencyType: EmergenciesEnum
    private lateinit var emergencyFired: EmergencyFired
    private lateinit var countdownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 10000 // 10 seconds countdown timer
    private val countDownInterval: Long = 1000
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_arrival)

        // Play a notification sound for 5 seconds when the activity is created
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, notificationSound)
        ringtone.play()

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
            }
        }

        // Start the countdown timer
        countdownTimer.start()

        // Set a click listener for the "Yes" button
        yesButton.setOnClickListener {
            // Stop the countdown timer if the user clicks "Yes"
            countdownTimer.cancel()
        }

        // Get the FusedLocationProviderClient instance
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

                    // Create a new EmergencyFired object with the location, time, and emergency type
                    emergencyFired = EmergencyFired(location, formattedTime, emergencyType)

                    // Display a toast message with the location and time information
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val message = "Latitude: $latitude, Longitude: $longitude, $formattedTime, $emergencyType"
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