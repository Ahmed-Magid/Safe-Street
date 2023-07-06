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
import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.models.Location
import com.example.safemvvm.models.Report
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.viewmodels.AddReportViewModel
import com.example.safemvvm.viewmodels.AddReportViewModelFactory
import com.example.safemvvm.viewmodels.ReportLocationMapViewModel
import com.example.safemvvm.viewmodels.ReportLocationMapViewModelFactory
import com.google.android.gms.maps.GoogleMap.OnCircleClickListener
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReportLocationMap : AppCompatActivity(), OnMapReadyCallback, OnCircleClickListener {
    private val TAG = "com.example.safemvvm.views.ReportLocationMap"
    private lateinit var viewModel: ReportLocationMapViewModel
    private lateinit var mMap: GoogleMap
    private var source: LatLng? = null
    private var reportLocation: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val AUTOCOMPLETE_REQUEST_CODE = 2
    private val DEFAULT_ZOOM = 15f
    private lateinit var setCurrentLocation: ImageView
    private lateinit var confirm: Button
    private lateinit var resetButton: Button
    private var circles = mutableListOf<Circle>()
    private var marker: Marker? = null
    private var isViewing: Boolean = false


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

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Get the LatLng of the selected place
                reportLocation = place.latLng

                if (reportLocation != null) {
                    marker?.remove()
                    marker = mMap.addMarker(MarkerOptions().position(reportLocation!!).title("Location"))
                }

                // Move the camera to the selected place
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(reportLocation!!, DEFAULT_ZOOM)
                mMap.animateCamera(cameraUpdate)
            }

            override fun onError(status: Status) {
                // Handle the error
                Log.i(TAG, "An error occurred: $status")
                Toast.makeText(
                    this@ReportLocationMap,
                    "Error: ${status.statusMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        val repository = Repository()
        val viewModelFactory = ReportLocationMapViewModelFactory(repository)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(ReportLocationMapViewModel::class.java)

        viewModel.getAllLocationsWithScoreResponse.observe(this) { response ->
            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.data != null) {
                    val data: List<Location> = Gson().fromJson(
                        response.body()?.data.toString(),
                        object : TypeToken<List<Location>>() {}.type
                    )
                    data.filter { it.averageScore >= 1.0 }.forEach {
                        val color = 90 - ((it.averageScore - 1) * 30)
                        circles.add(
                            mMap.addCircle(
                                CircleOptions()
                                    .center(LatLng(it.latitude.toDouble(), it.longitude.toDouble()))
                                    .radius(70.0)
                                    .fillColor(
                                        Color.HSVToColor(
                                            80,
                                            floatArrayOf(color.toFloat(), 1.0f, 1.0f)
                                        )
                                    )
                                    .strokeColor(Color.TRANSPARENT)
                                    .clickable(false)
                            )
                        )

                    }
                }
            }
        }


        val checkBox1 = findViewById<CheckBox>(R.id.checkBox1)
        val checkBox2 = findViewById<CheckBox>(R.id.checkBox2)
        setCurrentLocation = findViewById(R.id.my_location_button)
        resetButton = findViewById<Button>(R.id.reset_button)
        confirm = findViewById<Button>(R.id.confirm_button)

        checkBox1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setCurrentLocation.visibility = View.VISIBLE
                resetButton.visibility = View.VISIBLE
                confirm.visibility = View.VISIBLE
                checkBox2.isChecked = false
                circles.forEach { it.isClickable = false }
                isViewing = false
            } else {
                setCurrentLocation.visibility = View.GONE
                resetButton.visibility = View.GONE
                confirm.visibility = View.GONE
                checkBox2.isChecked = true
            }
        }

        checkBox2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setCurrentLocation.visibility = View.GONE
                resetButton.visibility = View.GONE
                confirm.visibility = View.GONE
                checkBox1.isChecked = false
                circles.forEach { it.isClickable = true }
                isViewing = true
            } else {
                setCurrentLocation.visibility = View.VISIBLE
                resetButton.visibility = View.VISIBLE
                confirm.visibility = View.VISIBLE
                checkBox1.isChecked = true
            }
        }

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
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    marker?.remove()
                    reportLocation = LatLng(location.latitude, location.longitude)
                    marker = mMap.addMarker(MarkerOptions().position(reportLocation!!))

                    // Move the camera to the user's current location
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(reportLocation!!, 18f)
                    mMap.animateCamera(cameraUpdate)
                }
            }
        }


        confirm.setOnClickListener {
            if (reportLocation != null) {
                marker?.remove()
                marker = mMap.addMarker(
                    MarkerOptions().position(reportLocation!!).title("Reported Location")
                )
                val intent = Intent(this, AddReportActivity::class.java)
                intent.putExtra("location", reportLocation)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a location to report", Toast.LENGTH_SHORT)
                    .show()
            }

        }

        resetButton.setOnClickListener {
            marker?.remove()
            reportLocation = null
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCircleClickListener(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not already granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Get the user's last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Move the camera to the user's current location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                marker = mMap.addMarker(MarkerOptions().position(currentLatLng).title("Location"))
                reportLocation = currentLatLng
            }
        }

        mMap.setOnMapClickListener { latLng ->
            if (isViewing)
                return@setOnMapClickListener
            if (reportLocation == null) {
                reportLocation = latLng
                marker = mMap.addMarker(MarkerOptions().position(latLng).title("Location"))
            } else {
                marker?.remove()
                reportLocation = latLng
                marker = mMap.addMarker(MarkerOptions().position(latLng).title("Location"))
            }
        }
        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val userId = localDB.getInt("userId", -1)
        viewModel.getAllLocationsWithScore("Bearer $token", userId)
    }

    override fun onCircleClick(circle: Circle) {
        val intent = Intent(this, ViewReports::class.java)
        intent.putExtra("longitude", circle.center.longitude.toString())
        intent.putExtra("latitude", circle.center.latitude.toString())
        startActivity(intent)
    }
}