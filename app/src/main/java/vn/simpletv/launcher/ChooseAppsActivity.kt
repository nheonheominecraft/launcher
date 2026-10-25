package vn.simpletv.launcher

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast

/** OK on a row shows or hides that app on the home screen. Only reachable when layout is unlocked. */
class ChooseAppsActivity : BaseActivity() {

    private lateinit var screen: Frame
    private lateinit var favs: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (prefs.layoutLocked) {
            Toast.makeText(this, R.string.locked_toast, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        screen = frame(getString(R.string.choose_title))
        setContentView(screen.root)

        val repo = AppRepository(this)
        favs = repo.favourites(prefs).toMutableList()
        screen.content.addView(ui.text(getString(R.string.loading), 28f))

        Thread {
            val specials = ArrayList<AppEntry>()
            repo.resolve(Keys.STOCK_HOME)?.let { specials.add(it) }
            repo.resolve(Keys.TV_INPUT)?.let { specials.add(it) }
            val apps = repo.installedApps()
            val icons = apps.map { it.info.loadIcon(packageManager) }
            runOnUiThread {
                if (!isDestroyed && !isFinishing) build(specials, apps, icons)
            }
        }.start()
    }

    private fun build(specials: List<AppEntry>, apps: List<AppItem>, icons: List<Drawable>) {
        val c = screen.content
        c.removeAllViews()

        val hint = ui.text(getString(R.string.choose_hint), 22f, dim = true)
        hint.setPadding(0, 0, 0, ui.dp(8))
        c.addView(hint)

        var n = 0
        fun addRow(key: String, title: String, icon: Drawable?, iconRes: Int) {
            val on = favs.contains(key)
            val r = ui.row(
                title,
                null,
                getString(if (on) R.string.shown else R.string.hidden),
                icon,
                iconRes,
                if (on) Colors.GOOD else Colors.TEXT_DIM
            )
            r.id = 1000 + n
            n++
            r.setOnClickListener {
                if (favs.contains(key)) favs.remove(key) else favs.add(key)
                prefs.setFavourites(favs)
                val now = favs.contains(key)
                ui.setValue(
                    r,
                    getString(if (now) R.string.shown else R.string.hidden),
                    if (now) Colors.GOOD else Colors.TEXT_DIM
                )
            }
            c.addView(r, ui.rowLp())
        }

        for (e in specials) addRow(e.key, e.label, e.icon, e.iconRes)
        for ((i, a) in apps.withIndex()) addRow(a.packageName, a.label, icons[i], 0)

        restoreFocus(c.getChildAt(1))
    }
}
