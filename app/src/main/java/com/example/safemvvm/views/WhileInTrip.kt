package com.example.safemvvm.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.ExtendTripBody
import com.example.safemvvm.models.LoginResponse
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.WhileInTripViewModel
import com.example.safemvvm.viewmodels.WhileInTripViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import java.io.File

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
    private val MINUTES_TO_ADD = 5
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var outputFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_while_in_trip)

        checkAndRequestPermissions()

        // Create SpeechRecognizer instance
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Set up RecognitionListener
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // Called when the speech recognition engine is ready for audio input
                startRecording()
                println("Ready")
            }

            override fun onBeginningOfSpeech() {
                // Called when the user has started speaking

                println("Began speaking")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Called when the RMS dB value of the audio input changes
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Called when a partial recognition result is available
                buffer?.let {
                    print("Buffer here")
                    // Perform some operations on the audio buffer
                    // ...
                }
            }

            override fun onEndOfSpeech() {
                // Called when the user has finished speaking
                println("End here")
            }

            override fun onError(error: Int) {
                // Called when an error occurs during speech recognition
                println("Error")
                speechRecognizer.startListening(speechRecognizerIntent)
                stopRecording()
                outputFile.delete()

            }

            override fun onResults(results: Bundle?) {
                // Called when the final recognition results are available
                val speechResults = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val result = speechResults?.get(0)
                println(result)
                if (result != null && result.contains("الحقوني")) {
                    println("FIRE EMERGENCY NOWWWWWWWWWWWWWWWWW")
                    // Predict
                }
//                speechResults?.let {
//                    // Process the recognized speech results
//                    print(it)
//                }
                stopRecording()
                outputFile.delete()
                speechRecognizer.startListening(speechRecognizerIntent)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Called when partial recognition results are available
                val speechResults = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                println(speechResults)
//                speechResults?.let {
//                    // Process the recognized speech results
//                    print(it)
//                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Called when a speech recognition event occurs
            }
        }

        speechRecognizer.setRecognitionListener(recognitionListener)

        // Start speech recognition
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // Set the language to Arabic

        speechRecognizer.startListening(speechRecognizerIntent)

        // Initialize the views
        timerTextView = findViewById(R.id.timer)
        cancelButton = findViewById(R.id.btn_cancel)
        extendTimerButton = findViewById(R.id.ExtendTimer)
        iArrivedButton = findViewById(R.id.IArrived)
        fireEmergencyButton = findViewById(R.id.FireEmergency)
        //take timeInSeconds from CreateTripActivity
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
                // Do something when the countdown timer finishes
                timerTextView.text = getString(R.string.timer_finished)
                val intent = Intent(this@WhileInTrip, CheckArrival::class.java)
                startActivity(intent)
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
        val tripId = localDB.getInt("tripId", -1)


        viewModel.endTripResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                Log.d("endTrip001", "endTrip: ${response.body()}  success" )
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else if(response.message().toString().contains("Trip not Found") ){
                Log.d("endTrip002", "Trip not Found" )
                Toast.makeText(this, "Trip not found", Toast.LENGTH_LONG).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else {
                Log.d("endTrip003", "endTrip: ${response.errorBody()} testing" )
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
            viewModel.endTrip("Bearer $token" , EndTripBody(tripId, customerId))
        }

        // Set click listener for the ExtendTimer button
        extendTimerButton.setOnClickListener {
            countdownTimer.cancel()
            // Extract the current time left from the timerTextView
//            val currentTimeLeft = timerTextView.text.toString().split(" ")[2].toInt()
//
//            // Add 5 minutes (300 seconds) to the current time left
//            secondsToAdd = (currentTimeLeft + MINUTES_TO_ADD) * 60
            viewModel.extendTrip("Bearer $token", ExtendTripBody(tripId, customerId, MINUTES_TO_ADD))

            // Start a new countdown timer with the updated time left

        }

        viewModel.extendTripResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message
                if (responseMessage == "Executed Successfully") {
                    Log.d("extendTrip001", "extendTrip: ${response.body()}  success" )
                    val data = Gson().fromJson(response.body()?.data.toString(), TripResponse::class.java)

                    countdownTimer = object : CountDownTimer(data.remainingTime * 1000L, 1000) {
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

        viewModel.cancelTripResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message
                if (responseMessage == "Deleted Successfully") {
                    Log.d("cancelTrip001", "cancelTrip: ${response.body()}  success" )
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else if (responseMessage == "Authentication Error") {
                    Log.d("cancelTrip002", "cancelTrip: ${response.body()}" )
                    Toast.makeText(this, "Session Expired", Toast.LENGTH_LONG).show()
                    localDB.edit().apply {
                        putString("token", "empty")
                        apply()
                    }
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    Log.d("cancelTrip003", "cancelTrip: ${response.body()}" )
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }

            } else {
                Log.d("cancelTrip004", "cancelTrip: ${response.errorBody()} testing" )
                //Long Toast
                Toast.makeText(this, "Error: ${response.errorBody()}", Toast.LENGTH_LONG).show()
            }
        }
        // Set click listener for the cancel button
        cancelButton.setOnClickListener {
            countdownTimer.cancel()
            timerTextView.text = getString(R.string.timer_cancelled)
            cancelButton.isEnabled = false
            extendTimerButton.isEnabled = false
            iArrivedButton.isEnabled = false
            localDB.getInt("tripId", -1).let { tripId ->
                viewModel.cancelTrip("Bearer $token" , tripId, customerId )
            }
        }

    }
    private fun checkAndRequestPermissions() {
        val permission = Manifest.permission.RECORD_AUDIO

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
        }
    }

    private fun startRecording() {
        println("Start Recording")
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        val outputDir = getExternalFilesDir(null)
        outputFile = File.createTempFile("helpMessage", ".mp4", outputDir)
        mediaRecorder.setOutputFile(outputFile.absolutePath)
        mediaRecorder.prepare()
        mediaRecorder.start()
    }

    private fun stopRecording() {
        println("Stop Recording")
        println(outputFile)
        mediaRecorder.stop()
        mediaRecorder.reset()
        mediaRecorder.release()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release SpeechRecognizer resources
        speechRecognizer.destroy()
    }
}