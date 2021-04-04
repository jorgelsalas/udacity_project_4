package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.R.id.*
import com.udacity.project4.R.raw.map_style
import com.udacity.project4.R.string.unable_to_change_map_type
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val REQUEST_LOCATION_PERMISSION = 1

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var map : GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//      TODO: add the map setup implementation
        requestMap()
//        TODO: zoom to the user location after taking his permission

//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    private fun requestMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

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

    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        if (isLocationPermissionGranted()) {
            map?.isMyLocationEnabled = true
            zoomToCurrentLocation()
        }
        else {
            requestLocationPermission()
        }
    }

    private fun zoomToCurrentLocation() {
        // TODO
    }

    private fun isLocationPermissionGranted() : Boolean {
        return checkSelfPermission(context!!, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PERMISSION_GRANTED)) {
                enableLocation()
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
            Toast.makeText(context, getString(unable_to_change_map_type), Toast.LENGTH_SHORT).show()
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
}
