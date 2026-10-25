package vn.simpletv.launcher

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * "weather" flavor: asks api.open-meteo.com (free, no key, no account) for the current
 * temperature at a fixed city position. Called from a background thread only.
 */
object WeatherSource {
    const val SUPPORTED = true

    fun fetch(lat: Double, lon: Double): WeatherResult? {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weather_code"
            )
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            if (conn.responseCode != 200) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            val temp = Math.round(current.getDouble("temperature_2m")).toInt()
            return WeatherResult(temp, current.getInt("weather_code"))
        } catch (e: Exception) {
            return null
        } finally {
            conn?.disconnect()
        }
    }
}
