//package com.example.safemvvm.views
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.Location
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.lifecycle.ViewModelProvider
//import com.example.safemvvm.R
//import com.example.safemvvm.models.EmergenciesEnum
//import com.example.safemvvm.models.EmergencyBody
//import com.example.safemvvm.models.EmergencyFired
//import com.example.safemvvm.repository.Repository
//import com.example.safemvvm.viewmodels.EmergenciesViewModel
//import com.example.safemvvm.viewmodels.EmergenciesViewModelFactory
//import com.example.safemvvm.viewmodels.HomeViewModel
//import com.example.safemvvm.viewmodels.HomeViewModelFactory
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.gson.Gson
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class Emergencies : AppCompatActivity() {
//    private lateinit var viewModel: EmergenciesViewModel
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private val REQUEST_LOCATION_PERMISSION = 1
//    lateinit var emergencyFired: EmergencyFired
//    lateinit var emergencyType : EmergenciesEnum
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_emergencies)
//        val repository = Repository()
//        val viewModelFactory = EmergenciesViewModelFactory(repository)
//        viewModel = ViewModelProvider(this,viewModelFactory).get(EmergenciesViewModel::class.java)
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        val kidnappingButton = findViewById<Button>(R.id.btn_kidnapping)
//        val harassmentButton = findViewById<Button>(R.id.btn_harassment)
//        val fireButton = findViewById<Button>(R.id.btn_fire)
//        val carFaultButton = findViewById<Button>(R.id.btn_carFault)
//
//
//        kidnappingButton.setOnClickListener {
//            emergencyType = EmergenciesEnum.KIDNAPPING
//            getCurrentLocation()
//        }
//
//        harassmentButton.setOnClickListener {
//            emergencyType = EmergenciesEnum.HARASSMENT
//            getCurrentLocation()
//        }
//
//        fireButton.setOnClickListener {
//            emergencyType = EmergenciesEnum.FIRE
//            getCurrentLocation()
//        }
//
//        carFaultButton.setOnClickListener {
//            emergencyType = EmergenciesEnum.CAR_FAULT
//            getCurrentLocation()
//        }
//
//        viewModel.fireEmergencyResponse.observe(this) { response ->
//            if (response.isSuccessful && response.body() != null) {
//                val responseMessage = response.body()?.message
//
//                if(responseMessage == "Created Successfully") {
//                    Log.d(
//                        "emergency001",
//                        responseMessage.toString()
//                    )
//                    Toast.makeText(this, "Emergency fired successfully", Toast.LENGTH_SHORT).show()
//                }else {
//                    Toast.makeText(this, responseMessage, Toast.LENGTH_LONG).show()
//                    Log.d(
//                        "emergency002",
//                        responseMessage.toString()
//                    )
//                }
//            } else {
//
//                Log.d("emergency004", response.errorBody().toString())
//                Log.d("emergency005", "${response.code()}")
//                if(response.code()==403 || response.code()==410){
//                    Log.d("emergency006", "code is 403 or 410")
//                    Toast.makeText(this, "session expired", Toast.LENGTH_LONG).show()
//                    val intent = Intent(this, Login::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(intent)
//                } else {
//                    Toast.makeText(
//                        this,
//                        "something went wrong please try again later",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }
////    private fun getCurrentLocation() {
////        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
////            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
////            return
////        }
////
////        fusedLocationClient.lastLocation
////            .addOnSuccessListener { location: Location? ->
////                if (location != null) {
////                    val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
////                    val token = localDB.getString("token","empty")
////                    val userId = localDB.getInt("userId",-1)
////                    val latitude = location.latitude
////                    val longitude = location.longitude
////                    val time = System.currentTimeMillis()
////                    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
////                    val formattedTime = dateFormat.format(time)
////                    emergencyFired = EmergencyFired(location, formattedTime, emergencyType)
////                    println(emergencyFired.type.toString())
////                    viewModel.fireEmergency("Bearer $token", EmergencyBody(userId, longitude, latitude, emergencyFired.type.toString()))
////                    Toast.makeText(this, "Latitude: $latitude, Longitude: $longitude, $formattedTime , $emergencyType ",Toast.LENGTH_LONG).show()
////                    Log.d("TAG", "Latitude: $latitude, Longitude: $longitude, $formattedTime , $emergencyType ")
////
////                } else {
////                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
////                }
////            }
////    }
//
////    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
////        when (requestCode) {
////            REQUEST_LOCATION_PERMISSION -> {
////                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
////                    getCurrentLocation()
////                } else {
////                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
////                }
////                return
////            }
////        }
////    }
//}