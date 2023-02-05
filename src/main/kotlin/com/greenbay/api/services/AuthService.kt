package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils.Companion.APPLICATION_JSON
import com.greenbay.api.utils.BaseUtils.Companion.CONTENT_TYPE
import com.greenbay.api.utils.BaseUtils.Companion.execute
import com.greenbay.api.utils.BaseUtils.Companion.generateJwt
import com.greenbay.api.utils.BaseUtils.Companion.getResponse
import com.greenbay.api.utils.Collections
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthService : TaskService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    fun setAuthRoutes(router: Router) {
        router.post("/api/v1/admin/login").handler(::adminLogin)
        router.post("/api/v1/user/login").handler(::userLogin)
        setTaskRoutes(router)
    }

    private fun adminLogin(rc: RoutingContext) {
        logger.info("adminLogin() -->")
        execute("admin", rc, { usr, body, response ->
            logger.info("[adminLogin]")
            val email = body.getString("email")
            val password = body.getString("password")
            val qry = JsonObject.of("email", email)
            getDatabase().findOne(Collections.ADMIN_TBL.toString(), qry, JsonObject(), {
                logger.info("[adminLogin]")
                val matches = getPasswordEncoder().matches(password, it.getString("password"))
                if (!it.getBoolean("activated")){
                    response.apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .end(getResponse("Account not activated,Check email to activate").encodePrettily())
                }
                if (matches) {
                    val tokens = generateJwt(it.getString("email"))
                    response.apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .end(getResponse("Login successful",tokens).encodePrettily())
                } else {
                    response.apply {
                        statusCode = BAD_REQUEST.code()
                        statusMessage = BAD_REQUEST.reasonPhrase()
                    }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .end(getResponse("Invalid credentials").encodePrettily())
                }
            }, {
                logger.error("[adminLogin] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        })
    }

    private fun userLogin(rc: RoutingContext) {

    }

}