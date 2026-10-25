package vn.simpletv.launcher

import android.os.Bundle

/** Lists the TV's HDMI / other pass-through inputs (only used when there is more than one). */
class InputsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val f = frame(getString(R.string.inputs_title))
        setContentView(f.root)

        val inputs = TvInputs.list(this)
        for ((i, inp) in inputs.withIndex()) {
            val r = ui.row(inp.label, null, null, null, R.drawable.ic_tv_input)
            r.id = 1000 + i
            r.setOnClickListener { TvInputs.open(this, inp.id) }
            f.content.addView(r, ui.rowLp())
        }
        restoreFocus(f.content.getChildAt(0))
    }
}
