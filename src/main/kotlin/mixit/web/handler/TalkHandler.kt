package mixit.web.handler

import mixit.MixitProperties
import mixit.model.Language
import mixit.model.Talk
import mixit.model.TalkFormat
import mixit.model.User
import mixit.repository.TalkRepository
import mixit.repository.UserRepository
import mixit.util.coroutine.collectList
import mixit.util.coroutine.collectMap
import mixit.util.coroutine.permanentRedirect
import mixit.util.formatTalkDate
import mixit.util.formatTalkTime
import mixit.util.coroutine.json
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.ok
import org.springframework.web.coroutine.function.server.body
import org.springframework.web.coroutine.function.server.language
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


@Component
class TalkHandler(val repository: TalkRepository,
                  val userRepository: UserRepository,
                  val properties: MixitProperties) {

    suspend fun findByEventView(year: Int, req: CoroutineServerRequest, topic: String? = null): CoroutineServerResponse? {
        val talks = repository
                .findByEvent(year.toString(), topic)
                .collectList()
                .let { talks ->
                    userRepository
                     .findMany(talks.flatMap(Talk::speakerIds))
                     .collectMap(User::login)
                     .let { speakers -> talks.map { it.toDto(req.language(), it.speakerIds.mapNotNull { speakers[it] }) }.groupBy { it.date } }
                }

        return CoroutineServerResponse.ok().render("talks", mapOf(
                Pair("talks", talks),
                Pair("year", year),
                Pair("title", when(topic) { null -> "talks.title.html|$year" else -> "talks.title.html.$topic|$year" }),
                Pair("baseUri", UriUtils.encode(properties.baseUri, StandardCharsets.UTF_8)),
                Pair("topic", topic),
                Pair("has2Columns", talks.size == 2)
        ))
    }


    suspend fun findOneView(year: Int, req: CoroutineServerRequest) = repository.findByEventAndSlug(year.toString(), req.pathVariable("slug")!!)?.let { talk ->
        userRepository.findMany(talk.speakerIds).collectList().let { speakers ->
        ok().render("talk", mapOf(
                Pair("talk", talk.toDto(req.language(), speakers)),
                Pair("speakers", speakers.map { it.toDto(req.language()) }.sortedBy { talk.speakerIds.indexOf(it.login) }),
                Pair("title", "talk.html.title|${talk.title}"),
                Pair("baseUri", UriUtils.encode(properties.baseUri, StandardCharsets.UTF_8)),
                Pair("vimeoPlayer", if(talk.video?.startsWith("https://vimeo.com/") == true) talk.video.replace("https://vimeo.com/", "https://player.vimeo.com/video/") else null)
        ))
    }}

    suspend fun findOne(req: CoroutineServerRequest) = ok().json().body(repository.findOne(req.pathVariable("login")!!))

    suspend fun findByEventId(req: CoroutineServerRequest) =
            ok().json().body(repository.findByEvent(req.pathVariable("year")!!), Talk::class.java)

    suspend fun redirectFromId(req: CoroutineServerRequest) = repository.findOne(req.pathVariable("id")!!)?.let {
        permanentRedirect("${properties.baseUri}/${it.event}/${it.slug}")
    }

    suspend fun redirectFromSlug(req: CoroutineServerRequest) = repository.findBySlug(req.pathVariable("slug")!!)?.let {
        permanentRedirect("${properties.baseUri}/${it.event}/${it.slug}")
    }

}

class TalkDto(
        val id: String?,
        val slug: String,
        val format: TalkFormat,
        val event: String,
        val title: String,
        val summary: String,
        val speakers: List<User>,
        val language: String,
        val addedAt: LocalDateTime,
        val description: String?,
        val topic: String?,
        val video: String?,
        val room: String?,
        val start: String?,
        val end: String?,
        val date: String?,
        val isEn: Boolean = (language == "english"),
        val isTalk: Boolean = (format == TalkFormat.TALK)
)

fun Talk.toDto(lang: Language, speakers: List<User>) = TalkDto(
        id, slug, format, event, title,
        summary, speakers, language.name.toLowerCase(), addedAt,
        description, topic,
        video, "rooms.${room?.name?.toLowerCase()}" , start?.formatTalkTime(lang), end?.formatTalkTime(lang),
        start?.formatTalkDate(lang)
)
