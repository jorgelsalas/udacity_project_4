package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.udacity.project4.R
import com.udacity.project4.R.id.*
import com.udacity.project4.R.raw.map_style
import com.udacity.project4.R.string.location_required_error
import com.udacity.project4.R.string.unable_to_change_map_type
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.lang.Exception
import java.util.*

private const val REQUEST_LOCATION_PERMISSION = 1
private const val REQUEST_BACKGROUND_LOCATION_PERMISSION = 2
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 3

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var map : GoogleMap? = null
    private val runningQOrLater = SDK_INT >= Q

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        requestMap()
//        TODO: zoom to the user location after taking his permission


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    private fun requestMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("NewApi")
    override fun onMapReady(map: GoogleMap?) {
        this.map = map

        setMapStyle()
        setMapLongClick(map!!)
        enableLocation()
    }

    private fun setMapStyle() {
        try {
            val mapStyle = MapStyleOptions.loadRawResourceStyle(context, map_style)
            val success = map?.setMapStyle(mapStyle) ?: false

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        }
        catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.addMarker(getMarkerOptions(latLng))
        }
    }

    private fun getMarkerOptions(latLng: LatLng) : MarkerOptions {
        return MarkerOptions()
                .position(latLng)
                .title(getString(R.string.dropped_pin))
                .snippet(getSnippet(latLng))
                .icon(defaultMarker(HUE_RED))
    }

    private fun getSnippet(latLng: LatLng) : String {
        return String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude, latLng.longitude)
    }

    @RequiresApi(Q)
    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        if (isLocationPermissionGranted()) {
            map?.isMyLocationEnabled = true
            if (runningQOrLater) {
                enableBackgroundLocation()
            }
            else {
                // TODO:
            }
        }
        else {
            requestLocationPermission()
        }
    }

    @RequiresApi(Q)
    private fun enableBackgroundLocation() {
        if (isBackgroundLocationPermissionGranted()) {
            Toast.makeText(context, "BG Loc enabled!", LENGTH_SHORT).show()
            verifyUserHasLocationEnabled()
        }
        else {
            requestBackgroundLocationPermission()
        }
    }

    private fun verifyUserHasLocationEnabled(resolve:Boolean = true) {

        val locationSettingsResponseTask = getLocationSettingsResponseTask()

        locationSettingsResponseTask.addOnFailureListener { exception ->
            onLocationSettingsResponseError(exception, resolve)
        }

        locationSettingsResponseTask.addOnCompleteListener(this::onLocationSettingsResponseSuccess)
    }

    private fun getLocationSettingsResponseTask() : Task<LocationSettingsResponse> {
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest())

        val settingsClient = LocationServices.getSettingsClient(activity!!)
        return settingsClient.checkLocationSettings(builder.build())
    }

    private fun getLocationRequest() : LocationRequest {
        return LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
    }

    private fun onLocationSettingsResponseError(exception: Exception, resolve:Boolean = true) {
        if (exception is ResolvableApiException && resolve){
            try {
                toast("Trying to resolve!")
                exception.startResolutionForResult(activity, REQUEST_TURN_DEVICE_LOCATION_ON)
            }
            catch (sendEx: IntentSender.SendIntentException) {
                toast("Exception while trying to resolve")
                Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
            }
        }
        else {
            Snackbar.make(binding.root, location_required_error, LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        toast("Snackback action!!!")
                        verifyUserHasLocationEnabled()
                    }
                    .show()
        }
    }

    private fun onLocationSettingsResponseSuccess(task : Task<LocationSettingsResponse>) {
        if ( task.isSuccessful ) {
            toast("Location is on!")
            zoomToCurrentLocation()
        }
        else {
            toast("task failed")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        toast("onActivityResult")
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            toast("Request matches! Retrying user location verification!")
            verifyUserHasLocationEnabled(false)
        }
    }

    private fun zoomToCurrentLocation() {
        // TODO
    }

    private fun isLocationPermissionGranted() : Boolean {
        return checkSelfPermission(context!!, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }

    @RequiresApi(Q)
    private fun isBackgroundLocationPermissionGranted() : Boolean {
        return if (runningQOrLater) {
            PERMISSION_GRANTED == checkSelfPermission(context!!, ACCESS_BACKGROUND_LOCATION)
        }
        else {
            true
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
    }

    @RequiresApi(Q)
    private fun requestBackgroundLocationPermission() {
        requestPermissions(arrayOf(ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION_PERMISSION)
    }

    @RequiresApi(Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PERMISSION_GRANTED)) {
                enableLocation()
            }
        }
        else if (requestCode == REQUEST_BACKGROUND_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PERMISSION_GRANTED)) {
                enableBackgroundLocation()
            }
        }
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when (item.itemId) {
            normal_map, hybrid_map, satellite_map, terrain_map -> {
                updateMapType(item.itemId)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateMapType(itemId: Int) {
        if (map != null) {
            map?.mapType = getMapType(itemId)
        }
        else {
            Toast.makeText(context, getString(unable_to_change_map_type), LENGTH_SHORT).show()
        }
    }

    private fun getMapType(@IdRes menuOption: Int) : Int {
        return when(menuOption) {
            normal_map -> MAP_TYPE_NORMAL
            hybrid_map -> MAP_TYPE_HYBRID
            satellite_map -> MAP_TYPE_SATELLITE
            terrain_map -> MAP_TYPE_TERRAIN
            else -> MAP_TYPE_NORMAL
        }
    }

    companion object {
        private val TAG = SelectLocationFragment::class.java.simpleName
    }

    private fun toast(message: String) {
        Toast.makeText(activity, message, LENGTH_SHORT).show()
    }
}
