package com.example.nodesocialandroid

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.LocationServices
import com.example.nodesocialandroid.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import java.util.*

class UserLocatorMap : AppCompatActivity(), OnMapReadyCallback {

    // TODO: Combine these micro-activities into one Activity class and create a smooth, seamless transition method between user profile, locator map, etc.

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions
    private lateinit var locationReqNEW: LocationRequest.Builder
    private lateinit var locationReqOLD: com.google.android.gms.location.LocationRequest

    private var userLat: Double = 0.0
    private var userLong: Double = 0.0
    var PERMISSION_ID = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        functions = Firebase.functions // initializing our Firebase Functions variable for API calls to locate nearby users

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getUserLocation() {
        mAuth = FirebaseAuth.getInstance() // Fetching our current user info. This is repetitive and can be eliminated when we combine all the user profile
                                           // activities into one Activity class
        val currentUserID: String? = mAuth.currentUser?.uid

        // if we have the permissions, go ahead and get the location and update database
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val userLocation: Location? = task.result
                    if (userLocation != null) { // If the user's location actually exists...
                        userLat = userLocation.latitude
                        userLong = userLocation.longitude

                        updateDatabaseCoords(userLat, userLong, currentUserID)
                    }
                    else { // Get a new location of the user
                        getNewLocation()
                    }

                }
            }
            else {
                // TODO: make notification to tell the user to enable Location Services on their device
            }
        }
        else {
            requestPermissions()
        }
    }

    // Checks to see if the user has granted us the permissions we need
    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ID
        )
    }

    // Checks to see if the user's phone has Location Services enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Debug:", "The permissions are enabled.")
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getNewLocation() {
        //TODO: Implement the extraction and storage of the user's realtime position to Firestore (NO LONGER REALTIME DATABASE)
        /*
        * LocationRequest.Builder() REQUIRES API VERSION >= 31 (S or "Snow Cone")
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationReqNEW = LocationRequest.Builder(LocationRequest.PASSIVE_INTERVAL)
        } else { // If the device's Android API version is < 31, we will use the deprecated method (REVISIT IN CASE OF DISCONTINUATION)
            locationReqOLD = com.google.android.gms.location.LocationRequest()
        }
    }

    // This function will navigate to the child node in the Firebase Realtime Database and update the user's coordinates
    private fun updateDatabaseCoords(userLatitude: Double?, userLongitude: Double?, userUID: String?) {
        // TODO: Update this to send the new coordinates to Firestore instead of Realtime Database
        // This will take in new coords and update the database that we are using with our app to store realtime location information
        val mDatabaseRef = FirebaseDatabase.getInstance().getReference()
        mDatabaseRef.child("user").child(userUID!!)
    }

    // Performs an API call to our JavaScript backend via Firebase Functions
    private fun locateNearbyUsers(): Task<String> {
        return functions
            .getHttpsCallable("locateNearbyUsers")
            .call()
            .continueWith { task ->
                val result = task.result?.data as String // saves the result of the Task as a String and will return that String template-type Task resultant of the function call
                result // The result should be basic, non-threatening information about surrounding users INCLUDING: (username, profile information, proximity, etc.)
            }
    }
}