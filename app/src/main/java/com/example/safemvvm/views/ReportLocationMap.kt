package com.example.safemvvm.views

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.safemvvm.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.gms.common.api.Status
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager

class ReportLocationMap : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "com.example.safemvvm.views.ReportLocationMap"
    private lateinit var mMap: GoogleMap
    private var source: LatLng? = null
    private var reportLocation: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val AUTOCOMPLETE_REQUEST_CODE = 2
    private val DEFAULT_ZOOM = 15f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_report_location_map)

        Places.initialize(applicationContext, "AIzaSyBCJXuU6WB-ARQgtiDBl7KRhZsSrvwdYvk")

        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        // Start the AutocompleteActivity
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setCountry("EG")
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Get the LatLng of the selected place
                reportLocation = place.latLng

                if(reportLocation != null) {
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(reportLocation!!).title("Destination"))
                }

                // Move the camera to the selected place
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(reportLocation!!, DEFAULT_ZOOM)
                mMap.animateCamera(cameraUpdate)
            }

            override fun onError(status: Status) {
                // Handle the error
                Log.i(TAG, "An error occurred: $status")
                Toast.makeText(this@ReportLocationMap, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })



        //works ok
        val setCurrentLocation = findViewById<Button>(R.id.CurrLocation)
        setCurrentLocation.setOnClickListener {
            // Get the user's current location and set it as the source
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the missing permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    mMap.clear()
                    reportLocation = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(reportLocation!!))

                    // Move the camera to the user's current location
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(reportLocation!!, 18f)
                    mMap.animateCamera(cameraUpdate)
                }
            }
        }

        val confirm = findViewById<Button>(R.id.confirm_button)
        confirm.setOnClickListener {
            if(reportLocation != null) {
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(reportLocation!!).title("Reported Location"))
                val intent = Intent(this, AddReportActivity::class.java)
                intent.putExtra("location", reportLocation)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Please select a location to report", Toast.LENGTH_SHORT).show()
            }

        }
        /*val back = findViewById<Button>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }*/
        val resetButton = findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener {
            mMap.clear()
            reportLocation = null
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not already granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Get the user's last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Move the camera to the user's current location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                reportLocation = currentLatLng
            }
        }

        mMap.setOnMapClickListener { latLng ->
            if (reportLocation == null) {
                reportLocation = latLng
                mMap.addMarker(MarkerOptions().position(latLng).title("Destination"))
            }
            else{
                mMap.clear()
                reportLocation = latLng
                mMap.addMarker(MarkerOptions().position(latLng).title("Destination"))
            }
        }
    }
}