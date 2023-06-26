package com.example.safemvvm.views
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.safemvvm.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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

    private val TAG = "com.example.safemvvm.views.CreateTripActivity"
    private lateinit var mMap: GoogleMap
    private var source: LatLng? = null
    private var destination: LatLng? = null
    private var time: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val AUTOCOMPLETE_REQUEST_CODE = 2
    private val DEFAULT_ZOOM = 15f
    // Define the mode, departure_time, and traffic_model parameters
    var mode = "driving"
    val departureTime = System.currentTimeMillis() / 1000
    val trafficModel = "best_guess"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trip)

        // Initialize the Places SDK
        Places.initialize(applicationContext, "AIzaSyBF-bd0QTq7yWmkjyir04aH8OjtICyFw6c")

        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // Start the AutocompleteActivity
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY, fields
        )
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
                destination = place.latLng

                if(destination != null && source != null) {
                    mMap.clear()
                    calculateDistance()
                    mMap.addMarker(MarkerOptions().position(source!!))
                    mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
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
                Toast.makeText(this@CreateTripActivity, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })



        //works ok
        val button = findViewById<Button>(R.id.CurrLocation)
        button.setOnClickListener {
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
                    source = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(source!!))
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(source!!, 18f)
                    mMap.animateCamera(cameraUpdate)
                }
            }
        }

        val resetButton = findViewById<Button>(R.id.reset_button)
        resetButton.setOnClickListener {
            mMap.clear()
            destination = null
            val timeTextView = findViewById<TextView>(R.id.timeTextView)
            timeTextView.text = "Time: "
            mMap.addMarker(MarkerOptions().position(source!!))
        }

        val walking = findViewById<Button>(R.id.walk_button)
        walking.setOnClickListener {
            mode = "walking"
            val modeText = findViewById<TextView>(R.id.mode)
            modeText.text = "W"
            if(destination != null && source != null) {
                mMap.clear()
                calculateDistance()
                mMap.addMarker(MarkerOptions().position(source!!))
                mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
            }
        }

        val driving = findViewById<Button>(R.id.driving_button)
        driving.setOnClickListener {
            mode = "driving"
            val modeText = findViewById<TextView>(R.id.mode)
            modeText.text = "D"
            if(destination != null && source != null) {
                mMap.clear()
                calculateDistance()
                mMap.addMarker(MarkerOptions().position(source!!))
                mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
            }
        }


        val confirm = findViewById<Button>(R.id.confirm_button)
        confirm.setOnClickListener {
            if(destination != null && source != null) {
                mMap.clear()
                calculateDistance()
                mMap.addMarker(MarkerOptions().position(source!!))
                mMap.addMarker(MarkerOptions().position(destination!!).title("Destination"))
                time = findViewById<TextView>(R.id.timeTextView).text.toString().toDouble()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Your trip has been confirmed", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Please select a destination", Toast.LENGTH_SHORT).show()
            }

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
                source = currentLatLng
            }
        }
        mMap.setOnMapClickListener { latLng ->
            if (source == null) {
                Toast.makeText(this, "Please set your current location first", Toast.LENGTH_SHORT).show()
            } else if (destination == null) {
                destination = latLng
                mMap.addMarker(MarkerOptions().position(latLng).title("Destination"))

                // Calculate the distance between the source and destination using the Directions API
                calculateDistance()
            }
            else{
                mMap.clear()
                destination = latLng
                mMap.addMarker(MarkerOptions().position(source!!))
                mMap.addMarker(MarkerOptions().position(latLng).title("Destination"))

                // Calculate the distance between the source and destination using the Directions API
                calculateDistance()
            }
        }
    }


    private fun calculateDistance() {
        // Define the API endpoint and API key
        val endpoint = "https://maps.googleapis.com/maps/api/directions/json"
        val apiKey = "AIzaSyBF-bd0QTq7yWmkjyir04aH8OjtICyFw6c"

        // Build the Directions API request URL
        val url = "$endpoint?origin=${source!!.latitude},${source!!.longitude}&destination=${destination!!.latitude},${destination!!.longitude}&mode=$mode&departure_time=$departureTime&traffic_model=$trafficModel&key=$apiKey"

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
                        val polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points")
                        val decodedPolyline = PolylineDecoder.decode(polyline)

                        // Display the distance and time values in the UI
                        runOnUiThread {
                            val distance = legs.getJSONObject(j).getJSONObject("distance").getString("text")
                            val time = legs.getJSONObject(j).getJSONObject("duration").getString("text")
                            val timeTextView = findViewById<TextView>(R.id.timeTextView)
                            timeTextView.text = "Time: $time"

                            // Draw a polyline that resembles the route between the source and destination

                            val polylineOptions = PolylineOptions()
                                .addAll(decodedPolyline)
                                .width(5f)
                                .color(Color.BLUE)
                            mMap.addPolyline(polylineOptions)
                        }
                    }
                }
            }
        }
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