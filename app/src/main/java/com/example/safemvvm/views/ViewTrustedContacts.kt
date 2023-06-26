package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.adapters.AddTrustedAdapter
import com.example.safemvvm.models.TrustedContact
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.internal.notify

class ViewTrustedContacts : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trusted_contacts)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // contact list will be from API
        var contactList = mutableListOf(
            TrustedContact("Arwa Hazem","arwa@gmail.com"),
            TrustedContact("Yara","yara@gmail.com"),
            TrustedContact("magid","magid@gmail.com")
        )

        val adapter = AddTrustedAdapter(contactList)
        recyclerView.adapter = adapter

        val buttonAddTrusted = findViewById<FloatingActionButton>(R.id.addButton)
        buttonAddTrusted.setOnClickListener {
            val etcontactEmail = findViewById<EditText>(R.id.et_contactEmail)
            val etcontactName = findViewById<EditText>(R.id.et_contactName)

            val contactEmail = etcontactEmail.text.toString()
            val contactName = etcontactName.text.toString()

            val trustedContact = TrustedContact(contactName,contactEmail)
            contactList.add(trustedContact)
            //modify the recycler view with the new added contact
            adapter.notifyItemInserted(contactList.size - 1)
            // clear edit views
                etcontactEmail.text = null
                etcontactName.text = null

        }
    }
}