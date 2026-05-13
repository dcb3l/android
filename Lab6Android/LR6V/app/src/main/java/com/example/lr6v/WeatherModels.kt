package com.example.lr6v

data class LocationData(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

data class WeatherData(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val windSpeed: Double,
    val weatherCode: Int,
    val time: String
)