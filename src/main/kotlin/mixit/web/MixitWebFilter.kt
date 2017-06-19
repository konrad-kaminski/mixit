package mixit.web

import mixit.MixitProperties
import org.springframework.web.server.CoroutineServerWebExchange
import org.springframework.web.server.CoroutineWebFilter
import org.springframework.web.server.CoroutineWebFilterChain
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.net.URI
import java.util.Locale


@Component
class CoroutineMixitWebFilter(val properties: MixitProperties) : CoroutineWebFilter {

    private val redirectDoneAttribute = "redirectDone"

    suspend override fun filter(exchange: CoroutineServerWebExchange, chain: CoroutineWebFilterChain) {
       if (exchange.request.headers.host?.hostString?.endsWith("mix-it.fr") ?: false) {
           val response = exchange.response
           response.statusCode = HttpStatus.PERMANENT_REDIRECT
           response.headers.location = URI("${properties.baseUri}${exchange.request.uri.path}")
       }
       else if (exchange.request.uri.path.startsWith("/admin")) {
            exchange.session?.let {
            if (it.attributes["username"] != null) {
                chain.filter(exchange)
            }
            else {
                val response = exchange.response
                response.statusCode = HttpStatus.TEMPORARY_REDIRECT
                response.headers.location = URI("${properties.baseUri}/login")
                }
            }
       }
       else if (exchange.request.uri.path.startsWith("/en/"))
            chain.filter(exchange.mutate().request(exchange.request.mutate()
                    .path(exchange.request.uri.path.substring(3))
                    .header(ACCEPT_LANGUAGE, "en").build()).build())
        else if (exchange.request.uri.path == "/" &&
                (exchange.request.headers.acceptLanguageAsLocales.firstOrNull() ?: Locale.FRENCH).language != "fr" &&
                !isSearchEngineCrawler(exchange)) {
            val response = exchange.response
            exchange.session?.let {
                if (it.attributes[redirectDoneAttribute] == true)
                    chain.filter(exchange.mutate().request(exchange.request.mutate().header(ACCEPT_LANGUAGE, "fr").build()).build())
                else {
                    response.statusCode = HttpStatus.TEMPORARY_REDIRECT
                    response.headers.location = URI("${properties.baseUri}/en/")
                    it.attributes[redirectDoneAttribute] = true
                    it.save()
                }
            }
        }
        else
            chain.filter(exchange.mutate().request(exchange.request.mutate().header(ACCEPT_LANGUAGE, "fr").build()).build())
    }

    private fun isSearchEngineCrawler(exchange: CoroutineServerWebExchange) : Boolean {
        val userAgent = exchange.request.headers.getFirst(HttpHeaders.USER_AGENT) ?: ""
        val bots = arrayOf("Google", "Bingbot", "Qwant", "Bingbot", "Slurp", "DuckDuckBot", "Baiduspider")
        return bots.any { userAgent.contains(it) }
    }
}

