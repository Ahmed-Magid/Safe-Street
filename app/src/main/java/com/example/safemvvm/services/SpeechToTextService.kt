package com.example.safemvvm.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.EmergencyBody
import com.example.safemvvm.models.EmergencyFired
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.WhileInTripViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.RecognizeRequest
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechSettings
import com.google.protobuf.ByteString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


class SpeechToTextService : Service() {
    private val SAMPLE_RATE = 16000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    private val RECORDING_DURATION_MS = 5000L
    private val SAFE_WORDS = mutableListOf("الحقوني", "الحقونى", "help", "ساعدوني", "ساعدونى")
    private val RECORDING_INTERVAL_MS = 1000L // Interval between recordings
    private lateinit var timer : Timer
    private var longitude = 0.0
    private var latitude = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var audioRecord: AudioRecord
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startPeriodicRecording()
        return START_STICKY
    }

    private fun startPeriodicRecording() {
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                startRecordingAndTranscription()
            }
        }, 0, RECORDING_INTERVAL_MS)
    }

    @SuppressLint("MissingPermission")
    private fun startRecordingAndTranscription() {

        println("Started")

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )

        val audioBuffer = ByteArray(BUFFER_SIZE)
        audioRecord.startRecording()

        val outputStream = ByteArrayOutputStream()

        mainHandler.postDelayed({
            println("Stopped")
            audioRecord.stop()
            audioRecord.release()

            val audioData = outputStream.toByteArray()

            // Pass the audio data to the speech-to-text API for transcription
            transcribeSpeech(audioData)
        }, RECORDING_DURATION_MS)

        while (true) {
            try {
                val numBytes = audioRecord.read(audioBuffer, 0, BUFFER_SIZE)
                outputStream.write(audioBuffer, 0, numBytes)
            } catch (e: Exception) {
                break
            }
        }
    }

    private fun transcribeSpeech(audioData: ByteArray) {
        val credentials = GoogleCredentials.fromStream(resources.openRawResource(R.raw.credential))
        val speechSettings = SpeechSettings.newBuilder().setCredentialsProvider { credentials }.build()
        val speechClient = SpeechClient.create(speechSettings)
        val audio = RecognitionAudio.newBuilder()
            .setContent(ByteString.copyFrom(audioData))
            .build()

        val config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
            .setSampleRateHertz(SAMPLE_RATE)
            .setLanguageCode("ar")
            .build()

        val request = RecognizeRequest.newBuilder()
            .setConfig(config)
            .setAudio(audio)
            .build()


        GlobalScope.launch {
            val response = speechClient.recognize(request)

            for (result in response.resultsList) {
                val transcript = result.alternativesList[0].transcript
                println(transcript)


                SAFE_WORDS.forEach { word ->
                    if (transcript.contains(word, ignoreCase = true)) {
                        // Execute your action when the keyword is detected
                        println("FIRE HERE")
                        val file = generateAudioFile(audioData, getExternalFilesDir(null)?.absolutePath + "/helpMessage.wav")

                        println(file.absolutePath)
                        val requestFile =
                            file.let {
                                RequestBody.create("multipart/form-data".toMediaTypeOrNull(),
                                    it
                                )
                            }
                        val record = requestFile.let {
                            MultipartBody.Part.createFormData("record", file.name,
                                it
                            )
                        }
                        // Predict
                        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
                        val userId = localDB.getInt("userId", -1)
                        val token = localDB.getString("token", "empty")
                        val repository = Repository()
                        if (repository.predict(record, userId).body() == "1") {
                            getCurrentLocation()
                            repository.fireEmergency("Bearer $token", EmergencyBody(userId, longitude, latitude, EmergenciesEnum.IN_DANGER.toString()))
                        }

                        file.delete()
                        return@forEach
                    }
                }

            }
            speechClient.close()
        }

    }

    fun generateAudioFile(audioData: ByteArray, filePath: String): File {
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AUDIO_FORMAT,
            audioData.size,
            AudioTrack.MODE_STATIC
        )

        audioTrack.write(audioData, 0, audioData.size)

        val outputFile = File(filePath)
        val outputStream = FileOutputStream(outputFile)
        outputStream.write(audioData)
        outputStream.close()
        audioTrack.release()
        return outputFile
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        // Get the user's current location using the FusedLocationProviderClient
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Format the current time

                    // Create a new EmergencyFired object with the location, time, and emergency type

                    // Display a toast message with the location and time information
                    latitude = location.latitude
                    longitude = location.longitude

                    // Log the location and time information
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}