package com.example.safemvvm.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EmergencyFired
import com.example.safemvvm.services.SpeechToTextService
import com.example.safemvvm.models.EndTripBody
import com.example.safemvvm.models.ExtendTripBody
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.WhileInTripViewModel
import com.example.safemvvm.viewmodels.WhileInTripViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

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
    private lateinit var emergencyType : EmergenciesEnum
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    lateinit var emergencyFired: EmergencyFired


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

        viewModel = ViewModelProvider(this,viewModelFactory).get(WhileInTripViewModel::class.java)
        startService(voiceService)

        viewModel.predictResponse.observe(this) {response ->
            if (response.isSuccessful && response.body() != null) {
                println("Prediction: ${response.body()}")
            }
        }


        // Initialize the views
        timerTextView = findViewById(R.id.timer)
        cancelButton = findViewById(R.id.btn_cancel)
        extendTimerButton = findViewById(R.id.ExtendTimer)
        iArrivedButton = findViewById(R.id.IArrived)
        //fireEmergencyButton = findViewById(R.id.FireEmergency)
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
                stopService(voiceService)
            }
        }

        // Start the countdown timer
        countdownTimer.start()

        // Set click listener for the FireEmergency button
//        fireEmergencyButton.setOnClickListener {
//            val intent = Intent(this, Emergencies::class.java)
//            startActivity(intent)
//        }


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
            stopService(voiceService)
        }

    }
    private fun checkAndRequestPermissions() {
        val permission = Manifest.permission.RECORD_AUDIO

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
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
                    //TODO: go to check emergency first then Send emergency to server
                    //viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, longitude, latitude, emergencyFired.type.toString()))
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

//
//    private val SAMPLE_RATE = 16000
//    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
//    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
//    private val BUFFER_SIZE = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
//    private lateinit var audioRecord: AudioRecord
//    private lateinit var speechClient: SpeechClient
//    private val mainHandler = Handler(Looper.getMainLooper())
//
//    private fun startRecording() {
//        println("Started")
//        val outputDir = getExternalFilesDir(null)
//        //outputFile = File.createTempFile("helpMessage", ".3gp", outputDir)
//        val outputFile = File(Environment.getExternalStorageDirectory(), "recording.3gp")
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Log.e("TAG", "Record audio permission not granted")
//            return
//        }
//        audioRecord = AudioRecord(
//            MediaRecorder.AudioSource.MIC,
//            SAMPLE_RATE,
//            CHANNEL_CONFIG,
//            AUDIO_FORMAT,
//            BUFFER_SIZE
//        )
//        val audioBuffer = ByteArray(BUFFER_SIZE)
//        audioRecord.startRecording()
//
//        val outputStream = ByteArrayOutputStream()
//
//        while (true) {
//            val numBytes = audioRecord.read(audioBuffer, 0, BUFFER_SIZE)
//            outputStream.write(audioBuffer, 0, numBytes)
//
//            // Check if recording should stop
//            // Implement your own logic here, e.g., based on a button press or a specific duration
//
//            // Example: Stop recording after 5 seconds
//            if (outputStream.size() >= SAMPLE_RATE * 5) {
//                audioRecord.stop()
//                audioRecord.release()
//                val audioData = outputStream.toByteArray()
//                // Pass the audio data to the speech-to-text API for recognition
//                sendRecordingForTranscription(audioData)
//                break
//            }
//        }
//
//
//
//    /*.apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//            setAudioEncoder(MediaRecorder.AudioEncoder.)
//            setAudioSamplingRate(16000)
//            setOutputFile(outputFile.absolutePath)
//            setMaxDuration(5000) // 5 seconds*/
////        }
//       /* mediaRecorder.prepare()
//        mediaRecorder.start()*/
//        // Wait for 5 seconds
////        Handler(Looper.getMainLooper()).postDelayed({
////            audioRecord.stop()
////            audioRecord.release()
////            println("Stopped")
////            println(outputFile.absolutePath)
////            val audioData = outputStream.toByteArray()
////            // Pass the audio data to the speech-to-text API for recognition
////            sendRecordingForTranscription(audioData)
////            /*mediaRecorder.stop()
////            mediaRecorder.release()
////            println("Stopped")
////            println(outputFile.absolutePath)
////            if (outputDir != null) {
////                outputDir.listFiles()?.let { println("Audio Files: ${it.size}") }
////            }
////            // Send the recording for transcription
////            sendRecordingForTranscription(outputFile)*/
////        }, 5000)
//    }
//
//
//    private fun sendRecordingForTranscription(bytes: ByteArray) {
//        // Create a SpeechClient using your authentication credentials
//        val credentials = GoogleCredentials.fromStream(resources.openRawResource(R.raw.credential))
//        val speechSettings = SpeechSettings.newBuilder().setCredentialsProvider { credentials }.build()
//        val speechClient = SpeechClient.create(speechSettings)
//
//        // Read the audio bytes
////        val audioData = Files.readAllBytes(bytes.toPath())
//        val audioBytes = ByteString.copyFrom(bytes)
//
//        // Build the recognition request
//        val recognitionConfig = RecognitionConfig.newBuilder()
//            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
//            .setSampleRateHertz(SAMPLE_RATE)
//            .setLanguageCode("en-US")
//            .build()
//        val audio = RecognitionAudio.newBuilder()
//            .setContent(audioBytes)
//            .build()
//        val request = RecognizeRequest.newBuilder()
//            .setConfig(recognitionConfig)
//            .setAudio(audio)
//            .build()
//
//        // Send the recognition request
//        val response = speechClient.recognize(request)
//        println(response)
//
//        // Process the transcription
//        val results = response.resultsList
//        if (results.isNotEmpty()) {
//            val transcription = results[0].alternativesList[0].transcript
//            println("User said: $transcription")
//            if (transcription.contains("help", ignoreCase = true)) {
//                // Execute the action for help
//                println("Fire Emergency Here")
//                startRecording()
//            } else {
//                // Repeat the process
//                println("Repeating")
//                startRecording()
//            }
//        } else {
//            println("Result is empty")
//            startRecording()
//        }
//
//        speechClient.close()
//    }

}