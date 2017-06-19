package org.springframework.http.server.coroutine

import org.springframework.http.CoroutineHttpInputMessage
import org.springframework.http.HttpRequest
import org.springframework.http.server.reactive.ServerHttpRequest

interface CoroutineServerHttpRequest: CoroutineHttpInputMessage, HttpRequest {
    fun mutate(): Builder

    fun extractServerHttpRequest(): ServerHttpRequest

    companion object {
        operator fun invoke(request: ServerHttpRequest): CoroutineServerHttpRequest = DefaultCoroutineServerHttpRequest(request)
    }

    interface Builder {
        fun header(key: String, value: String): Builder

        fun path(path: String): Builder

        fun build(): CoroutineServerHttpRequest
    }
}

class DefaultCoroutineServerHttpRequestBuilder(val builder: ServerHttpRequest.Builder) : CoroutineServerHttpRequest.Builder {
    override fun header(key: String, value: String): CoroutineServerHttpRequest.Builder = apply {
        builder.header(key, value)
    }

    override fun path(path: String): CoroutineServerHttpRequest.Builder = apply {
        builder.path(path)
    }

    override fun build(): CoroutineServerHttpRequest = CoroutineServerHttpRequest(builder.build())
}
