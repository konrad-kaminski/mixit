package mixit.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import mixit.model.Role
import mixit.model.User
import org.springframework.core.io.ClassPathResource
import org.springframework.data.mongodb.core.CoroutineMongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import mixit.util.coroutine.*
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Criteria.*


@Repository
class UserRepository(val template: CoroutineMongoTemplate,
                     val objectMapper: ObjectMapper) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun initData() {
        if (count() == 0L) {
            val usersResource = ClassPathResource("data/users.json")
            val users: List<User> = objectMapper.readValue(usersResource.inputStream)
            users.forEach { save(it) }
            logger.info("Users data initialization complete")
        }
    }

    suspend fun count() = template.count<User>()

    suspend fun findByRole(role: Role): ReceiveChannel<User> =
            template.find<User>(Query(where("role").`is`(role)))

    suspend fun findOneByRole(login: String, role: Role) =
        template.findOne<User>(Query(where("role").`in`(role).and("_id").`is`(login)))

    suspend fun findAll() = template.findAll<User>()

    suspend fun findOne(login: String) = template.findById<User>(login)

    suspend fun findMany(logins: List<String>): ReceiveChannel<User> = template.find<User>(Query(where("_id").`in`(logins)))

    suspend fun findByLegacyId(id: Long) =
            template.findOne<User>(Query(where("legacyId").`is`(id)))

    suspend fun deleteOne(login: String) = template.remove<User>(Query(where("_id").`is`(login)))

    suspend fun save(user: User?) = template.save(user)

}
