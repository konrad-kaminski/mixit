package mixit.web

import com.samskivert.mustache.Mustache
import mixit.util.MarkdownConverter
import mixit.util.localePrefix
import org.springframework.context.MessageSource
import org.springframework.web.server.CoroutineWebSession
import org.springframework.web.util.UriUtils
import java.util.*

fun generateModel(baseUri: String,
                  path: String,
                  locale: Locale,
                  session: CoroutineWebSession,
                  messageSource: MessageSource,
                  markdownConverter: MarkdownConverter
                  ) = mutableMapOf<String, Any>().apply {

    val username = session.getAttribute<String>("username")
        if (username != null) {
            this["username"] = username
            if (username == "mixit") this["admin"] = true
        }
        this["locale"] = locale.toString()
        this["localePrefix"] = localePrefix(locale)
        this["en"] = locale.language == "en"
        this["fr"] = locale.language == "fr"
        this["switchLangUrl"] = if (locale.language == "en") path else "/en" + path
        this["baseUri"] = baseUri
        this["uri"] = "$baseUri$path"
        this["i18n"] = Mustache.Lambda { frag, out ->
            val tokens = frag.execute().split("|")
            out.write(messageSource.getMessage(tokens[0], tokens.slice(IntRange(1, tokens.size - 1)).toTypedArray(), locale))
        }
        this["urlEncode"] = Mustache.Lambda { frag, out -> out.write(UriUtils.encodePathSegment(frag.execute(), "UTF-8")) }
        this["markdown"] = Mustache.Lambda { frag, out -> out.write(markdownConverter.toHTML(frag.execute())) }
}.toMap()
