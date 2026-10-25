package vn.simpletv.launcher

data class WeatherResult(val tempC: Int, val code: Int)

class City(val name: String, val lat: Double, val lon: Double)

/** Fixed list so nobody has to type on a TV remote. Add more cities here. */
object Cities {
    val ALL = listOf(
        City("Hà Nội", 21.0285, 105.8542),
        City("TP. Hồ Chí Minh", 10.8231, 106.6297),
        City("Hải Phòng", 20.8449, 106.6881),
        City("Đà Nẵng", 16.0544, 108.2022),
        City("Huế", 16.4637, 107.5909),
        City("Vinh", 18.6796, 105.6813),
        City("Quy Nhơn", 13.7820, 109.2192),
        City("Nha Trang", 12.2388, 109.1967),
        City("Đà Lạt", 11.9404, 108.4583),
        City("Buôn Ma Thuột", 12.6667, 108.0500),
        City("Vũng Tàu", 10.3460, 107.0843),
        City("Cần Thơ", 10.0452, 105.7469)
    )
}

/** Maps WMO weather codes (used by Open-Meteo) to a short label. */
fun weatherLabelRes(code: Int): Int = when (code) {
    0, 1 -> R.string.weather_clear
    2, 3 -> R.string.weather_cloudy
    45, 48 -> R.string.weather_fog
    in 51..67, in 80..82 -> R.string.weather_rain
    in 71..77, 85, 86 -> R.string.weather_snow
    in 95..99 -> R.string.weather_storm
    else -> R.string.weather_cloudy
}
