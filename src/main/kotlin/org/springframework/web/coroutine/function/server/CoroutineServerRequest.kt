package org.springframework.web.coroutine.function.server

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.reactive.open
import mixit.model.Language.Companion.findByTag
import mixit.util.coroutine.awaitFirstOrNull
import org.springframework.http.server.coroutine.CoroutineServerHttpRequest
import org.springframework.web.server.CoroutineWebSession
import org.springframework.web.coroutine.function.CoroutineBodyExtractor
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.server.session.asCoroutineWebSession
import java.net.URI

interface CoroutineServerRequest {
    fun <T> body(extractor: CoroutineBodyExtractor<T, CoroutineServerHttpRequest>): T

    fun <T> body(extractor: CoroutineBodyExtractor<T, CoroutineServerHttpRequest>, hints: Map<String, Any>): T

    suspend fun <T> body(elementClass: Class<out T>): T?

    fun <T> bodyToReceiveChannel(elementClass: Class<out T>): ReceiveChannel<T>

    fun headers(): ServerRequest.Headers

    fun pathVariable(name: String): String?

    suspend fun session(): CoroutineWebSession?

    fun uri(): URI

    fun extractServerRequest(): ServerRequest

    companion object {
        operator fun invoke(req: ServerRequest) = DefaultCoroutineServerRequest(req)
    }
}

class DefaultCoroutineServerRequest(val req: ServerRequest): CoroutineServerRequest {
    override fun <T> body(extractor: CoroutineBodyExtractor<T, CoroutineServerHttpRequest>): T =
            req.body(extractor.asBodyExtractor())

    override fun <T> body(extractor: CoroutineBodyExtractor<T, CoroutineServerHttpRequest>, hints: Map<String, Any>): T =
            req.body(extractor.asBodyExtractor(), hints)

    suspend override fun <T> body(elementClass: Class<out T>): T? =
            req.bodyToMono(elementClass).awaitFirstOrNull()

    override fun <T> bodyToReceiveChannel(elementClass: Class<out T>): ReceiveChannel<T> =
            req.bodyToFlux(elementClass).open()

    override fun headers(): ServerRequest.Headers = req.headers()

    override fun pathVariable(name: String): String? = req.pathVariable(name)

    suspend override fun session(): CoroutineWebSession? =  req.session().awaitFirstOrNull()?.asCoroutineWebSession()

    override fun uri(): URI = req.uri()

    override fun extractServerRequest(): ServerRequest = req
}

fun CoroutineServerRequest.language() =
        findByTag(this.headers().asHttpHeaders().acceptLanguageAsLocales.first().language)

inline suspend fun <reified T : Any> CoroutineServerRequest.body(): T? = body(T::class.java)


