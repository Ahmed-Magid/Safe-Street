package com.example.safemvvm.views

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.ExtendTripBody
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.AddressProvider
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.LocationProvider
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.CheckArrivalViewModel
import com.example.safemvvm.viewmodels.CheckArrivalViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton

class CheckArrival : AppCompatActivity() {

    private lateinit var alertTimeTextView: TextView
    private lateinit var yesButton: MaterialButton
    private lateinit var noButton: MaterialButton
    private lateinit var emergencyType: String
    private lateinit var countdownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 10000 // 10 seconds countdown timer
    private val countDownInterval: Long = 1000
    private lateinit var viewModel: CheckArrivalViewModel
    private val MINUTES_TO_ADD = 5
    val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    private lateinit var ringtone: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_arrival)

        ringtone = RingtoneManager.getRingtone(applicationContext, notificationSound)
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
                emergencyType = EmergenciesEnum.UserDidntArrive.toString()
                LocationProvider(this@CheckArrival).getCurrentLocation().thenApply {
                    viewModel.fireEmergency("Bearer $token", EmergencyBody(customerId, it.longitude, it.latitude, emergencyType,
                        AddressProvider(this@CheckArrival).getAddress(
                            it, "ar")))
                    viewModel.endTrip("Bearer $token" , EndTripBody(tripId, customerId))
                }

            }
        }

        // Start the countdown timer
        countdownTimer.start()


        val repository = Repository()
        val viewModelFactory = CheckArrivalViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(CheckArrivalViewModel::class.java)

        yesButton.setOnClickListener {
            countdownTimer.cancel()
            viewModel.endTrip("Bearer $token" , EndTripBody(tripId, customerId))
            ringtone.stop()
        }

        noButton.setOnClickListener {
            countdownTimer.cancel()
            viewModel.extendTrip("Bearer $token", ExtendTripBody(tripId, customerId, MINUTES_TO_ADD))
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
                ringtone.stop()
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
        ResponseHandler(this).observeResponse(
            viewModel.endTripResponse,
            Boolean::class.java,
            {
                LocalDatabaseManager(this).tripId(-1)
                Navigator(this).to(HomeActivity::class.java).andClearStack()
                ringtone.stop()
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
        ResponseHandler(this).observeResponse(
            viewModel.extendTripResponse,
            TripResponse::class.java,
            {
                LocalDatabaseManager(this).tripId(-1)
                Navigator(this).to(WhileInTrip::class.java).andPutExtraInt("time", it.remainingTime.toInt()).andClearStack()
                ringtone.stop()
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
    }
}