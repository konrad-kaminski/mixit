package mixit.web.handler

import mixit.repository.TicketRepository
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse

@Component
class TicketingHandler(val repository: TicketRepository) {

    suspend fun ticketing(req: CoroutineServerRequest) = CoroutineServerResponse.ok().render("ticketing-closed", mapOf(Pair("title", "ticketing.title")))

}
