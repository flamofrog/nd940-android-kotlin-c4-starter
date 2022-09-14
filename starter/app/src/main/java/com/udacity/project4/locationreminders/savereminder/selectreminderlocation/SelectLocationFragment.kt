package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Criteria
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


private const val TAG = "SelectLocationFragment"
private const val DEFAULT_ZOOM_LEVEL = 16F
private const val LOCATION_PERMISSION_REQUEST_CODE = 101

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "On CreateView")
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        Done: add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { gMap ->
            googleMap = gMap

//        Done: zoom to the user location after taking his permission
            gMap.checkPermissionsAndSetupZoom()


//        Done: add style to the map
            gMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )

//        Done: put a marker to location that the user selected
            gMap.setOnMapClickListener { latLng ->
                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .title(getString(R.string.dropped_pin))
                        .icon(requireContext().getBitmapDescriptor(R.drawable.ic_location))
                        .position(latLng)
                )
                val geocoder = Geocoder(context)
                _viewModel.reminderSelectedLocationStr.value =
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).getOrNull(0)
                        ?.let {
                            "${it.subThoroughfare} ${it.thoroughfare}"
                        } ?: "Unknown"
                _viewModel.selectedPOI.value = PointOfInterest(latLng, "", "")
                _viewModel.latitude.value = latLng.latitude
                _viewModel.longitude.value = latLng.longitude
            }
        }

//        Done: call this function after the user confirms on the selected location
        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun Context.getBitmapDescriptor(@DrawableRes vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId)
        val colourWhite = ContextCompat.getColor(this, R.color.white)
        vectorDrawable!!.apply {
            setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) setTint(colourWhite)
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    private fun GoogleMap.checkPermissionsAndSetupZoom() {
        if (!(ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            isMyLocationEnabled = true
            val locationManager =
                requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getBestProvider(Criteria(), true)
                ?.let { locationManager.getLastKnownLocation(it) }
            location?.let {
                moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), DEFAULT_ZOOM_LEVEL
                    )
                )
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            var successful = false
            if (grantResults.isNotEmpty()) successful = true
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) successful = false
            }
            if (successful) googleMap.checkPermissionsAndSetupZoom()
            else _viewModel.showErrorMessage.value =
                getString(R.string.permission_denied_explanation)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}
