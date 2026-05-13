package com.example.lr6v

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class WeatherRepository {

    fun getWeatherByCity(city: String): WeatherData? {
        val location = getCoordinates(city) ?: return null
        val weather = getCurrentWeather(location.latitude, location.longitude) ?: return null

        return WeatherData(
            cityName = location.name,
            country = location.country,
            temperature = weather.temperature,
            windSpeed = weather.windSpeed,
            weatherCode = weather.weatherCode,
            time = weather.time
        )
    }

    private fun getCoordinates(city: String): LocationData? {
        return try {
            val encodedCity = URLEncoder.encode(city, "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=en&format=json"
            val responseText = getJsonString(url) ?: return null

            val response = JSONObject(responseText)
            val results = response.optJSONArray("results") ?: return null
            if (results.length() == 0) return null

            val item = results.getJSONObject(0)
            LocationData(
                name = item.optString("name", city),
                country = item.optString("country", ""),
                latitude = item.optDouble("latitude", 0.0),
                longitude = item.optDouble("longitude", 0.0)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData? {
        val url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude" +
                "&longitude=$longitude" +
                "&current=temperature_2m,wind_speed_10m,weather_code" +
                "&timezone=auto"

        val responseText = getJsonString(url) ?: return null
        val response = JSONObject(responseText)
        val current = response.optJSONObject("current") ?: return null

        return WeatherData(
            cityName = "",
            country = "",
            temperature = current.optDouble("temperature_2m", 0.0),
            windSpeed = current.optDouble("wind_speed_10m", 0.0),
            weatherCode = current.optInt("weather_code", 0),
            time = current.optString("time", "")
        )
    }

    private fun getJsonString(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val result = reader.readText()
                reader.close()
                result
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}