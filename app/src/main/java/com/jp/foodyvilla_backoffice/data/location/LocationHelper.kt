package com.jp.foodyvilla_backoffice.data.location

import android.app.Activity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class LocationHelper(private val activity: Activity) {

    private val settingsClient = LocationServices.getSettingsClient(activity)

    fun checkAndEnableLocation(
        onEnabled: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true) // 🔥 force dialog

        val task = settingsClient.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onEnabled()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // 🔥 This shows the "Turn on GPS" dialog
                    exception.startResolutionForResult(activity, 1001)
                } catch (sendEx: Exception) {
                    onFailure(sendEx)
                }
            } else {
                onFailure(exception)
            }
        }
    }
}