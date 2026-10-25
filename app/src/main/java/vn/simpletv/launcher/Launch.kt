package vn.simpletv.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.tv.TvContract
import android.media.tv.TvInputManager
import android.provider.Settings
import android.widget.Toast

object Launch {

    fun start(ctx: Context, intent: Intent?): Boolean {
        if (intent == null) {
            Toast.makeText(ctx, R.string.cannot_open, Toast.LENGTH_LONG).show()
            return false
        }
        return try {
            ctx.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(ctx, R.string.cannot_open, Toast.LENGTH_LONG).show()
            false
        }
    }

    fun openSystemSettings(ctx: Context) {
        start(ctx, Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** True when Android currently uses this app as the HOME screen. */
    fun isDefaultHome(ctx: Context): Boolean {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val ri = ctx.packageManager.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY)
        return ri?.activityInfo?.packageName == ctx.packageName
    }

    /** The manufacturer / Google home screen (Google TV or Android TV), if there is one. */
    fun stockHome(ctx: Context): ResolveInfo? {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val all = ctx.packageManager.queryIntentActivities(i, 0)
            .filter { it.activityInfo.packageName != ctx.packageName }
        val preferred = listOf(Keys.GOOGLE_TV_HOME, Keys.ANDROID_TV_HOME)
        for (p in preferred) {
            val hit = all.firstOrNull { it.activityInfo.packageName == p }
            if (hit != null) return hit
        }
        return all.firstOrNull()
    }

    fun openStockHome(ctx: Context, ri: ResolveInfo) {
        val i = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setClassName(ri.activityInfo.packageName, ri.activityInfo.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        start(ctx, i)
    }
}

/**
 * HDMI / TV inputs. Android lets an app list the TV's pass-through inputs and ask the
 * system TV app to tune to one. Many boxes (and some Google TV devices) either have no
 * inputs or do not accept the request; in that case the tile is hidden or a clear
 * message tells the user to use the INPUT / SOURCE button on the remote.
 */
object TvInputs {

    class Input(val id: String, val label: String)

    fun list(ctx: Context): List<Input> {
        val m = ctx.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager ?: return emptyList()
        return try {
            m.tvInputList
                .filter { it.isPassthroughInput }
                .map { info ->
                    val custom = info.loadCustomLabel(ctx)
                    val label = (custom ?: info.loadLabel(ctx)).toString()
                    Input(info.id, label)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun open(ctx: Context, inputId: String) {
        try {
            val i = Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(inputId))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(ctx, R.string.inputs_failed, Toast.LENGTH_LONG).show()
        }
    }

    fun openFromHome(ctx: Context, inputs: List<Input>) {
        if (inputs.size == 1) {
            open(ctx, inputs[0].id)
        } else {
            ctx.startActivity(Intent(ctx, InputsActivity::class.java))
        }
    }
}
