package mixit.web.handler

import mixit.repository.EventRepository
import mixit.util.coroutine.json
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.ok
import org.springframework.web.coroutine.function.server.body


@Component
class EventHandler(val repository: EventRepository) {

    suspend fun findOne(req: CoroutineServerRequest) = ok().json().body(repository.findOne(req.pathVariable("id")!!))

    suspend fun findAll(req: CoroutineServerRequest) = ok().json().body(repository.findAll())

}

