package org.springframework.data.mongodb.core

import com.mongodb.client.result.DeleteResult
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.reactive.awaitSingle
import kotlinx.coroutines.experimental.reactive.open
import mixit.util.coroutine.awaitFirstOrNull
import org.springframework.data.mongodb.core.query.Query

open class CoroutineMongoTemplate(
    private val template: ReactiveMongoTemplate
): CoroutineMongoOperations {
    suspend override fun count(query: Query, entityClass: Class<*>): Long =
        template.count(query, entityClass).awaitSingle()

    suspend override fun remove(query: Query, entityClass: Class<*>): DeleteResult? =
        template.remove(query, entityClass).awaitFirstOrNull()

    override suspend fun <T> find(query: Query, entityClass: Class<T>): ReceiveChannel<T> =
        template.find(query, entityClass).open()

    suspend override fun <T> findAll(entityClass: Class<T>): ReceiveChannel<T> =
        template.findAll(entityClass).open()

    suspend override fun <T> findById(id: Any, entityClass: Class<T>): T? =
        template.findById(id, entityClass).awaitFirstOrNull()

    suspend override fun <T> save(objectToSave: T?): T? =
            objectToSave?.let {
                template.save(objectToSave).awaitFirstOrNull()
            }
}