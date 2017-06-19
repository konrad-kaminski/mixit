package mixit.web.handler

import mixit.model.*
import mixit.repository.UserRepository
import mixit.util.coroutine.json
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.created
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.ok
import org.springframework.web.coroutine.function.server.body
import org.springframework.web.coroutine.function.server.language
import java.net.URI.*
import java.net.URLDecoder


@Component
class UserHandler(val repository: UserRepository) {

    suspend fun findOneView(req: CoroutineServerRequest) =
            try {
                val idLegacy = req.pathVariable("login")!!.toLong()
                repository.findByLegacyId(idLegacy)?.let {
                    ok().render("user", mapOf(Pair("user", it.toDto(req.language()))))
                }
            } catch (e:NumberFormatException) {
                repository.findOne(URLDecoder.decode(req.pathVariable("login"), "UTF-8"))?.let {
                    ok().render("user", mapOf(Pair("user", it.toDto(req.language()))))
                }
            }

    suspend fun findOne(req: CoroutineServerRequest) = ok().json().body(repository.findOne(req.pathVariable("login")!!))

    suspend fun findAll(req: CoroutineServerRequest) = ok().json().body(repository.findAll(), User::class.java)

    suspend fun findStaff(req: CoroutineServerRequest) = ok().json().body(repository.findByRole(Role.STAFF))

    suspend fun findOneStaff(req: CoroutineServerRequest) = ok().json().body(repository.findOneByRole(req.pathVariable("login")!!, Role.STAFF))

    suspend fun create(req: CoroutineServerRequest) = repository.save(req.body<User>())?.let {
        created(create("/api/user/${it.login}")).json().body(it)
    }

}

class UserDto(
        val login: String,
        val firstname: String,
        val lastname: String,
        var email: String,
        var company: String? = null,
        var description: String,
        var emailHash: String? = null,
        var photoUrl: String? = null,
        val role: Role,
        var links: List<Link>,
        val logoType: String?,
        val logoWebpUrl: String? = null
)

fun User.toDto(language: Language) =
        UserDto(login, firstname, lastname, email ?: "", company, description[language] ?: "",
                emailHash, photoUrl, role, links, logoType(photoUrl), logoWebpUrl(photoUrl))

private fun logoWebpUrl(url: String?) =
        when {
            url == null -> null
            url.endsWith("png") -> url.replace("png", "webp")
            url.endsWith("jpg") -> url.replace("jpg", "webp")
            else -> null
        }

private fun logoType(url: String?) =
        when {
            url == null -> null
            url.endsWith("svg") -> "image/svg+xml"
            url.endsWith("png") -> "image/png"
            url.endsWith("jpg") -> "image/jpeg"
            else -> null
        }
