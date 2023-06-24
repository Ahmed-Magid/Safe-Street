package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.adapters.AddTrustedAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ViewTrustedContacts : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trusted_contacts)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val data = listOf(
            "Name 1",
            "Name 2",
            "Name 3",
            "Name 4",
            "Name 5",
            "Name 6",
            "Name 7",
            "Name 8",
            "Name 9",
            "Name 10",
            "Name 11",
            "Name 12",
            "Name 13",
            "Name 14",
            "Name 15",
            "Name 16",
            "Name 17",
            "Name 18",
            "Name 19",
            "Name 20",
            "Name 21",
            "Name 22",
            "Name 23",
            "Name 24",
            "Name 25"
        )
        val adapter = AddTrustedAdapter(data)
        recyclerView.adapter = adapter

        val buttonAddTrusted = findViewById<FloatingActionButton>(R.id.addButton)
        buttonAddTrusted.setOnClickListener {
            val intent = Intent(this, AddTrustedActivity::class.java)
            startActivity(intent)
        }
    }
}