package vn.simpletv.launcher

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

/** High-contrast palette. Focused item = bright yellow with black text and a white frame. */
object Colors {
    val BG = 0xFF0B1620.toInt()
    val CARD = 0xFF1B2B3A.toInt()
    val BORDER = 0xFF5B7288.toInt()
    val FOCUS = 0xFFFFD60A.toInt()
    val FOCUS_BORDER = 0xFFFFFFFF.toInt()
    val TEXT = 0xFFFFFFFF.toInt()
    val TEXT_DIM = 0xFFC9D6E2.toInt()
    val ON_FOCUS = 0xFF000000.toInt()
    val ON_FOCUS_DIM = 0xFF262626.toInt()
    val GOOD = 0xFF7CE38B.toInt()
    val WARN = 0xFFFFB86B.toInt()
    val MOVING = 0xFF7CE38B.toInt()
}

/** A screen: title on top, scrolling content, and a "back to main screen" bar at the bottom. */
class Frame(val root: View, val content: LinearLayout, val scroll: ScrollView)

/** Small helper that builds all views in code (no XML layouts, no libraries). */
class Ui(private val ctx: Context, val scale: Float) {

    private val density = ctx.resources.displayMetrics.density
    private val focusState = intArrayOf(android.R.attr.state_focused)
    private val noState = intArrayOf()

    fun dp(v: Int): Int = (v * density + 0.5f).toInt()

    /** Number of tiles per row on the home screen. */
    fun columns(): Int = if (scale > 1.1f) 3 else 4

    fun focusColors(focused: Int, normal: Int): ColorStateList =
        ColorStateList(arrayOf(focusState, noState), intArrayOf(focused, normal))

    private fun shape(fill: Int, stroke: Int, strokeDp: Int, radiusDp: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(fill)
        g.cornerRadius = dp(radiusDp).toFloat()
        g.setStroke(dp(strokeDp), stroke)
        return g
    }

    fun focusBg(radiusDp: Int): StateListDrawable {
        val d = StateListDrawable()
        d.addState(focusState, shape(Colors.FOCUS, Colors.FOCUS_BORDER, 6, radiusDp))
        d.addState(noState, shape(Colors.CARD, Colors.BORDER, 2, radiusDp))
        return d
    }

    fun movingBg(): Drawable = shape(Colors.MOVING, Colors.FOCUS, 6, 16)

    /** Text that turns black while its parent card is focused. */
    fun text(
        value: CharSequence,
        sp: Float,
        bold: Boolean = false,
        dim: Boolean = false,
        color: Int = if (dim) Colors.TEXT_DIM else Colors.TEXT,
        focusColor: Int = if (dim) Colors.ON_FOCUS_DIM else Colors.ON_FOCUS
    ): TextView {
        val tv = TextView(ctx)
        tv.text = value
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp * scale)
        tv.setTextColor(focusColors(focusColor, color))
        if (bold) tv.typeface = Typeface.DEFAULT_BOLD
        tv.isDuplicateParentStateEnabled = true
        return tv
    }

    /** A focusable, clickable card. OK / Enter on the remote triggers its click listener. */
    fun card(vertical: Boolean): LinearLayout {
        val c = LinearLayout(ctx)
        c.orientation = if (vertical) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        c.isFocusable = true
        c.isClickable = true
        c.background = focusBg(16)
        return c
    }

    fun rowLp(): LinearLayout.LayoutParams {
        val lp = LinearLayout.LayoutParams(MATCH, WRAP)
        lp.topMargin = dp(6)
        lp.bottomMargin = dp(6)
        return lp
    }

    /** Wide row: optional icon, title, optional description, optional value on the right. */
    fun row(
        title: String,
        desc: String? = null,
        value: String? = null,
        icon: Drawable? = null,
        iconRes: Int = 0,
        valueColor: Int = Colors.TEXT
    ): LinearLayout {
        val r = card(false)
        r.gravity = Gravity.CENTER_VERTICAL
        r.setPadding(dp(24), dp(16), dp(24), dp(16))
        r.minimumHeight = dp(76)

        if (icon != null || iconRes != 0) {
            val iv = ImageView(ctx)
            iv.isDuplicateParentStateEnabled = true
            if (icon != null) {
                iv.setImageDrawable(icon)
            } else {
                iv.setImageResource(iconRes)
                iv.imageTintList = focusColors(Colors.ON_FOCUS, Colors.TEXT)
            }
            val lp = LinearLayout.LayoutParams(dp(48), dp(48))
            lp.rightMargin = dp(20)
            r.addView(iv, lp)
        }

        val left = LinearLayout(ctx)
        left.orientation = LinearLayout.VERTICAL
        left.isDuplicateParentStateEnabled = true
        left.addView(text(title, 26f, bold = true))
        if (desc != null) {
            val d = text(desc, 20f, dim = true)
            d.setPadding(0, dp(4), 0, 0)
            left.addView(d)
        }
        r.addView(left, LinearLayout.LayoutParams(0, WRAP, 1f))

        if (value != null) {
            val v = text(value, 26f, bold = true, color = valueColor)
            v.setPadding(dp(16), 0, 0, 0)
            v.tag = "value"
            r.addView(v, LinearLayout.LayoutParams(WRAP, WRAP))
        }
        return r
    }

    fun setValue(row: View, value: String, color: Int) {
        val v = row.findViewWithTag<TextView>("value") ?: return
        v.text = value
        v.setTextColor(focusColors(Colors.ON_FOCUS, color))
    }

    /** Big square-ish tile: icon on top, label below. */
    fun tile(label: String, icon: Drawable?, iconRes: Int, iconDp: Int, labelSp: Float, minHeightDp: Int): LinearLayout {
        val t = card(true)
        t.gravity = Gravity.CENTER
        t.setPadding(dp(12), dp(16), dp(12), dp(16))
        t.minimumHeight = dp(minHeightDp)

        val iv = ImageView(ctx)
        iv.scaleType = ImageView.ScaleType.FIT_CENTER
        iv.isDuplicateParentStateEnabled = true
        if (icon != null) {
            iv.setImageDrawable(icon)
        } else {
            iv.setImageResource(iconRes)
            iv.imageTintList = focusColors(Colors.ON_FOCUS, Colors.TEXT)
        }
        t.addView(iv, LinearLayout.LayoutParams(dp(iconDp), dp(iconDp)))

        val tv = text(label, labelSp, bold = true)
        tv.gravity = Gravity.CENTER
        tv.maxLines = 2
        tv.ellipsize = TextUtils.TruncateAt.END
        tv.setPadding(0, dp(10), 0, 0)
        t.addView(tv, LinearLayout.LayoutParams(MATCH, WRAP))
        return t
    }

    /** Lays views out in rows of [columns] equal-width cells. Short last rows are padded. */
    fun grid(tiles: List<View>, columns: Int, gapPx: Int): LinearLayout {
        val box = LinearLayout(ctx)
        box.orientation = LinearLayout.VERTICAL
        var i = 0
        while (i < tiles.size) {
            val row = LinearLayout(ctx)
            row.orientation = LinearLayout.HORIZONTAL
            row.isBaselineAligned = false
            for (c in 0 until columns) {
                val cell: View = if (i + c < tiles.size) tiles[i + c] else View(ctx)
                val lp = LinearLayout.LayoutParams(0, MATCH, 1f)
                lp.setMargins(gapPx / 2, gapPx / 2, gapPx / 2, gapPx / 2)
                row.addView(cell, lp)
            }
            box.addView(row, LinearLayout.LayoutParams(MATCH, WRAP))
            i += columns
        }
        return box
    }
}
