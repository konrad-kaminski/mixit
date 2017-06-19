package org.springframework.web.coroutine.function

import org.springframework.core.ResolvableType.forClass
import org.springframework.core.ResolvableType.forClassWithGenerics
import org.springframework.http.codec.CoroutineHttpMessageReader
import org.springframework.http.server.coroutine.CoroutineServerHttpRequest
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.Part
import org.springframework.util.MultiValueMap

private val FORM_MAP_TYPE = forClassWithGenerics(MultiValueMap::class.java, String::class.java, String::class.java)

private val MULTIPART_MAP_TYPE = forClassWithGenerics(
        MultiValueMap::class.java, String::class.java, Part::class.java)

private val PART_TYPE = forClass(Part::class.java)


fun toFormData(): CoroutineBodyExtractor<MultiValueMap<String, String>?, CoroutineServerHttpRequest> =
        object : CoroutineBodyExtractor<MultiValueMap<String, String>?, CoroutineServerHttpRequest> {
            override suspend fun extract(serverRequest: CoroutineServerHttpRequest, context: CoroutineBodyExtractor.Context): MultiValueMap<String, String>? {
                val messageReader = messageReader<MultiValueMap<String, String>>(FORM_MAP_TYPE, MediaType.APPLICATION_FORM_URLENCODED, context)
                return context.serverResponse()
                        ?.let {
                            val readSingle = messageReader.readSingle(actualType = FORM_MAP_TYPE, elementType = FORM_MAP_TYPE, request = serverRequest, response = it, hints = context.hints())
                            readSingle
                        }
                        ?: messageReader.readSingle(FORM_MAP_TYPE, serverRequest, context.hints())
            }
        }

private fun <T> messageReader(elementType: org.springframework.core.ResolvableType,
                              mediaType: MediaType, context: CoroutineBodyExtractor.Context): CoroutineHttpMessageReader<T> =
        context.messageReaders().invoke()
                .filter { messageReader -> messageReader.canRead(elementType, mediaType) }
                .firstOrNull()
                ?.let { it as CoroutineHttpMessageReader<T> }
                ?: throw
        IllegalStateException(
                "Could not find HttpMessageReader that supports \"" + mediaType +
                        "\" and \"" + elementType + "\"")

