package org.springframework.web.server.session

import mixit.util.coroutine.awaitFirstOrNull
import org.springframework.web.server.CoroutineWebSession
import org.springframework.web.server.WebSession

class DefaultCoroutineWebSession(val session: WebSession): CoroutineWebSession {
    override val attributes: MutableMap<String, Any?>
        get() = session.attributes

    override fun <T> getAttribute(name: String): T? = session.getAttribute<T>(name).orElse(null)

    suspend override fun save(): Unit {
        session.save().awaitFirstOrNull()
    }
}

fun WebSession.asCoroutineWebSession() = DefaultCoroutineWebSession(this)
