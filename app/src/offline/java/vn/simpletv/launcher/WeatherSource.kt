package vn.simpletv.launcher

/** "offline" flavor: no internet permission, no weather. */
object WeatherSource {
    const val SUPPORTED = false

    @Suppress("UNUSED_PARAMETER")
    fun fetch(lat: Double, lon: Double): WeatherResult? = null
}
