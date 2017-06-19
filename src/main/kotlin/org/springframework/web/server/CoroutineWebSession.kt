package org.springframework.web.server

interface CoroutineWebSession {
    val attributes: MutableMap<String, Any?>

    fun <T> getAttribute(name: String): T?

    suspend fun save(): Unit
}