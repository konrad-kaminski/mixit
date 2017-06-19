package org.springframework.data.mongodb.core

import com.mongodb.client.result.DeleteResult
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import org.springframework.data.mongodb.core.query.Query

interface CoroutineMongoOperations {
    suspend fun count(query: Query, entityClass: Class<*>): Long

    suspend fun remove(query: Query, entityClass: Class<*>): DeleteResult?

    suspend fun <T> find(query: Query, entityClass: Class<T>): ReceiveChannel<T>

    suspend fun <T> findAll(entityClass: Class<T>): ReceiveChannel<T>

    suspend fun <T> findById(id: Any, entityClass: Class<T>): T?

    suspend fun <T> save(objectToSave: T?): T?
}