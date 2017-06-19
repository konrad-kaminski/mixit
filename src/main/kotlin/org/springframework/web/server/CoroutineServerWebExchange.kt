package org.springframework.web.server

import org.springframework.http.server.CoroutineServerHttpResponse
import org.springframework.http.server.coroutine.CoroutineServerHttpRequest

interface CoroutineServerWebExchange {
    val request: CoroutineServerHttpRequest

    val response: CoroutineServerHttpResponse

    val session: CoroutineWebSession?

    fun mutate(): Builder

    fun extractServerWebExchange(): ServerWebExchange

    companion object {
        operator fun invoke(exchange: ServerWebExchange): CoroutineServerWebExchange = DefaultCoroutineServerWebExchange(exchange)
    }

    interface Builder {
        fun request(request: CoroutineServerHttpRequest): Builder

        fun build(): CoroutineServerWebExchange
    }
}