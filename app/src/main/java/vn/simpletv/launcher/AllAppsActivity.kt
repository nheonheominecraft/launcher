package vn.simpletv.launcher

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout

/** Every installed app in one big grid. Icons are loaded off the main thread. */
class AllAppsActivity : BaseActivity() {

    private lateinit var screen: Frame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = frame(getString(R.string.all_apps_title))
        setContentView(screen.root)

        val loading = ui.text(getString(R.string.loading), 28f)
        screen.content.addView(loading)

        val repo = AppRepository(this)
        Thread {
            val apps = repo.installedApps()
            val icons = apps.map { it.info.loadIcon(packageManager) }
            runOnUiThread {
                if (!isDestroyed && !isFinishing) show(repo, apps, icons)
            }
        }.start()
    }

    private fun show(repo: AppRepository, apps: List<AppItem>, icons: List<Drawable>) {
        val c = screen.content
        c.removeAllViews()
        if (apps.isEmpty()) {
            c.addView(ui.text(getString(R.string.no_apps), 28f))
            return
        }
        val tiles = ArrayList<View>()
        for ((i, a) in apps.withIndex()) {
            val t = ui.tile(a.label, icons[i], 0, 64, 22f, 140)
            t.id = 1000 + i
            t.setOnClickListener { Launch.start(this, repo.launchIntent(a.packageName)) }
            tiles.add(t)
        }
        c.addView(ui.grid(tiles, ui.columns(), ui.dp(16)), LinearLayout.LayoutParams(MATCH, WRAP))
        restoreFocus(tiles[0])
    }
}
