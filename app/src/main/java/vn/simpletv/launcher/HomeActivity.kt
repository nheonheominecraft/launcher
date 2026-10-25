package vn.simpletv.launcher

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import java.util.Locale

/**
 * The home screen. Big clock and date, big tiles, nothing else.
 * Back does nothing except jump to the first tile, so it can never "exit" the launcher.
 */
class HomeActivity : BaseActivity() {

    private lateinit var scroll: ScrollView
    private lateinit var tilesBox: LinearLayout
    private lateinit var weatherView: TextView
    private val items = ArrayList<View>()
    private var lastFocus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Colors.BG)
        root.setPadding(ui.dp(56), ui.dp(28), ui.dp(56), ui.dp(20))

        val top = LinearLayout(this)
        top.orientation = LinearLayout.HORIZONTAL
        top.gravity = Gravity.CENTER_VERTICAL

        val clockBox = LinearLayout(this)
        clockBox.orientation = LinearLayout.VERTICAL

        val clock = TextClock(this)
        clock.setTextColor(Colors.TEXT)
        clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 72f * minOf(ui.scale, 1.25f))
        clock.typeface = Typeface.DEFAULT_BOLD
        clockBox.addView(clock)

        val date = TextClock(this)
        val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEEdMMMMy")
        date.format12Hour = pattern
        date.format24Hour = pattern
        date.setTextColor(Colors.TEXT_DIM)
        date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f * ui.scale)
        clockBox.addView(date)

        top.addView(clockBox, LinearLayout.LayoutParams(0, WRAP, 1f))

        weatherView = ui.text("", 28f, bold = true)
        weatherView.gravity = Gravity.END
        weatherView.visibility = View.GONE
        top.addView(weatherView, LinearLayout.LayoutParams(WRAP, WRAP))

        val topLp = LinearLayout.LayoutParams(MATCH, WRAP)
        topLp.bottomMargin = ui.dp(12)
        root.addView(top, topLp)

        scroll = ScrollView(this)
        tilesBox = LinearLayout(this)
        tilesBox.orientation = LinearLayout.VERTICAL
        scroll.addView(tilesBox)
        root.addView(scroll, LinearLayout.LayoutParams(MATCH, 0, 1f))

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        // Apps may have been installed or removed while we were away, so rebuild every time.
        populate()
        updateWeather()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // HOME pressed while already on the home screen: go back to the first tile.
        lastFocus = 0
        populate()
        scroll.scrollTo(0, 0)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        // A launcher must never close itself.
        items.firstOrNull()?.requestFocus()
        scroll.scrollTo(0, 0)
    }

    private fun populate() {
        val repo = AppRepository(this)
        val entries = ArrayList<AppEntry>()
        for (key in repo.favourites(prefs)) {
            val e = repo.resolve(key)
            if (e != null) entries.add(e)
        }

        val simple = prefs.simpleMode
        val iconDp = if (simple) 96 else 64
        val labelSp = if (simple) 34f else 24f
        val minH = if (simple) 200 else 150
        val cols = if (simple) 2 else ui.columns()
        val gap = ui.dp(if (simple) 24 else 16)

        tilesBox.removeAllViews()
        items.clear()

        val shown = if (simple) entries.take(4) else entries
        val tiles = ArrayList<View>()
        for (e in shown) {
            val t = ui.tile(e.label, e.icon, e.iconRes, iconDp, labelSp, minH)
            t.setOnClickListener { e.open() }
            tiles.add(t)
        }
        if (!simple) {
            val all = ui.tile(getString(R.string.tile_all_apps), null, R.drawable.ic_apps, iconDp, labelSp, minH)
            all.setOnClickListener { startActivity(Intent(this, AllAppsActivity::class.java)) }
            tiles.add(all)

            val settings = ui.tile(getString(R.string.tile_settings), null, R.drawable.ic_settings, iconDp, labelSp, minH)
            settings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
            tiles.add(settings)
        }
        items.addAll(tiles)

        if (entries.isEmpty()) {
            val hint = ui.text(getString(R.string.empty_home), 26f, color = Colors.WARN)
            hint.setPadding(0, 0, 0, ui.dp(12))
            tilesBox.addView(hint, LinearLayout.LayoutParams(MATCH, WRAP))
        }
        if (tiles.isNotEmpty()) {
            tilesBox.addView(ui.grid(tiles, cols, gap), LinearLayout.LayoutParams(MATCH, WRAP))
        }
        if (simple) {
            // Simple mode keeps one small, reachable way to the settings (for a family member).
            val s = ui.row(getString(R.string.tile_settings), null, null, null, R.drawable.ic_settings)
            s.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
            val lp = LinearLayout.LayoutParams(MATCH, WRAP)
            lp.topMargin = ui.dp(16)
            tilesBox.addView(s, lp)
            items.add(s)
        }

        for ((i, v) in items.withIndex()) {
            v.id = 1000 + i
            v.setOnFocusChangeListener { _, has -> if (has) lastFocus = i }
        }
        val target = items.getOrNull(lastFocus) ?: items.firstOrNull()
        target?.requestFocus()
    }

    private fun updateWeather() {
        if (!WeatherSource.SUPPORTED || !prefs.weatherEnabled) {
            weatherView.visibility = View.GONE
            return
        }
        val cityIdx = prefs.weatherCity.coerceIn(0, Cities.ALL.size - 1)
        showCachedWeather(cityIdx)

        val cached = prefs.cachedWeather()
        val fresh = cached != null && cached.city == cityIdx &&
            System.currentTimeMillis() - cached.time < 30 * 60 * 1000L
        if (fresh) return

        val city = Cities.ALL[cityIdx]
        Thread {
            val r = WeatherSource.fetch(city.lat, city.lon)
            if (r != null) {
                prefs.saveWeather(cityIdx, r.tempC, r.code)
                runOnUiThread {
                    if (!isDestroyed) showCachedWeather(cityIdx)
                }
            }
        }.start()
    }

    private fun showCachedWeather(cityIdx: Int) {
        val c = prefs.cachedWeather()
        if (c != null && c.city == cityIdx) {
            val name = Cities.ALL[cityIdx].name
            weatherView.text = "$name\n${c.temp}°C, ${getString(weatherLabelRes(c.code))}"
            weatherView.visibility = View.VISIBLE
        } else {
            weatherView.visibility = View.GONE
        }
    }
}
