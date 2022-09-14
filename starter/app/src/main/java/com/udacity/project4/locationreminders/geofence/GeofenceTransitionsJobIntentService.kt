package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

private const val TAG = "GeofenceTransitionsJobI"
class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //        TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //Done: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //Done: call @sendNotification
        Log.i(TAG, "onHandleWork")
        sendNotification(GeofencingEvent.fromIntent(intent).triggeringGeofences)
    }

    //TODO: get the request id of the current geofence
    @SuppressLint("VisibleForTests")
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        Log.i(TAG, "sendNotification (size: ${triggeringGeofences.size}")

        triggeringGeofences.forEach { triggeredGeofence ->
            Log.i(TAG, "triggered geofence: ${triggeredGeofence.requestId}")

            val requestId = triggeredGeofence.requestId
            //Get the local repository instance
            val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                Log.i(TAG, "got Result: $result")

                if (result is Result.Success<ReminderDTO>) {
                    Log.i(TAG, "result is success!")
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }
    }

}
