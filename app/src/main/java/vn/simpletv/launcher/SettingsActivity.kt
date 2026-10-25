package vn.simpletv.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast

/** One flat list. No nested menus. OK changes a value or opens ONE simple screen. */
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val f = frame(getString(R.string.settings_title))
        setContentView(f.root)
        val c = f.content

        var n = 0
        fun add(row: LinearLayout, action: () -> Unit) {
            row.id = 1000 + n
            n++
            row.setOnClickListener { action() }
            c.addView(row, ui.rowLp())
        }
        fun onOff(b: Boolean): String = getString(if (b) R.string.on else R.string.off)

        // Is this launcher really the default home screen?
        val isDefault = Launch.isDefaultHome(this)
        val status = ui.text(
            getString(if (isDefault) R.string.status_default else R.string.status_not_default),
            22f,
            bold = true,
            color = if (isDefault) Colors.GOOD else Colors.WARN
        )
        status.setPadding(ui.dp(8), 0, ui.dp(8), ui.dp(8))
        c.addView(status, LinearLayout.LayoutParams(MATCH, WRAP))

        val sizeNames = arrayOf(
            getString(R.string.size_normal),
            getString(R.string.size_large),
            getString(R.string.size_xlarge)
        )
        add(ui.row(getString(R.string.set_text_size), getString(R.string.set_text_size_desc), sizeNames[prefs.textScaleIndex])) {
            prefs.textScaleIndex = (prefs.textScaleIndex + 1) % 3
            recreate()
        }

        add(ui.row(getString(R.string.set_simple), getString(R.string.set_simple_desc), onOff(prefs.simpleMode))) {
            prefs.simpleMode = !prefs.simpleMode
            recreate()
        }

        val locked = prefs.layoutLocked
        val lockedValue = if (locked) getString(R.string.locked_value) else null

        add(ui.row(getString(R.string.set_choose), getString(R.string.set_choose_desc), lockedValue, valueColor = Colors.WARN)) {
            if (locked) toastLocked() else startActivity(Intent(this, ChooseAppsActivity::class.java))
        }

        add(ui.row(getString(R.string.set_arrange), getString(R.string.set_arrange_desc), lockedValue, valueColor = Colors.WARN)) {
            if (locked) toastLocked() else startActivity(Intent(this, ArrangeActivity::class.java))
        }

        add(ui.row(getString(R.string.set_lock), getString(R.string.set_lock_desc), onOff(locked))) {
            prefs.layoutLocked = !prefs.layoutLocked
            recreate()
        }

        val langName = Languages.ALL.firstOrNull { it.code == prefs.language }?.name
            ?: getString(R.string.lang_system)
        add(ui.row(getString(R.string.set_language), getString(R.string.set_language_desc), langName)) {
            startActivity(
                Intent(this, ChoiceActivity::class.java)
                    .putExtra(ChoiceActivity.EXTRA_KIND, ChoiceActivity.KIND_LANGUAGE)
            )
        }

        if (WeatherSource.SUPPORTED) {
            add(ui.row(getString(R.string.set_weather), getString(R.string.set_weather_desc), onOff(prefs.weatherEnabled))) {
                prefs.weatherEnabled = !prefs.weatherEnabled
                recreate()
            }
            val cityName = Cities.ALL[prefs.weatherCity.coerceIn(0, Cities.ALL.size - 1)].name
            add(ui.row(getString(R.string.set_city), getString(R.string.set_city_desc), cityName)) {
                startActivity(
                    Intent(this, ChoiceActivity::class.java)
                        .putExtra(ChoiceActivity.EXTRA_KIND, ChoiceActivity.KIND_CITY)
                )
            }
        }

        add(ui.row(getString(R.string.set_tv_settings), getString(R.string.set_tv_settings_desc))) {
            Launch.openSystemSettings(this)
        }

        add(ui.row(getString(R.string.set_help), getString(R.string.set_help_desc))) {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        restoreFocus(c.getChildAt(1))
    }

    private fun toastLocked() {
        Toast.makeText(this, R.string.locked_toast, Toast.LENGTH_LONG).show()
    }
}
