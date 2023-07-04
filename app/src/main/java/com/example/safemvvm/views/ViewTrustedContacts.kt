package com.example.safemvvm.views

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
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

class ViewTrustedContacts : AppCompatActivity(),  AddTrustedAdapter.OnItemClickListener  {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: TrustedContactViewModel

    var contactList: MutableList<TrustedContact> = mutableListOf()
    val adapter = AddTrustedAdapter(contactList,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_trusted_contacts)

        val localDB: SharedPreferences = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val repository = Repository()
        val viewModelFactory = TrustedContactViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(TrustedContactViewModel::class.java)

        viewModel.getAllTrusted("Bearer $token", userId)
        // contact list will be from API


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
                    if(responseMessage == "Authentication Error"){
                        val intent = Intent(this, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            } else {
                Log.d("getTrusted006", response.errorBody().toString())
                if(response.code()==403 || response.code()==410){
                    Log.d("Trusted006", "code is 403 or 410")
                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }else{
                    Toast.makeText(
                        this,
                        "something went wrong please try again later",
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
                    if(responseMessage == "Authentication Error"){
                        val intent = Intent(this, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            } else {
                Log.d("Trusted006", response.errorBody().toString())
                if(response.code()==403 || response.code()==410){
                    Log.d("Trusted006", "code is 403 or 410")
                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
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
        val buttonAddTrusted = findViewById<Button>(R.id.addButton)
        buttonAddTrusted.setOnClickListener {
            val etcontactEmail = findViewById<EditText>(R.id.et_contactEmail)
            val contactEmail = etcontactEmail.text.toString()
            viewModel.addTrustedContact("Bearer $token", AddContactBody(userId,contactEmail))
            etcontactEmail.text = null
        }
    }

    override fun onDeleteClick(position: Int) {


        val localDB: SharedPreferences = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)

        viewModel.deleteTrustedContact("Bearer $token",userId,contactList[position].email)

        viewModel.deleteContactResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                val responseMessage = response.body()?.message

                if(responseMessage == "Deleted Successfully"){
                    Log.d("deleteTrusted002","deleted")
                    contactList.removeAt(position) // Assuming 'dataset' is the list of items in your adapter
                    adapter.notifyItemRemoved(position)

                }else {
                    Log.d("deleteTrusted003","success but not deleted")
                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
                    if(responseMessage == "Authentication Error"){
                        val intent = Intent(this, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            } else {
                Log.d("deleteTrusted006", response.errorBody().toString())
                if(response.code()==403 || response.code()==410){
                    Log.d("Trusted006", "code is 403 or 410")
                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }else{
                    Toast.makeText(
                        this,
                        "something went wrong please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d("deleteTrusted004", response.errorBody().toString())
                    Log.d("deleteTrusted005", "${response.code()}")
                }

            }
        }
    }
}