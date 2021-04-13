package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private val TAG = SelectLocationFragment::class.java.simpleName

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = SelectLocationFragment.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private lateinit var geofencingClient: GeofencingClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(binding.root.context)

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
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

            if (_viewModel.validateEnteredData(reminderDataItem)) {
//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
                addGeofence(reminderDataItem.id)
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


    @SuppressLint("MissingPermission")
    private fun addGeofence(id: String) {
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(_viewModel.latitude.value!!,
                _viewModel.longitude.value!!,
                100f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()



        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(context, R.string.geofences_added,
                    Toast.LENGTH_SHORT)
                    .show()
                Log.i("Added Geofence", geofence.requestId)
            }
            addOnFailureListener {
                Toast.makeText(context, R.string.geofences_not_added,
                    Toast.LENGTH_SHORT).show()
                if ((it.message != null)) {
                    Log.w(TAG, it.message!!)
                }
            }
        }
    }

    // TODO: Is geofence removal required here?
//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun removeGeofences() {
//        // TODO: In the example they ran this in the onDestroy of the activity: does this make sense?
//        if (!isBackgroundLocationPermissionGranted() && !isLocationPermissionGranted()) {
//            return
//        }
//        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
//            addOnSuccessListener {
//                Log.d(TAG, getString(R.string.geofences_removed))
//                Toast.makeText(context, R.string.geofences_removed, Toast.LENGTH_SHORT)
//                    .show()
//            }
//            addOnFailureListener {
//                Log.d(TAG, getString(R.string.geofences_not_removed))
//            }
//        }
//    }

}
