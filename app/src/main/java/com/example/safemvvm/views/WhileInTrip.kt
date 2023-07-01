package com.example.safemvvm.views

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.WhileInTripViewModel
import com.example.safemvvm.viewmodels.WhileInTripViewModelFactory
import com.google.android.material.button.MaterialButton

class WhileInTrip : AppCompatActivity() {
    // TODO : tell user when they close the app and open it that they're currently in a trip
    // TODO: timer text view isn't working properly
    private lateinit var viewModel: WhileInTripViewModel
    private lateinit var countdownTimer: CountDownTimer
    private lateinit var timerTextView: TextView
    private lateinit var cancelButton: MaterialButton
    private lateinit var extendTimerButton: MaterialButton
    private lateinit var iArrivedButton: MaterialButton
    private lateinit var fireEmergencyButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_while_in_trip)
        // Initialize the views
        timerTextView = findViewById(R.id.timer)
        cancelButton = findViewById(R.id.btn_cancel)
        extendTimerButton = findViewById(R.id.ExtendTimer)
        iArrivedButton = findViewById(R.id.IArrived)
        fireEmergencyButton = findViewById(R.id.FireEmergency)

        //take timeInSeconds from CreateTripActivity
        val timeInSeconds = intent.getIntExtra("timeInSeconds", 0)

        countdownTimer = object : CountDownTimer(timeInSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the timerTextView with the remaining time
                val minutesLeft = (millisUntilFinished / 1000) / 60
                val secondsLeft = (millisUntilFinished / 1000) % 60
                timerTextView.text = buildString {
                    append("Time left: ")
                    append(minutesLeft)
                    append(" min ")
                    append(secondsLeft)
                    append(" sec")
                }
            }

            override fun onFinish() {
                // Do something when the countdown timer finishes
                timerTextView.text = getString(R.string.timer_finished)
            }
        }

        // Start the countdown timer
        countdownTimer.start()

        // Set click listener for the FireEmergency button
        fireEmergencyButton.setOnClickListener {
            val intent = Intent(this, Emergencies::class.java)
            startActivity(intent)
        }


        val repository = Repository()
        val viewModelFactory = WhileInTripViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(WhileInTripViewModel::class.java)
        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val customerId = localDB.getInt("userId", -1)
        val id = localDB.getInt("tripId", -1)

        viewModel.endTripResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                Log.d("endTrip", "endTrip: ${response.body()}  success" )
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                Log.d("endTrip", "endTrip: ${response.errorBody()} testing" )
                //Long Toast
                Toast.makeText(this, "Error: ${response.errorBody()}", Toast.LENGTH_LONG).show()
            }
        }


        // Set click listener for the IArrived button
        iArrivedButton.setOnClickListener {
            countdownTimer.cancel()
            timerTextView.text = getString(R.string.timer_stopped)
            cancelButton.isEnabled = false
            extendTimerButton.isEnabled = false
            iArrivedButton.isEnabled = false
            viewModel.endTrip("Bearer $token" , EndTripBody(id, customerId))
        }

        // Set click listener for the ExtendTimer button
        extendTimerButton.setOnClickListener {
            countdownTimer.cancel()
            // Extract the current time left from the timerTextView
            val currentTimeLeft = timerTextView.text.toString().split(" ")[2].toInt()

            // Add 5 minutes (300 seconds) to the current time left
            val newTimeLeft = currentTimeLeft + 300

            // Start a new countdown timer with the updated time left
            countdownTimer = object : CountDownTimer(newTimeLeft * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutesLeft = (millisUntilFinished / 1000) / 60
                    val secondsLeft = (millisUntilFinished / 1000) % 60
                    timerTextView.text = buildString {
                        append("Time left: ")
                        append(minutesLeft)
                        append(" min ")
                        append(secondsLeft)
                        append(" sec")
                    }
                }

                override fun onFinish() {
                    timerTextView.text = getString(R.string.timer_finished)
                    val intent = Intent(this@WhileInTrip, CheckArrival::class.java)
                    startActivity(intent)
                }
            }
            countdownTimer.start()
            cancelButton.isEnabled = true
            extendTimerButton.isEnabled = true
            iArrivedButton.isEnabled = true
        }

        // Set click listener for the cancel button
        cancelButton.setOnClickListener {
            countdownTimer.cancel()
            timerTextView.text = getString(R.string.timer_cancelled)
            cancelButton.isEnabled = false
            extendTimerButton.isEnabled = false
            iArrivedButton.isEnabled = false
        }

    }
}