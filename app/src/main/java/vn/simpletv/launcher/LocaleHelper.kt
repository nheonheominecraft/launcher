package vn.simpletv.launcher

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * To add a language:
 *  1. create app/src/main/res/values-XX/strings.xml (copy values/strings.xml and translate)
 *  2. add one line to [Languages.ALL] below
 */
object Languages {
    class Lang(val code: String, val name: String)

    val ALL = listOf(
        Lang("vi", "Tiếng Việt"),
        Lang("en", "English")
    )
}

object LocaleHelper {
    /** Wraps a context so it uses the language chosen in the launcher settings. */
    fun wrap(base: Context): Context {
        val code = Prefs(base).language
        if (code.isEmpty()) return base
        val locale = Locale.forLanguageTag(code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
