package vn.simpletv.launcher

import android.os.Bundle

/**
 * Plain-language help. Every paragraph is a focusable card, so the D-pad scrolls through
 * the text reliably and screen readers read one paragraph at a time.
 */
class HelpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val f = frame(getString(R.string.help_title))
        setContentView(f.root)

        val sections = ArrayList<Pair<String, String>>()
        sections.add(Pair(getString(R.string.help_about_title), getString(R.string.help_about)))
        sections.add(Pair(getString(R.string.help_default_title), getString(R.string.help_default)))
        sections.add(Pair(getString(R.string.help_limits_title), getString(R.string.help_limits)))
        sections.add(Pair(getString(R.string.help_return_title), getString(R.string.help_return)))
        sections.add(Pair(getString(R.string.help_hdmi_title), getString(R.string.help_hdmi)))
        if (WeatherSource.SUPPORTED) {
            sections.add(Pair(getString(R.string.help_weather_title), getString(R.string.help_weather)))
        }

        for ((i, s) in sections.withIndex()) {
            val r = ui.row(s.first, s.second)
            r.id = 1000 + i
            f.content.addView(r, ui.rowLp())
        }
        restoreFocus(f.content.getChildAt(0))
    }
}
