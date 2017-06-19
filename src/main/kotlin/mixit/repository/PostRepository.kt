package mixit.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import mixit.model.Post
import mixit.model.Language
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import mixit.util.coroutine.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort.Direction.*
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.*


@Repository
class PostRepository(val template: CoroutineMongoTemplate,
                     val objectMapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun initData() {
        if (count() == 0L) {
            val blogResource = ClassPathResource("data/blog.json")
            val posts: List<Post> = objectMapper.readValue(blogResource.inputStream)
            posts.forEach { save(it) }
            logger.info("Blog posts data initialization complete")
        }
    }

    suspend fun count() = template.count<Post>()

    suspend fun findOne(id: String) = template.findById<Post>(id)

    suspend fun findBySlug(slug: String, lang: Language) =
            template.findOne<Post>(Query(where("slug.$lang").`is`(slug)))

    suspend fun findAll(lang: Language? = null): ReceiveChannel<Post> {
        val query = Query()
        query.with(Sort.by(Order(DESC, "addedAt")))
        query.fields().exclude("content")
        if (lang != null) {
            query.addCriteria(where("title.$lang").exists(true))
        }
        return template.find(query)
    }

    suspend fun deleteOne(id: String) = template.remove<Post>(Query(where("_id").`is`(id)))

    suspend fun save(article: Post) = template.save(article)
}
