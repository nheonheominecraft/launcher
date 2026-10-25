package vn.simpletv.launcher

import android.content.Context
import android.content.SharedPreferences

/** Last weather result, kept only on this device. */
data class CachedWeather(val city: Int, val temp: Int, val code: Int, val time: Long)

/**
 * All settings live in one private SharedPreferences file on the device.
 * Nothing here is ever sent anywhere.
 */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)

    /** 0 = normal, 1 = large, 2 = extra large */
    var textScaleIndex: Int
        get() = sp.getInt("text_scale", 0).coerceIn(0, 2)
        set(value) {
            sp.edit().putInt("text_scale", value).apply()
        }

    val textScale: Float
        get() = when (textScaleIndex) {
            1 -> 1.25f
            2 -> 1.5f
            else -> 1.0f
        }

    var simpleMode: Boolean
        get() = sp.getBoolean("simple_mode", false)
        set(value) {
            sp.edit().putBoolean("simple_mode", value).apply()
        }

    /** When true (default) the home layout cannot be edited. */
    var layoutLocked: Boolean
        get() = sp.getBoolean("layout_locked", true)
        set(value) {
            sp.edit().putBoolean("layout_locked", value).apply()
        }

    /** Language code ("vi", "en", ...) or "" to follow the TV. Default: Vietnamese. */
    var language: String
        get() = sp.getString("language", "vi") ?: "vi"
        set(value) {
            sp.edit().putString("language", value).apply()
        }

    var weatherEnabled: Boolean
        get() = sp.getBoolean("weather_enabled", false)
        set(value) {
            sp.edit().putBoolean("weather_enabled", value).apply()
        }

    var weatherCity: Int
        get() = sp.getInt("weather_city", 0)
        set(value) {
            sp.edit().putInt("weather_city", value).apply()
        }

    /** Ordered list of home-screen entries; null until first run. */
    fun getFavourites(): List<String>? {
        val raw = sp.getString("favourites", null) ?: return null
        return if (raw.isEmpty()) emptyList() else raw.split("\n")
    }

    fun setFavourites(list: List<String>) {
        sp.edit().putString("favourites", list.joinToString("\n")).apply()
    }

    fun saveWeather(city: Int, temp: Int, code: Int) {
        sp.edit().putString("weather", "$city|$temp|$code|${System.currentTimeMillis()}").apply()
    }

    fun cachedWeather(): CachedWeather? {
        val p = sp.getString("weather", null)?.split("|") ?: return null
        if (p.size != 4) return null
        return try {
            CachedWeather(p[0].toInt(), p[1].toInt(), p[2].toInt(), p[3].toLong())
        } catch (e: NumberFormatException) {
            null
        }
    }

    /** Changes when something that needs the screens to be rebuilt changes. */
    fun signature(): String =
        "$language|$textScaleIndex|$simpleMode|$layoutLocked|$weatherEnabled|$weatherCity"
}
