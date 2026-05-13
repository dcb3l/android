package com.example.lr6v

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // UI Елементи
    private lateinit var etCity: EditText
    private lateinit var btnGetWeather: Button
    private lateinit var btnEnableAlerts: Button
    private lateinit var btnDisableAlerts: Button
    private lateinit var btnTestNotification: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var tvLocation: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvUpdated: TextView
    private lateinit var tvAlertStatus: TextView

    // Логіка та дані
    private val repository = WeatherRepository()
    private val executor = Executors.newSingleThreadExecutor()
    private var lastCity: String = ""
    private var lastWeather: WeatherData? = null
    private var alertsEnabled: Boolean = false

    companion object {
        private const val PREFS_NAME = "weather_alert_prefs"
        private const val KEY_LAST_CITY = "key_last_city"
        private const val KEY_ALERTS_ENABLED = "key_alerts_enabled"
        private const val UNIQUE_WORK_NAME = "weather_alert_periodic_work"

        private const val STATE_LAST_CITY = "state_last_city"
        private const val STATE_ALERTS_ENABLED = "state_alerts_enabled"
        private const val STATE_LOCATION = "state_location"
        private const val STATE_TEMPERATURE = "state_temperature"
        private const val STATE_WIND = "state_wind"
        private const val STATE_CONDITION = "state_condition"
        private const val STATE_UPDATED = "state_updated"
        private const val STATE_ALERT_STATUS = "state_alert_status"
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
                NotificationHelper.showWeatherNotification(
                    context = this,
                    title = "Weather Alert",
                    message = "Test notification: notifications are working correctly."
                )
            } else {
                Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        NotificationHelper.createChannel(this)
        restoreState(savedInstanceState)
        setupListeners()
    }

    private fun initViews() {
        etCity = findViewById(R.id.etCity)
        btnGetWeather = findViewById(R.id.btnGetWeather)
        btnEnableAlerts = findViewById(R.id.btnEnableAlerts)
        btnDisableAlerts = findViewById(R.id.btnDisableAlerts)
        btnTestNotification = findViewById(R.id.btnTestNotification)
        progressBar = findViewById(R.id.progressBar)

        tvLocation = findViewById(R.id.tvLocation)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvWind = findViewById(R.id.tvWind)
        tvCondition = findViewById(R.id.tvCondition)
        tvUpdated = findViewById(R.id.tvUpdated)
        tvAlertStatus = findViewById(R.id.tvAlertStatus)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        lastCity = savedInstanceState?.getString(STATE_LAST_CITY)
            ?: prefs.getString(KEY_LAST_CITY, "") ?: ""

        alertsEnabled = savedInstanceState?.getBoolean(STATE_ALERTS_ENABLED)
            ?: prefs.getBoolean(KEY_ALERTS_ENABLED, false)

        etCity.setText(lastCity)

        tvLocation.text = savedInstanceState?.getString(STATE_LOCATION) ?: "Location: —"
        tvTemperature.text = savedInstanceState?.getString(STATE_TEMPERATURE) ?: "Temperature: —"
        tvWind.text = savedInstanceState?.getString(STATE_WIND) ?: "Wind: —"
        tvCondition.text = savedInstanceState?.getString(STATE_CONDITION) ?: "Condition: —"
        tvUpdated.text = savedInstanceState?.getString(STATE_UPDATED) ?: "Updated: —"
        tvAlertStatus.text = savedInstanceState?.getString(STATE_ALERT_STATUS)
            ?: if (alertsEnabled) "Alerts: enabled" else "Alerts: disabled"
    }

    private fun setupListeners() {
        btnGetWeather.setOnClickListener { fetchWeather() }
        btnEnableAlerts.setOnClickListener { enableAlerts() }
        btnDisableAlerts.setOnClickListener { disableAlerts() }
        btnTestNotification.setOnClickListener { sendTestNotification() }
    }

    private fun fetchWeather() {
        val city = etCity.text.toString().trim()
        if (city.isEmpty()) {
            Toast.makeText(this, "Enter a city name", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        executor.execute {
            val result = repository.getWeatherByCity(city)

            runOnUiThread {
                showLoading(false)
                if (result == null) {
                    Toast.makeText(this, "Failed to load weather data", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                lastCity = city
                lastWeather = result
                savePrefs()
                updateWeatherUi(result)

                if (alertsEnabled) {
                    handlePossibleAlert(result, city)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeatherUi(weather: WeatherData) {
        tvLocation.text = "Location: ${weather.cityName}, ${weather.country}"
        tvTemperature.text = "Temperature: ${"%.1f".format(weather.temperature)} °C"
        tvWind.text = "Wind: ${"%.1f".format(weather.windSpeed)} km/h"
        tvCondition.text = "Condition: ${WeatherCodeMapper.toText(weather.weatherCode)}"
        tvUpdated.text = "Updated: ${weather.time}"
    }

    private fun enableAlerts() {
        val city = etCity.text.toString().trim()
        if (city.isEmpty()) {
            Toast.makeText(this, "Enter a city first", Toast.LENGTH_SHORT).show()
            return
        }

        requestNotificationPermissionIfNeeded()

        alertsEnabled = true
        lastCity = city
        savePrefs()
        tvAlertStatus.text = "Alerts: enabled"

        val workRequest = PeriodicWorkRequestBuilder<WeatherAlertWorker>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf(WeatherAlertWorker.KEY_CITY to city))
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Toast.makeText(this, "Weather alerts enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableAlerts() {
        alertsEnabled = false
        savePrefs()
        tvAlertStatus.text = "Alerts: disabled"
        WorkManager.getInstance(this).cancelUniqueWork(UNIQUE_WORK_NAME)
        Toast.makeText(this, "Weather alerts disabled", Toast.LENGTH_SHORT).show()
    }

    private fun sendTestNotification() {
        if (!canPostNotifications()) {
            requestNotificationPermissionIfNeeded()
            return
        }
        NotificationHelper.showWeatherNotification(
            context = this,
            title = "Weather Alert",
            message = "Test notification: the notification system is working."
        )
    }

    private fun handlePossibleAlert(weather: WeatherData, city: String) {
        val alertMessage = buildAlertMessage(weather)
        if (alertMessage != null && canPostNotifications()) {
            NotificationHelper.showWeatherNotification(
                context = this,
                title = "Weather Alert: $city",
                message = alertMessage
            )
        }
    }

    private fun buildAlertMessage(weather: WeatherData): String? {
        return when {
            weather.temperature <= 0 -> "Frost warning. Temp: ${"%.1f".format(weather.temperature)}°C."
            weather.temperature >= 30 -> "Heat warning. Temp: ${"%.1f".format(weather.temperature)}°C."
            weather.windSpeed >= 50 -> "Strong wind. Speed: ${"%.1f".format(weather.windSpeed)} km/h."
            WeatherCodeMapper.isRainOrSnow(weather.weatherCode) -> "Precipitation alert: ${WeatherCodeMapper.toText(weather.weatherCode)}."
            else -> null
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun canPostNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnGetWeather.isEnabled = !isLoading
    }

    private fun savePrefs() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_CITY, lastCity)
            .putBoolean(KEY_ALERTS_ENABLED, alertsEnabled)
            .apply()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_LAST_CITY, etCity.text.toString())
        outState.putBoolean(STATE_ALERTS_ENABLED, alertsEnabled)
        outState.putString(STATE_LOCATION, tvLocation.text.toString())
        outState.putString(STATE_TEMPERATURE, tvTemperature.text.toString())
        outState.putString(STATE_WIND, tvWind.text.toString())
        outState.putString(STATE_CONDITION, tvCondition.text.toString())
        outState.putString(STATE_UPDATED, tvUpdated.text.toString())
        outState.putString(STATE_ALERT_STATUS, tvAlertStatus.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}