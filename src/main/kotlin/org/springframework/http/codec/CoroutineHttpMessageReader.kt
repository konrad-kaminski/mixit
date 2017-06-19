package org.springframework.http.codec

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import org.springframework.core.ResolvableType
import org.springframework.http.server.coroutine.CoroutineServerHttpRequest
import org.springframework.http.server.CoroutineServerHttpResponse
import org.springframework.http.CoroutineHttpInputMessage
import org.springframework.http.MediaType

interface CoroutineHttpMessageReader<out T> {
    fun canRead(elementType: ResolvableType, mediaType: MediaType): Boolean

    fun read(elementType: ResolvableType, message: CoroutineHttpInputMessage, hints: Map<String, Any>): ReceiveChannel<T>

    suspend fun readSingle(elementType: ResolvableType, message: CoroutineHttpInputMessage, hints: Map<String, Any>): T?

    suspend fun readSingle(actualType: ResolvableType, elementType: ResolvableType, request: CoroutineServerHttpRequest,
                           response: CoroutineServerHttpResponse?, hints: Map<String, Any>): T? =
        readSingle(elementType, request, hints)
}