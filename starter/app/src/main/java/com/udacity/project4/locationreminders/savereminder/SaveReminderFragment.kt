package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

private const val TAG = "SaveReminderFragment"

private const val LOCATION_PERMISSION_REQUEST_CODE = 101
private const val LOCATION_PERMISSION_REQUEST_CODE_PRE_BACKGROUND = 102

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {


//            Done: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            checkLocationAndSaveGeofence()
        }
    }

    private fun checkLocationAndSaveGeofence() {
        _viewModel.apply {
            val reminderDI = ReminderDataItem(
                title = reminderTitle.value,
                description = reminderDescription.value,
                location = reminderSelectedLocationStr.value,
                latitude = latitude.value,
                longitude = longitude.value
            )

            // Done: Check Location permissions and that location is enabled
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    createGeofenceRequest(reminderDI)
                } else {
                    requestEnableLocation(requireContext()) {
                        createGeofenceRequest(reminderDI)
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= 29) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ), LOCATION_PERMISSION_REQUEST_CODE_PRE_BACKGROUND
                    )
                } else {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ), LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }



    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun checkPermissions(): Boolean {
        val finePermission = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarsePermission = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val backgroundPermissionInfo = if (Build.VERSION.SDK_INT >= 29) ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) else PackageManager.PERMISSION_GRANTED
        return (finePermission == PackageManager.PERMISSION_GRANTED && coarsePermission == PackageManager.PERMISSION_GRANTED && backgroundPermissionInfo == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_PRE_BACKGROUND) {
            var successful = false
            if (grantResults.isNotEmpty()) successful = true
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) successful = false
            }
            if (successful) {
                if (Build.VERSION.SDK_INT >= 29)
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ), LOCATION_PERMISSION_REQUEST_CODE
                    )
            } else {
                _viewModel.showErrorMessage.value =
                    getString(R.string.permission_denied_explanation_geofence)
                findNavController().navigateUp()
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            var successful = false
            if (grantResults.isNotEmpty()) successful = true
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) successful = false
            }
            if (successful) {
                checkLocationAndSaveGeofence()
            } else {
                _viewModel.showErrorMessage.value =
                    getString(R.string.permission_denied_explanation_geofence)
                findNavController().navigateUp()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private fun createGeofenceRequest(reminderDataItem: ReminderDataItem) {

        if (_viewModel.validateAndSaveReminder(reminderDataItem)) {
            if (_viewModel.latitude.value != null && _viewModel.longitude.value != null) {
                val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
                val geofencePendingIntent: PendingIntent by lazy {
                    intent.action = ACTION_GEOFENCE_EVENT
                    PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                val geofence = Geofence.Builder()
                    .setRequestId(reminderDataItem.id)
                    .setCircularRegion(
                        reminderDataItem.latitude ?: 0.0,
                        reminderDataItem.longitude ?: 0.0,
                        GEOFENCE_RADIUS_M
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "Cannot register geofence as location permissions have not been granted")
                    return
                }
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Log.i(TAG, getString(R.string.geofence_added))
                    }
                    addOnFailureListener {
                        Log.e(TAG, "Failed to add Geofence! :( ${it.message}")
                    }
                }
            } else {
                Log.e(TAG, "Lat/ Lon missing!")
            }
        } else {
            Log.d(TAG, "Validation not passed.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
