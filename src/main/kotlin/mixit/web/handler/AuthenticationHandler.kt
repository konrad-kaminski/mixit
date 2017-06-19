package mixit.web.handler

import mixit.MixitProperties
import org.springframework.web.coroutine.function.server.CoroutineServerRequest
import org.springframework.web.coroutine.function.server.CoroutineServerResponse.Companion.ok
import mixit.util.coroutine.seeOther
import org.springframework.stereotype.Component
import org.springframework.web.coroutine.function.toFormData


@Component
class AuthenticationHandler(val properties: MixitProperties) {

    suspend fun loginView(req: CoroutineServerRequest) = ok().render("login")

    suspend fun login(req: CoroutineServerRequest) = req.body(toFormData())?.let { data ->
        req.session()?.let { session ->
            val formData = data.toSingleValueMap()
            if (formData["username"] == properties.admin.username && formData["password"] == properties.admin.password) {
                session.attributes["username"] =  data.toSingleValueMap()["username"]
                seeOther("${properties.baseUri}/admin")
            }
            else ok().render("login-error")
        }
    }
}
