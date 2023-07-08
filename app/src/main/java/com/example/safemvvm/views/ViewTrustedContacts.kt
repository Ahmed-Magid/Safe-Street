package com.example.safemvvm.views

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safemvvm.R
import com.example.safemvvm.adapters.AddTrustedAdapter
import com.example.safemvvm.models.AddContactBody
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.TrustedContact
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.TrustedContactViewModel
import com.example.safemvvm.viewmodels.TrustedContactViewModelFactory

class ViewTrustedContacts : AppCompatActivity(),  AddTrustedAdapter.OnItemClickListener  {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: TrustedContactViewModel

    var contactList: MutableList<TrustedContact> = mutableListOf()
    val adapter = AddTrustedAdapter(contactList,this)
    var isZero = 1

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

        recyclerView.adapter = adapter

        val buttonAddTrusted = findViewById<Button>(R.id.addButton)
        buttonAddTrusted.setOnClickListener {
            val etcontactEmail = findViewById<EditText>(R.id.et_contactEmail)
            val contactEmail = etcontactEmail.text.toString()
            viewModel.addTrustedContact("Bearer $token", AddContactBody(userId,contactEmail))
            etcontactEmail.text = null
        }
        isZero = intent.getIntExtra("home", 1)
        observeResponses()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isZero == 0) {
            val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
            val token = localDB.getString("token", "empty")
            val userId = localDB.getInt("userId", -1)
            viewModel.logout("Bearer $token", IdBody(userId))
            Navigator(this).to(Login::class.java).andClearStack()
        }
    }

    override fun onDeleteClick(position: Int) {


        val localDB: SharedPreferences = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)

        viewModel.deleteTrustedContact("Bearer $token",userId,contactList[position].email)

        ResponseHandler(this).observeResponse(
            viewModel.deleteContactResponse,
            Boolean::class.java,
            {
                contactList.removeAt(position) // Assuming 'dataset' is the list of items in your adapter
                adapter.notifyItemRemoved(position)
            },
            {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.getContactsResponse,
            Array<TrustedContact>::class.java,
            {
                contactList.addAll(it)
                adapter.notifyDataSetChanged()
            },
            {
                if (isZero != 0)
                    Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )

        ResponseHandler(this).observeResponse(
            viewModel.addContactResponse,
            TrustedContact::class.java,
            {

                contactList.add(it)
                adapter.notifyItemInserted(contactList.size - 1)
                Toast.makeText(this, "User added successfully", Toast.LENGTH_LONG).show()
                if (isZero == 0) {
                    Handler().postDelayed({
                        Navigator(this).to(HomeActivity::class.java).andClearStack()
                    }, 1000)
                    isZero = 1
                }

            },
            {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
    }
}