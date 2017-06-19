package mixit.web

import kotlinx.coroutines.experimental.channels.consumeEach
import mixit.MixitProperties
import mixit.repository.EventRepository
import mixit.util.MarkdownConverter
import mixit.web.handler.*
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.*
import org.springframework.web.coroutine.function.server.CoroutineRenderingResponse
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.ok
import org.springframework.web.coroutine.function.server.router
import mixit.util.coroutine.filter
import mixit.util.coroutine.locale
import org.springframework.web.coroutine.function.server.CoroutineHandlerFunction
import org.springframework.web.reactive.function.server.RouterFunctions.resources
import java.util.Locale


@Configuration
class WebsiteRoutes(val adminHandler: AdminHandler,
                    val authenticationHandler: AuthenticationHandler,
                    val blogHandler: BlogHandler,
                    val globalHandler: GlobalHandler,
                    val newsHandler: NewsHandler,
                    val talkHandler: TalkHandler,
                    val sponsorHandler: SponsorHandler,
                    val ticketingHandler: TicketingHandler,
                    val messageSource: MessageSource,
                    val properties: MixitProperties,
                    val eventRepository: EventRepository,
                    val markdownConverter: MarkdownConverter) {

    private val logger = LoggerFactory.getLogger(WebsiteRoutes::class.java)


    @Bean
    @DependsOn("databaseInitializer")
    fun websiteRouter() = router {
        GET("/blog/feed") { blogHandler.feed(it) }
        accept(TEXT_HTML).nest {
            GET("/") { sponsorHandler.viewWithSponsors("home", null, it) }
            GET("/about") { globalHandler.findAboutView(it) }
            GET("/news") { newsHandler.newsView(it) }
            GET("/ticketing") { ticketingHandler.ticketing(it) }
            GET("/sponsors") { sponsorHandler.viewWithSponsors("sponsors", "sponsors.title", it) }
            GET("/mixteen") { globalHandler.mixteenView(it) }
            GET("/faq") { globalHandler.faqView(it) }
            GET("/come") { globalHandler.comeToMixitView(it) }
            GET("/schedule") { globalHandler.scheduleView(it) }

            // Authentication
            GET("/login") { authenticationHandler.loginView(it) }

            // Talks
            eventRepository.findAll().consumeEach { event ->
                val year = event.year
                GET("/$year") { talkHandler.findByEventView(year, it) }
                GET("/$year/makers") { talkHandler.findByEventView(year, it, "makers") }
                GET("/$year/aliens") { talkHandler.findByEventView(year, it, "aliens") }
                GET("/$year/tech") { talkHandler.findByEventView(year, it, "tech") }
                GET("/$year/design") { talkHandler.findByEventView(year, it, "design") }
                GET("/$year/hacktivism") { talkHandler.findByEventView(year, it, "hacktivism") }
                GET("/$year/learn") { talkHandler.findByEventView(year, it, "learn") }
                GET("/$year/{slug}") { talkHandler.findOneView(year, it) }
            }

            "/admin".nest {
                GET("/") { adminHandler.admin(it) }
                GET("/ticketing") { adminHandler.adminTicketing(it) }
                GET("/talks") { adminHandler.adminTalks(it) }
                DELETE("/")
                GET("/talks/edit/{slug}") { adminHandler.editTalk(it) }
                GET("/talks/create") { adminHandler.createTalk(it) }
                GET("/users") { adminHandler.adminUsers(it) }
                GET("/users/edit/{login}") { adminHandler.editUser(it) }
                GET("/users/create") { adminHandler.createUser(it) }
                GET("/blog") { adminHandler.adminBlog(it) }
                GET("/post/edit/{id}") { adminHandler.editPost(it) }
                GET("/post/create") { adminHandler.createPost(it) }
            }

            "/blog".nest {
                GET("/") { blogHandler.findAllView(it) }
                GET("/{slug}") { blogHandler.findOneView(it) }
            }
        }

        accept(TEXT_EVENT_STREAM).nest {
            GET("/news/sse") { newsHandler.newsSse(it) }
        }

        contentType(APPLICATION_FORM_URLENCODED).nest {
            POST("/login") { authenticationHandler.login(it) }
            //POST("/ticketing", ticketingHandler::submit)
            "/admin".nest {
                POST("/talks") { adminHandler.adminSaveTalk(it) }
                POST("/talks/delete") { adminHandler.adminDeleteTalk(it) }
                POST("/users") { adminHandler.adminSaveUser(it) }
                POST("/users/delete") {adminHandler.adminDeleteUser(it) }
                POST("/post") { adminHandler.adminSavePost(it) }
                POST("/post/delete") { adminHandler.adminDeletePost(it) }
            }
        }

        if (properties.baseUri != "https://mixitconf.org") {
            logger.warn("SEO disabled via robots.txt because ${properties.baseUri} baseUri is not the production one (https://mixitconf.org)")
            GET("/robots.txt") {
                ok().contentType(TEXT_PLAIN).syncBody("User-agent: *\nDisallow: /")
            }
        }
    }.filter { request: CoroutineServerRequest, next: CoroutineHandlerFunction<CoroutineServerResponse>  ->
        val locale : Locale = request.locale()
        val session = request.session()!!
        val path = request.uri().path
        val model = generateModel(properties.baseUri!!, path, locale, session, messageSource, markdownConverter)
                next.invoke(request)?.let { if (it is CoroutineRenderingResponse) CoroutineRenderingResponse.from(it).modelAttributes(model).build() else it }
    }

    @Bean
    @DependsOn("websiteRouter")
    fun resourceRouter() = resources("/**", ClassPathResource("static/"))

}

