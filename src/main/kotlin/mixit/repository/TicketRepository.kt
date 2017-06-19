package mixit.repository

import mixit.model.*
import mixit.util.coroutine.findAll
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.stereotype.Repository


@Repository
class TicketRepository(val template: CoroutineMongoTemplate
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun findAll() = template.findAll<Ticket>()


}
