package com.example.safemvvm.views.voicesample

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.safemvvm.R
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.VoiceParagraphViewModel
import com.example.safemvvm.viewmodels.VoiceParagraphViewModelFactory
import com.example.safemvvm.views.HomeActivity
import com.example.safemvvm.views.Login
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class VoiceParagraphs : AppCompatActivity() {
    private val SAMPLE_RATE = 16000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    private lateinit var viewModel: VoiceParagraphViewModel
    private val fragmentList = listOf(Paragraph1(), Paragraph2(), Paragraph3(), Paragraph4())
    private var isRecording = false
    private lateinit var audioRecord: AudioRecord
    private var recordNumber = 1
    private lateinit var outputFile: File
    private var recordFiles = mutableListOf<File>()
    private lateinit var outputStream: ByteArrayOutputStream

    //@RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_paragraphs)
        val nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.isEnabled = false

        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val adapter = MyPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

        val permission = Manifest.permission.RECORD_AUDIO
        ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)

        val recordButton = findViewById<Button>(R.id.record)
        recordButton.setOnClickListener {

            isRecording = !isRecording
            if (isRecording) {
                recordButton.text = "Stop"
                GlobalScope.launch(Dispatchers.IO) {
                    startRecording()
                }
                //startRecording()
            } else {
                recordButton.text = "Record"
                nextButton.isEnabled = true
                stopRecording()
            }

        }

        val repository = Repository()
        val viewModelFactory = VoiceParagraphViewModelFactory(repository)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(VoiceParagraphViewModel::class.java)
        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)

        nextButton.setOnClickListener {
            nextButton.isEnabled = false
            recordNumber++
            recordFiles.add(outputFile)
            val currentItem = viewPager.currentItem
            if (currentItem < fragmentList.size - 1) {
                viewPager.currentItem = currentItem + 1
            } else if (currentItem == fragmentList.size - 1) {
                val records: List<MultipartBody.Part> = recordFiles.map { file ->
                    val requestFile =
                        RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("records", file.name, requestFile)
                }
                viewModel.train(records, localDB.getInt("userId", -1))
                recordButton.isEnabled = false
            }
        }

        val progressText = findViewById<TextView>(R.id.progress)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                progressText.text = "${position + 1}/${fragmentList.size}"
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        observeResponses()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isRecording) {
            audioRecord.stop()
            audioRecord.release()
        }
        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", "empty")
        val userId = localDB.getInt("userId", -1)
        viewModel.logout("Bearer $token", IdBody(userId))
        recordFiles.forEach { it.delete() }
        Navigator(this).to(Login::class.java).andClearStack()
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )

        val audioBuffer = ByteArray(BUFFER_SIZE)
        audioRecord.startRecording()

        outputStream = ByteArrayOutputStream()

        while (isRecording) {
            try {
                val numBytes = audioRecord.read(audioBuffer, 0, BUFFER_SIZE)
                outputStream.write(audioBuffer, 0, numBytes)
            } catch (e: Exception) {
                break
            }
        }
    }

    private fun stopRecording() {
        audioRecord.stop()
        audioRecord.release()
        val audioData = outputStream.toByteArray()
        outputFile = generateAudioFile(audioData, getExternalFilesDir(null)?.absolutePath + "/record$recordNumber.wav")
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

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.savedResponse,
            Boolean::class.java,
            {
                Toast.makeText(this, "Audio Saved", Toast.LENGTH_SHORT).show()
                Navigator(this).to(HomeActivity::class.java).andClearStack()
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )

        ResponseHandler(this).observeFlaskResponse(
            viewModel.trainResponse,
        ) {
            recordFiles.forEach { it.delete() }
            LocalDatabaseManager(this).saved(true)
            val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
            val token = localDB.getString("token", "empty")
            viewModel.setSaved("Bearer $token", 1, localDB.getInt("userId", -1))
        }

        ResponseHandler(this).observeResponse(
            viewModel.logoutResponse,
            Boolean::class.java,
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
    }


    inner class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }
    }
}