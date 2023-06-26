package com.example.safemvvm.voicesample

import android.Manifest
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.safemvvm.R
import com.example.safemvvm.views.HomeActivity
import com.example.voiceparagraphs.Paragraph1
import com.example.voiceparagraphs.Paragraph2
import com.example.voiceparagraphs.Paragraph3
import com.example.voiceparagraphs.Paragraph4
import java.io.File

class VoiceParagraphs : AppCompatActivity() {
    private val fragmentList = listOf(Paragraph1(), Paragraph2(), Paragraph3(), Paragraph4())
    private var isRecording = false
    private lateinit var mediaRecorder: MediaRecorder
    private var recordNumber = 1
    private lateinit var outputFile: String
    private var recordFilePaths = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_paragraphs)
        val nextButton = findViewById<Button>(R.id.nextButton)
        nextButton.isEnabled = false

        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val adapter = MyPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

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


        nextButton.setOnClickListener {
            nextButton.isEnabled = false
            recordNumber++
            recordFilePaths.add(outputFile)
            val currentItem = viewPager.currentItem
            if (currentItem < fragmentList.size - 1) {
                viewPager.currentItem = currentItem + 1
            }
            else if(currentItem == fragmentList.size - 1){
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }

        val progressText = findViewById<TextView>(R.id.progress)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecording() {
        val permission = Manifest.permission.RECORD_AUDIO
        ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
        mediaRecorder = MediaRecorder(this)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        val outputDir = getExternalFilesDir(null)
        outputFile = File.createTempFile("record$recordNumber", ".mp4", outputDir).absolutePath
        mediaRecorder.setOutputFile(outputFile)
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