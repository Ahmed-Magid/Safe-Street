package com.example.safemvvm.managers

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.example.safemvvm.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.RecognitionAudio
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.RecognizeRequest
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechSettings
import com.google.protobuf.ByteString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Timer
import java.util.TimerTask

class SpeechToTextService : Service() {
    private val SAMPLE_RATE = 16000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    private val RECORDING_DURATION_MS = 5000L
    private val SAFE_WORD = "help"
    private val RECORDING_INTERVAL_MS = 1L // Interval between recordings
    private lateinit var timer : Timer

    private lateinit var audioRecord: AudioRecord
    private val mainHandler = Handler(Looper.getMainLooper())


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
            .setLanguageCode("en-US")
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


                if (transcript.contains(SAFE_WORD, ignoreCase = true)) {
                    // Execute your action when the keyword is detected
                    println("FIRE HERE")
                    val file = saveAudioToFile(audioData)
                    // Predict


                    file?.delete()
                }
            }
            speechClient.close()
        }

    }

    private fun saveAudioToFile(audioData: ByteArray): File? {
        val file = File.createTempFile("helpMessage", ".wav", getExternalFilesDir(null))
        return try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(audioData)
            fileOutputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}