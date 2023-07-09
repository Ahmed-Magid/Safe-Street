package com.example.safemvvm.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.ExtendTripBody
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.services.SpeechToTextService
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.WhileInTripViewModel
import com.example.safemvvm.viewmodels.WhileInTripViewModelFactory
import com.google.android.material.button.MaterialButton

class WhileInTrip : AppCompatActivity() {
    private lateinit var viewModel: WhileInTripViewModel
    private lateinit var countdownTimer: CountDownTimer
    private lateinit var timerTextView: TextView
    private lateinit var cancelButton: MaterialButton
    private lateinit var extendTimerButton: MaterialButton
    private lateinit var iArrivedButton: MaterialButton
    private val MINUTES_TO_ADD = 5
    private lateinit var emergencyType : EmergenciesEnum


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_while_in_trip)

        checkAndRequestPermissions()

        val voiceService = Intent(this, SpeechToTextService::class.java)
        val repository = Repository()
        val viewModelFactory = WhileInTripViewModelFactory(repository)

        val carFault = findViewById<ImageView>(R.id.iv_carFault)
        val fire = findViewById<ImageView>(R.id.iv_fire)
        val harassment = findViewById<ImageView>(R.id.iv_harassment)
        val kidnapping = findViewById<ImageView>(R.id.iv_kidnapping)
        val robbery = findViewById<ImageView>(R.id.iv_robbery)
        val murder = findViewById<ImageView>(R.id.iv_murder)

        carFault.setOnClickListener{
            emergencyType = EmergenciesEnum.CAR_FAULT
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        fire.setOnClickListener {
            emergencyType = EmergenciesEnum.FIRE
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        harassment.setOnClickListener{
            emergencyType = EmergenciesEnum.HARASSMENT
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        kidnapping.setOnClickListener {
            emergencyType = EmergenciesEnum.KIDNAPPING
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        robbery.setOnClickListener {
            emergencyType = EmergenciesEnum.ROBBERY
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        murder.setOnClickListener{
            emergencyType = EmergenciesEnum.MURDER
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        viewModel = ViewModelProvider(this,viewModelFactory).get(WhileInTripViewModel::class.java)
        startService(voiceService)

        timerTextView = findViewById(R.id.timer)
        cancelButton = findViewById(R.id.btn_cancel)
        extendTimerButton = findViewById(R.id.ExtendTimer)
        iArrivedButton = findViewById(R.id.IArrived)

        val timeInSeconds = intent.getIntExtra("time", 0)
        Log.d("WhileInTripTime", "time in seconds:$timeInSeconds")
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
                timerTextView.text = getString(R.string.timer_finished)
                val intent = Intent(this@WhileInTrip, CheckArrival::class.java)
                startActivity(intent)
                stopService(voiceService)
            }
        }
        countdownTimer.start()

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val customerId = localDB.getInt("userId", -1)
        val tripId = localDB.getInt("tripId", -1)

        iArrivedButton.setOnClickListener {
            countdownTimer.cancel()
            timerTextView.text = getString(R.string.timer_stopped)
            cancelButton.isEnabled = false
            extendTimerButton.isEnabled = false
            iArrivedButton.isEnabled = false
            viewModel.endTrip("Bearer $token" , EndTripBody(tripId, customerId))
            stopService(voiceService)
        }

        extendTimerButton.setOnClickListener {
            countdownTimer.cancel()

            viewModel.extendTrip("Bearer $token", ExtendTripBody(tripId, customerId, MINUTES_TO_ADD))
        }
        cancelButton.setOnClickListener {
            countdownTimer.cancel()
            timerTextView.text = getString(R.string.timer_cancelled)
            cancelButton.isEnabled = false
            extendTimerButton.isEnabled = false
            iArrivedButton.isEnabled = false
            localDB.getInt("tripId", -1).let { tripId ->
                viewModel.cancelTrip("Bearer $token" , tripId, customerId )
            }
            stopService(voiceService)
        }
        observeResponses()
    }
    private fun checkAndRequestPermissions() {
        val permission = Manifest.permission.RECORD_AUDIO

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
        }
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.extendTripResponse,
            TripResponse::class.java,
            {
                countdownTimer = object : CountDownTimer(it.remainingTime * 1000L, 1000) {
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
                        Navigator(this@WhileInTrip).to(CheckArrival::class.java).andKeepStack()
                    }
                }
                countdownTimer.start()
                cancelButton.isEnabled = true
                extendTimerButton.isEnabled = true
                iArrivedButton.isEnabled = true
            },
            {}
        )

        ResponseHandler(this).observeResponse(
            viewModel.cancelTripResponse,
            Boolean::class.java,
            {
                Navigator(this).to(HomeActivity::class.java).andClearStack()
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
                Navigator(this).to(HomeActivity::class.java).andClearStack()
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        val voiceService = Intent(this, SpeechToTextService::class.java)
        stopService(voiceService)
    }
}