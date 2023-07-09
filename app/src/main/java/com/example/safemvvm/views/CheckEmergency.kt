package com.example.safemvvm.views

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.services.SpeechToTextService
import com.example.safemvvm.utils.AddressProvider
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.LocationProvider
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.EmergenciesViewModel
import com.example.safemvvm.viewmodels.EmergenciesViewModelFactory


class CheckEmergency : AppCompatActivity() {
    private lateinit var viewModel: EmergenciesViewModel
    private lateinit var alertTimeTextView: TextView
    private lateinit var countdownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 10000 // 10 seconds countdown timer
    private val countDownInterval: Long = 1000
    private lateinit var emergencyType : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_emergency)
        // Get the FusedLocationProviderClient instance
        val repository = Repository()
        val viewModelFactory = EmergenciesViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(EmergenciesViewModel::class.java)
        // Play a notification sound for 10 seconds when the activity is created
        val defaultNotificationSound: Uri = Uri.parse("android.resource://com.example.safemvvm/raw/alarm")

        val ringtone = RingtoneManager.getRingtone(applicationContext, defaultNotificationSound)
        ringtone.play()

        val voiceServices = Intent(this, SpeechToTextService::class.java)
        stopService(voiceServices)
        println("Service Stopped")

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val userId = localDB.getInt("userId", -1)

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
                emergencyType = intent.getStringExtra("emergencyType").toString()
                LocationProvider(this@CheckEmergency).getCurrentLocation().thenApply {
                    viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, it.longitude, it.latitude, emergencyType,
                        AddressProvider(this@CheckEmergency).getAddress(
                            it, "ar")))
                }
            }
        }
        countdownTimer.start()


        val yes = findViewById<Button>(R.id.yes)

        yes.setOnClickListener {
            emergencyType = intent.getStringExtra("emergencyType").toString()
            countdownTimer.cancel()
            LocationProvider(this).getCurrentLocation().thenApply {
                viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, it.longitude, it.latitude, emergencyType,
                    AddressProvider(this).getAddress(
                        it, "ar")))
            }
            ringtone.stop()

        }

        val no = findViewById<Button>(R.id.no)
        no.setOnClickListener {
            countdownTimer.cancel()
            Navigator(this).to(HomeActivity::class.java).andClearStack()
            ringtone.stop()
        }
        observeResponses()
    }

    private fun updateCountdownText() {
        val seconds = (timeLeftInMillis / 1000).toInt()
        val countdownText = "Time left: $seconds seconds"
        alertTimeTextView.text = countdownText
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.fireEmergencyResponse,
            Any::class.java,
            {
                Toast.makeText(this, "Emergency Fired Successfully.", Toast.LENGTH_SHORT).show()
                Navigator(this).to(HomeActivity::class.java).andClearStack()
            },
            {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
    }

}

