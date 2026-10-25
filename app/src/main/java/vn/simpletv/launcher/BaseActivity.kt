package vn.simpletv.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView

/**
 * Shared behaviour: applies the chosen language, builds the common screen frame and
 * rebuilds itself when a setting that affects the look has changed.
 *
 * Focusable items get ids 1000, 1001, ... so the focused one can be restored after a rebuild.
 */
abstract class BaseActivity : Activity() {

    protected lateinit var prefs: Prefs
    protected lateinit var ui: Ui
    private var appliedSignature = ""
    private var savedFocusId = -1

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        ui = Ui(this, prefs.textScale)
        appliedSignature = prefs.signature()
        savedFocusId = savedInstanceState?.getInt("focus_id", -1) ?: -1
    }

    override fun onResume() {
        super.onResume()
        if (prefs.signature() != appliedSignature) recreate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val f = currentFocus
        if (f != null) outState.putInt("focus_id", f.id)
    }

    /** Focus the item that had focus before a rebuild, otherwise [fallback]. */
    protected fun restoreFocus(fallback: View?) {
        val saved: View? = if (savedFocusId >= 1000) findViewById(savedFocusId) else null
        val target = saved ?: fallback
        target?.requestFocus()
    }

    protected fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    protected fun frame(title: String, withHomeBar: Boolean = true): Frame {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Colors.BG)
        // Generous margins keep everything inside the "safe area" of TVs that crop the edges.
        root.setPadding(ui.dp(56), ui.dp(28), ui.dp(56), ui.dp(20))

        val titleView = ui.text(title, 40f, bold = true)
        titleView.setPadding(0, 0, 0, ui.dp(12))
        root.addView(titleView, LinearLayout.LayoutParams(MATCH, WRAP))

        val scroll = ScrollView(this)
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(MATCH, 0, 1f))

        if (withHomeBar) {
            val bar = ui.row(getString(R.string.go_home), getString(R.string.go_home_desc))
            bar.setOnClickListener { goHome() }
            val lp = LinearLayout.LayoutParams(MATCH, WRAP)
            lp.topMargin = ui.dp(12)
            root.addView(bar, lp)
        }
        return Frame(root, content, scroll)
    }
}
