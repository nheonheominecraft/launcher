package vn.simpletv.launcher

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Collections

/**
 * Pick an app with OK, move it with UP / DOWN, drop it with OK.
 * While an app is picked up, the D-pad cannot wander off, so nothing moves by accident.
 */
class ArrangeActivity : BaseActivity() {

    private lateinit var screen: Frame
    private val entries = ArrayList<AppEntry>()
    private val leftover = ArrayList<String>()   // favourites not available on this device; kept as is
    private var moving = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (prefs.layoutLocked) {
            Toast.makeText(this, R.string.locked_toast, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        screen = frame(getString(R.string.arrange_title))
        setContentView(screen.root)

        val repo = AppRepository(this)
        for (key in repo.favourites(prefs)) {
            val e = repo.resolve(key)
            if (e != null) entries.add(e) else leftover.add(key)
        }
        render(0)
    }

    private fun render(focusIdx: Int) {
        val c = screen.content
        c.removeAllViews()

        val hint = ui.text(getString(R.string.arrange_hint), 22f, dim = true)
        hint.setPadding(0, 0, 0, ui.dp(8))
        c.addView(hint, LinearLayout.LayoutParams(MATCH, WRAP))

        if (entries.isEmpty()) {
            c.addView(ui.text(getString(R.string.arrange_empty), 26f))
            return
        }

        for ((i, e) in entries.withIndex()) {
            val isMoving = i == moving
            val r = ui.row(
                "${i + 1}. ${e.label}",
                if (isMoving) getString(R.string.arrange_moving) else null,
                if (isMoving) "▲▼" else null,
                e.icon,
                e.iconRes
            )
            if (isMoving) r.background = ui.movingBg()
            r.id = 1000 + i
            r.setOnClickListener {
                moving = if (moving == -1) i else -1
                render(i)
            }
            c.addView(r, ui.rowLp())
        }
        val target: View? = c.findViewById(1000 + focusIdx)
        target?.requestFocus()
    }

    private fun shift(d: Int) {
        val to = moving + d
        if (d == 0 || to !in entries.indices) return
        Collections.swap(entries, moving, to)
        moving = to
        prefs.setFavourites(entries.map { it.key } + leftover)
        render(to)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (moving >= 0) {
            val code = event.keyCode
            if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN ||
                code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT
            ) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    when (code) {
                        KeyEvent.KEYCODE_DPAD_UP -> shift(-1)
                        KeyEvent.KEYCODE_DPAD_DOWN -> shift(1)
                        else -> {}
                    }
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (moving >= 0) {
            val i = moving
            moving = -1
            render(i)
        } else {
            super.onBackPressed()
        }
    }
}
