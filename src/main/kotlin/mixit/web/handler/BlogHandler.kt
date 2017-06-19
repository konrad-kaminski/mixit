package mixit.web.handler

import mixit.MixitProperties
import mixit.model.*
import mixit.model.Language.*
import mixit.repository.PostRepository
import mixit.repository.UserRepository
import mixit.util.*
import mixit.util.coroutine.collectList
import mixit.util.coroutine.collectMap
import org.springframework.web.coroutine.function.server.body
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.ok
import mixit.util.coroutine.permanentRedirect
import mixit.util.coroutine.json
import org.springframework.http.MediaType.*
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.server.language


@Component
class BlogHandler(val repository: PostRepository,
                  val userRepository: UserRepository,
                  val properties: MixitProperties) {

    suspend fun findOneView(req: CoroutineServerRequest) = repository.findBySlug(req.pathVariable("slug")!!, req.language())
            ?.let { post ->
                userRepository.findOne(post.authorId)?.let { author ->
                    val model = mapOf(Pair("post", post.toDto(author, req.language())), Pair("title", "blog.post.title|${post.title[req.language()]}"))
                    ok().render("post", model)
                }
            } ?: repository.findBySlug(req.pathVariable("slug")!!, if (req.language() == FRENCH) ENGLISH else FRENCH)?.let {
                permanentRedirect("${properties.baseUri}${if (req.language() == ENGLISH) "/en" else ""}/blog/${it.slug[req.language()]}")
            }

    suspend fun findAllView(req: CoroutineServerRequest) = repository.findAll(req.language())
            .collectList()
            .let { posts -> userRepository.findMany(posts.map { it.authorId }).collectMap{ it.login }.let { authors ->
                val model = mapOf(Pair("posts", posts.map { it.toDto(authors[it.authorId]!!, req.language()) }), Pair("title", "blog.title"))
                ok().render("blog", model)
            }}

    suspend fun findOne(req: CoroutineServerRequest) = ok().json().body(repository.findOne(req.pathVariable("id")!!))

    suspend fun findAll(req: CoroutineServerRequest) = ok().json().body(repository.findAll())

    suspend fun redirect(req: CoroutineServerRequest) = repository.findOne(req.pathVariable("id")!!)?.let {
        permanentRedirect("${properties.baseUri}/blog/${it.slug[req.language()]}")
    }

    suspend fun feed(req: CoroutineServerRequest) = ok().contentType(APPLICATION_ATOM_XML).render("feed", mapOf(Pair("feed", repository.findAll(req.language()).collectList().let { it.toFeed(req.language(), "blog.feed.title", "/blog") })))

}

class PostDto(
        val id: String?,
        val slug: String,
        val author: User,
        val addedAt: String,
        val title: String,
        val headline: String,
        val content: String?
)

fun Post.toDto(author: User, language: Language) = PostDto(
        id,
        slug[language] ?: "",
        author,
        addedAt.formatDate(language),
        title[language] ?: "",
        headline[language] ?: "",
        if (content != null) content[language] else  null)

class Feed(
        val title: String,
        val link: String,
        val updated: String,
        val entries: List<FeedEntry>
)

class FeedEntry(
        val id: String,
        val title: String,
        val link: String,
        val updated: String
)

fun Post.toFeedEntry(language: Language) = FeedEntry(
        id!!,
        title[language]!!,
        slug[language]!!,
        addedAt.toRFC3339()
)

fun List<Post>.toFeed(language: Language, title: String, link: String)= Feed(
        title,
        link,
        first().addedAt.toRFC3339(),
        map { it.toFeedEntry(language) }
)