package com.jp.foodyvilla_backoffice.data.repo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationRepository(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? {

        return try {
            val location = fusedClient.lastLocation.await()

            location?.let {
                Pair(it.latitude, it.longitude)
            }

        } catch (e: Exception) {
            null
        }
    }


    fun hasLocationPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED ||

                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PermissionChecker.PERMISSION_GRANTED
    }

    fun isGpsEnabled(): Boolean {

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    suspend fun fetchLocation(
    ): Result<Pair<Double, Double>> {

        return try {

            val fusedClient =
                LocationServices.getFusedLocationProviderClient(context)

            val result = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()

            if (result != null) {

                Result.success(
                    Pair(result.latitude, result.longitude)
                )

            } else {

                Result.failure(
                    Exception("Location fix unavailable")
                )
            }

        } catch (e: SecurityException) {

            Result.failure(
                Exception("Permission denied")
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    suspend fun getAddressFromLocation(
        latitude: Double,
        longitude: Double
    ): Result<String> {

        return try {

            val geocoder = Geocoder(context, Locale.getDefault())

            val addresses = withContext(Dispatchers.IO) {

                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                )
            }

            if (!addresses.isNullOrEmpty()) {

                val address = addresses[0].getAddressLine(0)

                Result.success(address)

            } else {

                Result.failure(
                    Exception("Address not found")
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}