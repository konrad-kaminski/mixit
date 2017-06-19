package org.springframework.http.server

import org.springframework.http.CoroutineHttpOutputMessage
import org.springframework.http.HttpStatus

interface CoroutineServerHttpResponse: CoroutineHttpOutputMessage {
    var statusCode: HttpStatus
}