package vn.simpletv.launcher

import android.os.Bundle

/** One simple list used for both "language" and "city". OK picks and goes back. */
class ChoiceActivity : BaseActivity() {

    companion object {
        const val EXTRA_KIND = "kind"
        const val KIND_LANGUAGE = "language"
        const val KIND_CITY = "city"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val kind = intent.getStringExtra(EXTRA_KIND) ?: KIND_LANGUAGE

        val ids = ArrayList<String>()
        val names = ArrayList<String>()
        val current: String
        val titleText: String

        if (kind == KIND_CITY) {
            for ((i, c) in Cities.ALL.withIndex()) {
                ids.add(i.toString())
                names.add(c.name)
            }
            current = prefs.weatherCity.toString()
            titleText = getString(R.string.set_city)
        } else {
            ids.add("")
            names.add(getString(R.string.lang_system))
            for (l in Languages.ALL) {
                ids.add(l.code)
                names.add(l.name)
            }
            current = prefs.language
            titleText = getString(R.string.set_language)
        }

        val f = frame(titleText)
        setContentView(f.root)

        for ((i, id) in ids.withIndex()) {
            val isCurrent = id == current
            val r = ui.row(
                names[i],
                null,
                if (isCurrent) getString(R.string.selected) else null,
                valueColor = Colors.GOOD
            )
            r.id = 1000 + i
            r.setOnClickListener {
                if (kind == KIND_CITY) prefs.weatherCity = id.toInt() else prefs.language = id
                finish()
            }
            f.content.addView(r, ui.rowLp())
        }

        val idx = ids.indexOf(current).coerceAtLeast(0)
        restoreFocus(f.content.getChildAt(idx))
    }
}
