package org.springframework.http.server.coroutine

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.reactive.open
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import java.net.URI

class DefaultCoroutineServerHttpRequest(val request: ServerHttpRequest): CoroutineServerHttpRequest {
    override val body: ReceiveChannel<DataBuffer>
        get() = request.body.open()

    override fun getHeaders(): HttpHeaders = request.headers

    override fun getMethod(): HttpMethod = request.method

    override fun getURI(): URI = request.uri

    override fun mutate(): CoroutineServerHttpRequest.Builder = DefaultCoroutineServerHttpRequestBuilder(request.mutate())

    override fun extractServerHttpRequest(): ServerHttpRequest = request
}