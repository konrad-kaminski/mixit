package mixit

import mixit.util.run
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Mustache.TemplateLoader
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.data.mongodb.core.ReactiveMongoTemplate


@SpringBootApplication
@EnableConfigurationProperties(MixitProperties::class)
class MixitApplication {

    @Bean
    fun mustacheCompiler(templateLoader: TemplateLoader) =
            // TODO Find a way to disable HTML escaping before enabling user authentication
            Mustache.compiler().escapeHTML(false).withLoader(templateLoader)

    @Bean
    fun coroutineMongoTemplate(reactiveMongoTemplate: ReactiveMongoTemplate) = CoroutineMongoTemplate(reactiveMongoTemplate)
}

fun main(args: Array<String>) {
    run(MixitApplication::class, *args)
}
