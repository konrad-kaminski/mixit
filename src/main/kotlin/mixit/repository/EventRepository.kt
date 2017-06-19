package mixit.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mixit.model.*
import mixit.util.coroutine.*
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository


@Repository
class EventRepository(val template: CoroutineMongoTemplate,
                      val objectMapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun initData() {
        if (count() == 0L) {
            val eventsResource = ClassPathResource("data/events.json")
            val events: List<Event> = objectMapper.readValue(eventsResource.inputStream)
            events.forEach { save(it) }
            logger.info("Events data initialization complete")
        }
    }

    suspend fun count() = template.count<Event>()

    suspend fun findAll() = template.find<Event>(Query().with(Sort.by("year")))

    suspend fun findOne(id: String) = template.findById<Event>(id)

    suspend fun save(event: Event) = template.save(event)
}
