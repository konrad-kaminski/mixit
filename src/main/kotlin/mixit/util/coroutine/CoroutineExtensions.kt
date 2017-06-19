package mixit.util.coroutine

import com.mongodb.client.result.DeleteResult
import kotlinx.coroutines.experimental.AbstractCoroutine
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.newCoroutineContext
import kotlinx.coroutines.experimental.reactive.awaitFirstOrDefault
import org.reactivestreams.Publisher
import org.springframework.data.mongodb.core.CoroutineMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.web.coroutine.function.server.CoroutineBodyBuilder
import org.springframework.web.coroutine.function.server.CoroutineHandlerFunction
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse
import org.springframework.web.coroutine.function.server.asCoroutineServerResponse
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.net.URI
import java.util.Locale
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine

// ----------------------
// Spring Data extensions
// ----------------------

inline suspend fun <reified T : Any> CoroutineMongoOperations.find(query: Query): ReceiveChannel<T> =
        find(query, T::class.java)

inline suspend fun <reified T : Any> CoroutineMongoOperations.findById(id: Any): T? =
        findById(id, T::class.java)

inline suspend fun <reified T : Any> CoroutineMongoOperations.findOne(query: Query): T? =
        find(query, T::class.java).receiveOrNull()

inline suspend fun <reified T : Any> CoroutineMongoOperations.findAll(): ReceiveChannel<T> =
        findAll(T::class.java)

inline suspend fun <reified T : Any> CoroutineMongoOperations.count(): Long =
        count(Query(), T::class.java)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline suspend fun <reified T : Any> CoroutineMongoOperations.remove(query: Query): DeleteResult? =
        remove(query, T::class.java)

// -------------------------
// Spring WebFlux extensions
// -------------------------

suspend fun permanentRedirect(uri: String) = CoroutineServerResponse.permanentRedirect(URI(uri)).build()

suspend fun seeOther(uri: String) = CoroutineServerResponse.seeOther(URI(uri)).build()

fun CoroutineBodyBuilder.json() = contentType(APPLICATION_JSON_UTF8)

fun <T: CoroutineServerResponse, R: CoroutineServerResponse> RouterFunction<ServerResponse>.filter(filterFunction: CoroutineHandlerFilterFunction<T, R>): RouterFunction<ServerResponse> =
    filter(filterFunction.asHandlerFunction())

private fun <T: CoroutineServerResponse, R: CoroutineServerResponse> CoroutineHandlerFilterFunction<T, R>.asHandlerFunction()
        : HandlerFilterFunction<ServerResponse, ServerResponse>
        = HandlerFilterFunction { request, next: HandlerFunction<ServerResponse> ->
    mono(Unconfined) {
        this@asHandlerFunction.invoke(org.springframework.web.coroutine.function.server.CoroutineServerRequest.Companion(request)) { request ->
            next.handle(request.extractServerRequest()).awaitFirstOrNull()?.let { it.asCoroutineServerResponse() }
        }?.extractServerResponse()
    }
}

typealias CoroutineHandlerFilterFunction<T, R> = suspend (CoroutineServerRequest, CoroutineHandlerFunction<T>) -> R?

fun CoroutineServerRequest.locale() = this.headers().asHttpHeaders().acceptLanguageAsLocales.first() ?: Locale.ENGLISH

// ----------------------------
// Kotlin coroutines extensions
// ----------------------------

suspend fun <T> ReceiveChannel<T>.collectList(): List<T> {
    val list = mutableListOf<T>()

    consumeEach { list += it }

    return list
}

suspend fun <K, T> ReceiveChannel<T>.collectMap(keyExtractor: (T) -> K): Map<K, T> {
    val map = mutableMapOf<K, T>()

    consumeEach { map += keyExtractor(it) to it }

    return map
}

inline suspend fun <T> Publisher<T>.awaitFirstOrNull(): T? = awaitFirstOrDefault(null as T)

fun <T> mono(
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> T?
): Mono<T> = Mono.create { sink ->
    val newContext = newCoroutineContext(context)
    val coroutine = MonoCoroutine(newContext, sink)
    coroutine.initParentJob(context[Job])
    sink.onDispose(coroutine)
    block.startCoroutine(coroutine, coroutine)
}

private class MonoCoroutine<in T>(
        override val parentContext: CoroutineContext,
        private val sink: MonoSink<T>
) : AbstractCoroutine<T>(true), Disposable {
    var disposed = false

    @Suppress("UNCHECKED_CAST")
    override fun afterCompletion(state: Any?, mode: Int) {
        when {
            disposed                        -> {}
            state is CompletedExceptionally -> sink.error(state.exception)
            state != null                   -> sink.success(state as T)
            else                            -> sink.success()
        }
    }

    override fun dispose() {
        disposed = true
        cancel(cause = null)
    }

    override fun isDisposed(): Boolean = disposed
}