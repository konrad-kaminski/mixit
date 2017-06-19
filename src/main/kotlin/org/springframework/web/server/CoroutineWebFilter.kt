package org.springframework.web.server

import kotlinx.coroutines.experimental.Unconfined
import mixit.util.coroutine.mono
import reactor.core.publisher.Mono

interface CoroutineWebFilter: WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> = mono(Unconfined) {
        filter(CoroutineServerWebExchange(exchange), CoroutineWebFilterChain(chain))
    } as Mono<Void>

    suspend fun filter(exchange: CoroutineServerWebExchange, chain: CoroutineWebFilterChain): Unit
}