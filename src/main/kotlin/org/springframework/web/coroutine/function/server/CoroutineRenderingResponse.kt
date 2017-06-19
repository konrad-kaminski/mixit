package org.springframework.web.coroutine.function.server

import org.springframework.web.reactive.function.server.RenderingResponse

interface CoroutineRenderingResponse: CoroutineServerResponse {
    companion object {
        fun from(other: CoroutineRenderingResponse): Builder =
                DefaultCoroutineRenderingResponseBuilder(RenderingResponse.from(other.extractServerResponse() as RenderingResponse))

        operator fun invoke(resp: RenderingResponse): CoroutineRenderingResponse = DefaultCoroutineRenderingResponse(resp)
    }

    interface Builder {
        suspend fun build(): CoroutineRenderingResponse

        fun modelAttributes(attributes: Map<String, *>): Builder
    }
}