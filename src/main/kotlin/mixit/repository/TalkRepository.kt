package mixit.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import mixit.model.Talk
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import mixit.util.coroutine.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort.*
import org.springframework.data.domain.Sort.Direction.*
import org.springframework.data.mongodb.core.query.Criteria.*


@Repository
class TalkRepository(val template: CoroutineMongoTemplate,
                     val objectMapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun initData() {
        if (count() == 0L) {
            listOf(2012, 2013, 2014, 2015, 2016, 2017).forEach { year ->
                val talksResource = ClassPathResource("data/talks_$year.json")
                val talks: List<Talk> = objectMapper.readValue(talksResource.inputStream)
                talks.forEach { save(it) }
            }
            logger.info("Talks data initialization complete")
        }
    }

    suspend fun count() = template.count<Talk>()

    suspend fun findByEvent(eventId: String, topic: String? = null): ReceiveChannel<Talk> {
        val criteria = where("event").`is`(eventId)
        if (topic != null) criteria.and("topic").`is`(topic)
        return template.find<Talk>(Query(criteria).with(by(Order(ASC, "start"))))
    }


    suspend fun findOne(id: String) = template.findById<Talk>(id)

    suspend fun findBySlug(slug: String) =
            template.findOne<Talk>(Query(where("slug").`is`(slug)))

    suspend fun findByEventAndSlug(eventId: String, slug: String): Talk? =
            template.findOne<Talk>(Query(where("slug").`is`(slug).and("event").`is`(eventId)))

    suspend fun deleteOne(id: String) = template.remove<Talk>(Query(where("_id").`is`(id)))

    suspend fun save(talk: Talk) = template.save(talk)

}
