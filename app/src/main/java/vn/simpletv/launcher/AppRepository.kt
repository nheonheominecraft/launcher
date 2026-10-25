package vn.simpletv.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import java.text.Collator

/** Special (non-app) home entries use keys starting with "@". Apps use their package name. */
object Keys {
    const val STOCK_HOME = "@stockhome"
    const val TV_INPUT = "@tvinput"
    const val GOOGLE_TV_HOME = "com.google.android.apps.tv.launcherx"
    const val ANDROID_TV_HOME = "com.google.android.tvlauncher"
}

class AppItem(val packageName: String, val label: String, val info: ResolveInfo)

/** One thing that can be shown and opened from the home screen. */
class AppEntry(
    val key: String,
    val label: String,
    val icon: Drawable?,
    val iconRes: Int,
    val open: () -> Unit
)

class AppRepository(private val ctx: Context) {

    private val pm: PackageManager = ctx.packageManager

    /** Intent that opens the app, or null if it is not installed / not launchable. */
    fun launchIntent(pkg: String): Intent? {
        val i = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg)
        if (i == null) return null
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return i
    }

    private fun labelAndIcon(pkg: String): Pair<String, Drawable>? {
        return try {
            val ai = pm.getApplicationInfo(pkg, 0)
            Pair(ai.loadLabel(pm).toString(), ai.loadIcon(pm))
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /** All launchable apps except this launcher, sorted by name. Call from a background thread. */
    fun installedApps(): List<AppItem> {
        val seen = HashSet<String>()
        val result = ArrayList<AppItem>()
        val categories = arrayOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
        for (cat in categories) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(cat)
            for (ri in pm.queryIntentActivities(query, 0)) {
                val pkg = ri.activityInfo.packageName
                if (pkg == ctx.packageName) continue
                if (!seen.add(pkg)) continue
                result.add(AppItem(pkg, ri.loadLabel(pm).toString(), ri))
            }
        }
        val collator = Collator.getInstance()
        result.sortWith { a, b -> collator.compare(a.label, b.label) }
        return result
    }

    /** Turns a favourites key into something showable, or null if unavailable on this device. */
    fun resolve(key: String): AppEntry? {
        if (key == Keys.STOCK_HOME) {
            val ri = Launch.stockHome(ctx) ?: return null
            val pkg = ri.activityInfo.packageName
            val label = if (pkg == Keys.GOOGLE_TV_HOME) {
                ctx.getString(R.string.tile_google_tv)
            } else {
                ctx.getString(R.string.tile_original_home)
            }
            return AppEntry(key, label, ri.loadIcon(pm), 0) { Launch.openStockHome(ctx, ri) }
        }
        if (key == Keys.TV_INPUT) {
            val inputs = TvInputs.list(ctx)
            if (inputs.isEmpty()) return null
            return AppEntry(key, ctx.getString(R.string.tile_tv_input), null, R.drawable.ic_tv_input) {
                TvInputs.openFromHome(ctx, inputs)
            }
        }
        val intent = launchIntent(key) ?: return null
        val li = labelAndIcon(key) ?: return null
        return AppEntry(key, li.first, li.second, 0) { Launch.start(ctx, intent) }
    }

    /** First-run favourites: YouTube, YouTube TV, Netflix (whichever are installed), then TV inputs. */
    fun defaultFavourites(): List<String> {
        val groups = listOf(
            listOf("com.google.android.youtube.tv", "com.google.android.youtube"),
            listOf("com.google.android.youtube.tvunplugged"),
            listOf("com.netflix.ninja", "com.netflix.mediaclient")
        )
        val result = ArrayList<String>()
        for (g in groups) {
            val found = g.firstOrNull { launchIntent(it) != null }
            if (found != null) result.add(found)
        }
        result.add(Keys.STOCK_HOME)
        result.add(Keys.TV_INPUT)
        return result
    }

    fun favourites(prefs: Prefs): List<String> {
        val saved = prefs.getFavourites()
        if (saved != null) return saved
        val defaults = defaultFavourites()
        prefs.setFavourites(defaults)
        return defaults
    }
}
