package com.example.lr6v
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class WeatherAlertWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    private val repository = WeatherRepository()
    companion object {
        const val KEY_CITY = "key_city"
    }
    override fun doWork(): Result {
        val city = inputData.getString(KEY_CITY) ?: return Result.failure()
        val weather = repository.getWeatherByCity(city) ?: return Result.retry()
        val alertMessage = when {
            weather.temperature <= 0 -> {
                "Frost warning in $city. Temperature is ${"%.1f".format(weather.temperature)} \u00b0C."
            }
            weather.temperature >= 30 -> {
                "Heat warning in $city. Temperature is ${"%.1f".format(weather.temperature)} \u00b0C."
            }
            weather.windSpeed >= 50 -> {
                "Strong wind in $city. Wind speed is ${"%.1f".format(weather.windSpeed)} km/h."
            }
            WeatherCodeMapper.isRainOrSnow(weather.weatherCode) -> {
                "Precipitation alert in $city. ${WeatherCodeMapper.toText(weather.weatherCode)}."
            }
            else -> null
        }
        if (alertMessage != null && canPostNotifications(applicationContext)) {
            NotificationHelper.createChannel(applicationContext)
            NotificationHelper.showWeatherNotification(
                context = applicationContext,
                title = "Weather Alert: $city",
                message = alertMessage
            )
        }
        return Result.success()
    }
    private fun canPostNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}