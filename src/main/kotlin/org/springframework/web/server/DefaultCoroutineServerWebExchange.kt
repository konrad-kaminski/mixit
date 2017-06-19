package org.springframework.web.server

import org.springframework.http.server.CoroutineServerHttpResponse
import org.springframework.http.server.coroutine.CoroutineServerHttpRequest

class DefaultCoroutineServerWebExchange(val exchange: ServerWebExchange): CoroutineServerWebExchange {
    override val request: CoroutineServerHttpRequest
        get() = CoroutineServerHttpRequest(exchange.request)
    override val response: CoroutineServerHttpResponse
        get() = TODO("not implemented")
    override val session: CoroutineWebSession?
        get() = TODO("not implemented")

    override fun mutate(): CoroutineServerWebExchange.Builder = DefaultCoroutineServerWebExchangeBuilder(exchange.mutate())

    override fun extractServerWebExchange(): ServerWebExchange = exchange
}

class DefaultCoroutineServerWebExchangeBuilder(val builder: ServerWebExchange.Builder) : CoroutineServerWebExchange.Builder {
    override fun build(): CoroutineServerWebExchange = CoroutineServerWebExchange(builder.build())

    override fun request(request: CoroutineServerHttpRequest): CoroutineServerWebExchange.Builder = apply {
        builder.request(request.extractServerHttpRequest())
    }
}
