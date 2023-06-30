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
import com.example.safemvvm.models.TrustedContact
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.TrustedContactViewModel
import com.example.safemvvm.viewmodels.TrustedContactViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ViewTrustedContacts : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: TrustedContactViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trusted_contacts)

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val repository = Repository()
        val viewModelFactory = TrustedContactViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(TrustedContactViewModel::class.java)

        viewModel.getAllTrusted("Bearer $token", userId)
        // contact list will be from API
        var contactList: MutableList<TrustedContact> = mutableListOf()
        val adapter = AddTrustedAdapter(contactList)
        recyclerView.adapter = adapter



        viewModel.getContactsResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Executed Successfully"){
                    Log.d("getTrusted002","get sll trusted success")
                    println("heelo1")
                    println(response.body()?.data.toString())

                    val apiContactList: List<TrustedContact> = Gson().fromJson(response.body()?.data.toString(), object : TypeToken<List<TrustedContact>>() {}.type)
                    contactList.addAll(apiContactList)
                    adapter.notifyDataSetChanged()

                }else {
                    Log.d("getTrusted003","success but not retrieved")
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                }
            } else {
                Log.d("getTrusted006", response.errorBody().toString())
                if(response.code()==403 || response.code()==410){
                    Log.d("Trusted006", "code is 403 or 410")
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(
                        this,
                        "something went wrong please try again laterr",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("getTrusted004", response.errorBody().toString())
                    Log.d("getTrusted005", "${response.code()}")
                }

            }
        }



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
                        "something went wrong please try again laterr",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("Trusted004", response.errorBody().toString())
                    Log.d("Trusted005", "${response.code()}")
                }

            }
        }
        val buttonAddTrusted = findViewById<FloatingActionButton>(R.id.addButton)
        buttonAddTrusted.setOnClickListener {
            val etcontactEmail = findViewById<EditText>(R.id.et_contactEmail)
            val contactEmail = etcontactEmail.text.toString()
            viewModel.addTrustedContact("Bearer $token", AddContactBody(userId,contactEmail))
            etcontactEmail.text = null
        }
    }
}