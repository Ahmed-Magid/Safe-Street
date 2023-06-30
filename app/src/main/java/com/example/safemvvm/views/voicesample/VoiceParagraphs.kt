package com.example.safemvvm.views.voicesample

import android.Manifest
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.safemvvm.R
import com.example.safemvvm.models.User
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.RegistrationViewModel
import com.example.safemvvm.viewmodels.RegistrationViewModelFactory
import com.example.safemvvm.views.Login
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class VoiceParagraphs : AppCompatActivity() {
    private lateinit var viewModel: RegistrationViewModel
    private val fragmentList = listOf(Paragraph1(), Paragraph2(), Paragraph3(), Paragraph4())
    private var isRecording = false
    private lateinit var mediaRecorder: MediaRecorder
    private var recordNumber = 1
    private lateinit var outputFile: File
    private var recordFiles = mutableListOf<File>()

    @RequiresApi(Build.VERSION_CODES.S)
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
                startRecording()
            } else {
                recordButton.text = "Record"
                nextButton.isEnabled = true
                stopRecording()
            }

        }

        val user = intent.getSerializableExtra("userInfo") as User
        val repository = Repository()
        val viewModelFactory = RegistrationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(RegistrationViewModel::class.java)
        //TODO remove tags in logs with names at end of the project
        viewModel.registerResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if (responseMessage == "Created Successfully") {
                    Toast.makeText(
                        this,
                        "Please check your email for account activation.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("Arwa success reg", responseMessage)
                    Intent(this, Login::class.java).also { startActivity(it) }
                } else {
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    Log.d(
                        "Arwa success reach but error in fields",
                        responseMessage.toString()
                    )

                }
            } else {
                Toast.makeText(
                    this,
                    "something went wrong please try again later",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("Arwa not success", response.errorBody().toString())
                Log.d("Arwa not success", "${response.code()}")
            }
        }

        nextButton.setOnClickListener {
            nextButton.isEnabled = false
            recordNumber++
            recordFiles.add(outputFile)
            val currentItem = viewPager.currentItem
            if (currentItem < fragmentList.size - 1) {
                viewPager.currentItem = currentItem + 1
            } else if (currentItem == fragmentList.size - 1) {
                viewModel.register(user)
                recordFiles.forEach { it.delete() }
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }

        val progressText = findViewById<TextView>(R.id.progress)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // Not needed for this task
            }

            override fun onPageSelected(position: Int) {
                progressText.text = "${position + 1}/${fragmentList.size}"
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Not needed for this task
            }
        })
    }


    private fun startRecording() {
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        val outputDir = getExternalFilesDir(null)
        outputFile = File.createTempFile("record$recordNumber", ".mp4", outputDir)
        mediaRecorder.setOutputFile(outputFile.absolutePath)
        mediaRecorder.prepare()
        mediaRecorder.start()
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.reset()
        mediaRecorder.release()
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