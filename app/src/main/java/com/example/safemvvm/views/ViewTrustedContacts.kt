package com.example.safemvvm.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.adapters.AddTrustedAdapter
import com.example.safemvvm.models.AddContactBody
import com.example.safemvvm.models.LoginResponse
import com.example.safemvvm.models.TrustedContact
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.MainViewModel
import com.example.safemvvm.viewmodels.MainViewModelFactory
import com.example.safemvvm.viewmodels.TrustedContactViewModel
import com.example.safemvvm.viewmodels.TrustedContactViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import okhttp3.internal.notify

class ViewTrustedContacts : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: TrustedContactViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trusted_contacts)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val repository = Repository()
        val viewModelFactory = TrustedContactViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(TrustedContactViewModel::class.java)


        // contact list will be from API
        var contactList = mutableListOf(
            TrustedContact("Arwa Hazem","arwa@gmail.com"),
            TrustedContact("Yara","yara@gmail.com"),
            TrustedContact("magid","magid@gmail.com")
        )


        val adapter = AddTrustedAdapter(contactList)
        recyclerView.adapter = adapter

        viewModel.addContactResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Created Successfully"){
                    Log.d("Trusted002","Added")
                    val data = Gson().fromJson(response.body()?.data.toString(), TrustedContact::class.java)
                    contactList.add(data)
                    //modify the recycler view with the new added contact
                    adapter.notifyItemInserted(contactList.size - 1)

                }else {
                    Log.d("Trusted003","success but not added")
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                }
            } else {
                Log.d("Trusted006", response.errorBody().toString())
                if(response.code()==403 || response.code()==410){
                    Log.d("Trusted006", "code is 403 or 410")
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(
                        this,
                        "something went wrong please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("Trusted004", response.errorBody().toString())
                    Log.d("Trusted005", "${response.code()}")
                }

            }
        }
        val buttonAddTrusted = findViewById<FloatingActionButton>(R.id.addButton)
        buttonAddTrusted.setOnClickListener {
            /*val etcontactEmail = findViewById<EditText>(R.id.et_contactEmail)
           // val etcontactName = findViewById<EditText>(R.id.et_contactName)

            val contactEmail = etcontactEmail.text.toString()
            //val contactName = etcontactName.text.toString()

            val trustedContact = TrustedContact(contactName,contactEmail)
            contactList.add(trustedContact)
            //modify the recycler view with the new added contact
            adapter.notifyItemInserted(contactList.size - 1)
            // clear edit views
                etcontactEmail.text = null*/
            val etcontactEmail = findViewById<EditText>(R.id.et_contactEmail)
            val contactEmail = etcontactEmail.text.toString()
            val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
            val token = localDB.getString("token","empty")
            val userId = localDB.getInt("userId",-1)
            viewModel.addTrustedContact("Bearer $token", AddContactBody(userId,contactEmail))
        }
    }
}