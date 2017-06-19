package org.springframework.web.coroutine.function

import org.springframework.http.CoroutineHttpInputMessage
import org.springframework.http.codec.CoroutineHttpMessageReader
import org.springframework.http.server.CoroutineServerHttpResponse
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.web.reactive.function.BodyExtractor

interface CoroutineBodyExtractor<T, in M: CoroutineHttpInputMessage> {
    suspend fun extract(inputMessage: M, context: Context): T

    fun <N: ReactiveHttpInputMessage> asBodyExtractor(): BodyExtractor<T, N> = TODO()

    interface Context {
        fun messageReaders(): (() -> Sequence<CoroutineHttpMessageReader<*>>)

        fun serverResponse(): CoroutineServerHttpResponse?

        fun hints(): Map<String, Any>
    }
}