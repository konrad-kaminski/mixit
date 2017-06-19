package mixit.web.handler

import mixit.model.*
import mixit.repository.UserRepository
import mixit.util.coroutine.collectList
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.ok
import org.springframework.web.coroutine.function.server.language
import java.util.*


@Component
class GlobalHandler(val userRepository: UserRepository) {

    suspend fun findAboutView(req: CoroutineServerRequest) = userRepository.findByRole(Role.STAFF).collectList().let {
        val users = it.map { it.toDto(req.language()) }
        Collections.shuffle(users)
        ok().render("about", mapOf(Pair("staff", users), Pair("title", "about.title")))
    }

    suspend fun mixteenView(req: CoroutineServerRequest) = ok().render("mixteen", mapOf(Pair("title", "mixteen.title")))

    suspend fun faqView(req: CoroutineServerRequest) = ok().render("faq", mapOf(Pair("title", "faq.title")))

    suspend fun comeToMixitView(req: CoroutineServerRequest) = ok().render("come", mapOf(Pair("title", "come.title")))

    suspend fun scheduleView(req: CoroutineServerRequest) = ok().render("schedule", mapOf(Pair("title", "schedule.title")))
}

