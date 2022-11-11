package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.widget.Toast
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

fun requestEnableLocation(context: Context, successfulCallback: () -> Unit) {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).apply {
        setAlwaysShow(true)
    }
    val client = LocationServices.getSettingsClient(context)
    val task = client.checkLocationSettings(builder.build())
    task.addOnSuccessListener {
        successfulCallback()
    }
    task.addOnFailureListener {
        Toast.makeText(context, "Cannot Create Geofence. Location must be enabled!", Toast.LENGTH_LONG).show()
    }
}