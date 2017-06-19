package mixit.web.handler

import kotlinx.coroutines.experimental.reactive.open
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.stereotype.Controller
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.ok
import org.springframework.web.coroutine.function.server.body
import reactor.core.publisher.Flux
import java.time.Duration.ofMillis


@Controller
class NewsHandler {

    suspend fun newsView(req: CoroutineServerRequest) = ok().render("news")

    suspend fun newsSse(req: CoroutineServerRequest) = ok()
            .contentType(TEXT_EVENT_STREAM)
            .body(Flux.interval(ofMillis(100)).map { "Hello $it!" }.open())
}
