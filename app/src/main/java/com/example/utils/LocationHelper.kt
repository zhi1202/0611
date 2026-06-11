package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationHelper {

    /**
     * 以非同步掛起 (Suspend) 的方式安全獲取使用者當前的 GPS 位置。
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // 1. 先嘗試取得最後記錄的位置，這通常是最省電且最快速的
        val lastLocation = suspendCancellableCoroutine<Location?> { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }

        if (lastLocation != null) {
            return lastLocation
        }

        // 2. 若最後位置為空，則主動請求一次即時位置更新
        return suspendCancellableCoroutine { continuation ->
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateIntervalMillis(2000L)
                .setMaxUpdates(1) // 只更新一次就結束
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    fusedLocationClient.removeLocationUpdates(this)
                    if (continuation.isActive) continuation.resume(location)
                }
            }

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                ).addOnFailureListener {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    if (continuation.isActive) continuation.resume(null)
                }
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(null)
            }

            // 當協程被取消時，記得移除監聽器，避免記憶體洩漏
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}
