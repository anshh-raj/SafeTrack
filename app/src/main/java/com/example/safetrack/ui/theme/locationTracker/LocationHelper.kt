package com.example.safetrack.ui.theme.locationTracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper (private val context: Context){

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private var  locationCallBack: LocationCallback? = null

    fun startLocUpdates(callback: (Location)-> Unit){

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000  // request location every 10 seconds
        )
            .setWaitForAccurateLocation(true)  // Ensures GPS accuracy, not network fallback
            .setMinUpdateIntervalMillis(5000)
            .setMinUpdateDistanceMeters(0f)  // update even on 0 meter movement
            .build()

        locationCallBack = object : LocationCallback(){

            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    Log.d("LocationHelper", "onLocationResult: Location callback fired")
                    callback(it)
                }?: run {
                    Log.d("LocationHelper", "onLocationResult: No Location in result")
                }
            }
        }

        locationCallBack?.let {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    it,
                    Looper.getMainLooper()
                )
            }

        }
    }

    fun stopLocationUpdate(){
        locationCallBack?.let { fusedLocationProviderClient.removeLocationUpdates(it) }
    }
}