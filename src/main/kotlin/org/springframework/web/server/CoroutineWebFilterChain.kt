package org.springframework.web.server

import mixit.util.coroutine.awaitFirstOrNull

interface CoroutineWebFilterChain {
    suspend fun filter(exchange: CoroutineServerWebExchange): Unit

    companion object {
        operator fun invoke(chain: WebFilterChain): CoroutineWebFilterChain = DefaultCoroutineWebFilterChain(chain)
    }
}

class DefaultCoroutineWebFilterChain(val chain: WebFilterChain): CoroutineWebFilterChain {
    suspend override fun filter(exchange: CoroutineServerWebExchange) {
        chain.filter(exchange.extractServerWebExchange()).awaitFirstOrNull()
    }
}