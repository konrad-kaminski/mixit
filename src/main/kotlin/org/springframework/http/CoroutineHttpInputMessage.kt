package org.springframework.http

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import org.springframework.core.io.buffer.DataBuffer

interface CoroutineHttpInputMessage: HttpMessage {
    val body: ReceiveChannel<DataBuffer>
}