package com.example.safemvvm.views

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.Location
import com.example.safemvvm.models.Trip
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.CreateTripViewModel
import com.example.safemvvm.viewmodels.CreateTripViewModelFactory
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class CreateTripActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var viewModel: CreateTripViewModel
    private val TAG = "com.example.safemvvm.views.CreateTripActivity"
    private lateinit var mMap: GoogleMap
    private var source: LatLng? = null
    private var destination: LatLng? = null
    private var timeInSeconds = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val AUTOCOMPLETE_REQUEST_CODE = 2
    private val DEFAULT_ZOOM = 15f

    // Define the mode, departure_time, and traffic_model parameters
    private var mode = "driving"
    private val departureTime = System.currentTimeMillis() / 1000
    private val trafficModel = "best_guess"
    private val polylines = mutableListOf<Polyline>()
    private val markers = mutableListOf<Marker>()

    private fun clear() {
        clearPolylines()
        clearMarkers()
    }

    private fun clearPolylines() {
        for (polyline in polylines) {
            polyline.remove()
        }
        polylines.clear()
    }

    private fun clearMarkers() {
        for (marker in markers) {
            marker.remove()
        }
        markers.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trip)

        // Initialize the Places SDK
        Places.initialize(applicationContext, "AIzaSyBCJXuU6WB-ARQgtiDBl7KRhZsSrvwdYvk")

        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // Start the AutocompleteActivity
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY, fields
        )
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
                destination = place.latLng

                if (destination != null && source != null) {
                    clear()
                    calculateDistance()
                    mMap.addMarker(MarkerOptions().position(source!!))?.let { markers.add(it) }
                    mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
                        ?.let { markers.add(it) }

                }

                // Move the camera to the selected place
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(destination!!, DEFAULT_ZOOM)
                mMap.animateCamera(cameraUpdate)

                // Calculate and display the distance between the source and destination
                calculateDistance()
            }

            override fun onError(status: Status) {
                // Handle the error
                Log.i(TAG, "An error occurred: $status")
                Toast.makeText(
                    this@CreateTripActivity,
                    "Error: ${status.statusMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        //works ok
        val currentLocation = findViewById<ImageView>(R.id.my_location_button)
        currentLocation.setOnClickListener {
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
                    source = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(source!!))
                        ?.let { it1 -> markers.add(it1) }
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(source!!, 18f)
                    mMap.animateCamera(cameraUpdate)
                }
            }
        }

        val resetButton = findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener {
            clear()
            destination = null
            val timeTextView = findViewById<TextView>(R.id.timeTextView)
            timeTextView.text = "Time: "
            timeInSeconds = 0
            mMap.addMarker(MarkerOptions().position(source!!))
                ?.let { it1 -> markers.add(it1) }
        }

        val walking = findViewById<Button>(R.id.walk_button)
        val driving = findViewById<Button>(R.id.driving_button)
        walking.setOnClickListener {
            mode = "walking"
            driving.setBackgroundColor(Color.parseColor("#E242CF42"))
            driving.setTextColor(Color.parseColor("#5D4037"))
            walking.setBackgroundColor(Color.parseColor("#076107"))
            walking.setTextColor(Color.WHITE)
            if (destination != null && source != null) {
                clear()
                calculateDistance()
                mMap.addMarker(MarkerOptions().position(source!!))
                    ?.let { it1 -> markers.add(it1) }
                mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
                    ?.let { markers.add(it) }
            }
        }

        driving.setOnClickListener {
            mode = "driving"
            walking.setBackgroundColor(Color.parseColor("#E242CF42"))
            walking.setTextColor(Color.parseColor("#5D4037"))
            driving.setBackgroundColor(Color.parseColor("#076107"))
            driving.setTextColor(Color.WHITE)
            if (destination != null && source != null) {
                clear()
                calculateDistance()
                mMap.addMarker(MarkerOptions().position(source!!))
                    ?.let { it1 -> markers.add(it1) }
                mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
                    ?.let { markers.add(it) }
            }
        }

        val repository = Repository()
        val viewModelFactory = CreateTripViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[CreateTripViewModel::class.java]

        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val userId = localDB.getInt("userId", -1)

        val confirm = findViewById<Button>(R.id.confirm_button)
        confirm.setOnClickListener {
            if (destination != null && source != null) {
                clear()
                calculateDistance()
                mMap.addMarker(MarkerOptions().position(source!!))
                    ?.let { it1 -> markers.add(it1) }
                mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
                    ?.let { markers.add(it) }
                viewModel.addTrip(
                    "Bearer $token",
                    Trip(
                        userId,
                        timeInSeconds,
                        source!!.longitude,
                        source!!.latitude,
                        destination!!.longitude,
                        destination!!.latitude
                    )
                )
            } else {
                Toast.makeText(this, "Please select a destination", Toast.LENGTH_SHORT).show()
            }
        }
        observeResponses()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //mMap.setOnCircleClickListener(this)

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
        }

        // Get the user's last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Move the camera to the user's current location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                    ?.let { markers.add(it) }
                source = currentLatLng
            }
        }

        mMap.setOnMapClickListener { latLng ->
            if (source == null) {
                Toast.makeText(this, "Please set your current location first", Toast.LENGTH_SHORT)
                    .show()
            } else if (destination == null) {
                destination = latLng
                mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
                    ?.let { markers.add(it) }

                // Calculate the distance between the source and destination using the Directions API
                calculateDistance()
            } else {
                clear()
                destination = latLng
                mMap.addMarker(MarkerOptions().position(source!!))
                    ?.let { it1 -> markers.add(it1) }
                mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
                    ?.let { markers.add(it) }

                // Calculate the distance between the source and destination using the Directions API
                calculateDistance()
            }
        }
        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token", null)
        val userId = localDB.getInt("userId", -1)
        viewModel.getAllLocationsWithScore("Bearer $token", userId)
    }


    private fun calculateDistance() {
        // Define the API endpoint and API key
        val endpoint = "https://maps.googleapis.com/maps/api/directions/json"
        val apiKey = "AIzaSyBCJXuU6WB-ARQgtiDBl7KRhZsSrvwdYvk"

        // Build the Directions API request URL
        val url =
            "$endpoint?origin=${source!!.latitude},${source!!.longitude}&destination=${destination!!.latitude},${destination!!.longitude}&mode=$mode&departure_time=$departureTime&traffic_model=$trafficModel&key=$apiKey"

        // Send the Directions API request using OkHttp
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        CoroutineScope(Dispatchers.IO).launch {
            val response = client.newCall(request).execute()

            // Parse the Directions API response to extract the route polyline
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            val routes = jsonObject.getJSONArray("routes")
            for (i in 0 until routes.length()) {
                val legs = routes.getJSONObject(i).getJSONArray("legs")
                for (j in 0 until legs.length()) {
                    val steps = legs.getJSONObject(j).getJSONArray("steps")
                    for (k in 0 until steps.length()) {
                        val polyline =
                            steps.getJSONObject(k).getJSONObject("polyline").getString("points")
                        val decodedPolyline = PolylineDecoder.decode(polyline)

                        // Display the distance and time values in the UI
                        runOnUiThread {
                            val distance =
                                legs.getJSONObject(j).getJSONObject("distance").getString("text")
                            val time =
                                legs.getJSONObject(j).getJSONObject("duration").getString("text")
                            timeInSeconds =
                                legs.getJSONObject(j).getJSONObject("duration").getInt("value")
                            val timeTextView = findViewById<TextView>(R.id.timeTextView)
                            timeTextView.text = "Time: $time"
                            Log.d("CreateTripActivity", "calculateTime: $timeInSeconds")

                            // Draw a polyline that resembles the route between the source and destination

                            val polylineOptions = PolylineOptions()
                                .addAll(decodedPolyline)
                                .width(5f)
                                .color(Color.parseColor("#076107"))
                            polylines.add(mMap.addPolyline(polylineOptions))
                        }
                    }
                }
            }
        }
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.getAllLocationsWithScoreResponse,
            Array<Location>::class.java,
            {
                it.filter { it.averageScore >= 1.0 }.forEach {
                    val color = 90 - ((it.averageScore - 1) * 30)
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
                    )
                }
            },
            {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        )
        ResponseHandler(this).observeResponse(
            viewModel.addTripResponse,
            TripResponse::class.java,
            {
                Toast.makeText(this, "Your trip has been confirmed", Toast.LENGTH_LONG).show()
                LocalDatabaseManager(this).tripId(it.id)
                Navigator(this).to(WhileInTrip::class.java)
                    .andPutExtraInt("time", it.remainingTime.toInt()).andClearStack()
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )
    }
}

object PolylineDecoder {
    fun decode(polyline: String): List<LatLng> {
        val len = polyline.length
        var index = 0
        val array = ArrayList<LatLng>(len / 2)
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = polyline[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = polyline[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            array.add(LatLng(lat * 1e-5, lng * 1e-5))
        }
        return array
    }
}