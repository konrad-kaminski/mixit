package mixit.web

import mixit.web.handler.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.coroutine.function.server.router
import org.springframework.http.MediaType.APPLICATION_JSON


@Configuration
class ApiRoutes(val blogHandler: BlogHandler,
                 val eventHandler: EventHandler,
                 val talkHandler: TalkHandler,
                 val userHandler: UserHandler) {

    @Bean
    fun apiRouter() = router {
        (accept(APPLICATION_JSON) and "/api").nest {
            "/blog".nest {
                GET("/") { blogHandler.findAll(it) }
                GET("/{id}") { blogHandler.findOne(it) }
            }

            "/event".nest {
                GET("/") { eventHandler.findAll(it) }
                GET("/{id}") { eventHandler.findOne(it) }
            }


            // Talks
            GET("/talk/{login}") { talkHandler.findOne(it) }
            GET("/{year}/talk") { talkHandler.findByEventId(it) }

            // users
            "/user".nest {
                GET("/") { userHandler.findAll(it) }
                POST("/") { userHandler.create(it) }
                GET("/{login}") { userHandler.findOne(it) }
            }
            "/staff".nest {
                GET("/") { userHandler.findStaff(it) }
                GET("/{login}") { userHandler.findOneStaff(it) }
            }
        }
    }
}
