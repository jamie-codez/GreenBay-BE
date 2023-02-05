package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils
import com.greenbay.api.utils.BaseUtils.Companion.execute
import com.greenbay.api.utils.Collections
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.*

open class AppUserService : AdminService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    fun setAppUserRoutes(router: Router) {
        router.post("/api/v1/users").handler(::createUser)
        router.get("/api/v1/users").handler(::getUsers)
        router.get("/api/v1/users/:email").handler(::updateUser)
        router.delete("/api/v1/users/:email").handler(::deleteUser)
        setAdminRoutes(router)
    }

    private fun createUser(rc: RoutingContext) {
        logger.info("createUser() -->")
        execute("createUser", rc, { usr, body, response ->
            val encodedPass = getPasswordEncoder().encode(body.getString("password"))
            body.remove("password")
            body.put("password", encodedPass)
            body.put("activated",false)
            body.put("roles", JsonArray.of("user"))
            getDatabase().save(Collections.USER_TBL.toString(), body, {
                logger.error("[createUser]")
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("User created successfully").encodePrettily())
                emailTask(rc,body)
            }, {
                logger.error("[createUser] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }

    private fun emailTask(rc: RoutingContext, body: JsonObject) {
        logger.info("emailTask() -->")
        val uuid = UUID.randomUUID().toString()
        val email = body.getString("email")
        val address: String = rc.request().localAddress().hostAddress()
        val port: Int = rc.request().localAddress().port()
        val link = "http://$address:$port/api/v1/user/activate/$uuid"
        val html = "<a href=\"$link\"> here </a>"
        val mailBody =
            "Hello ${body.getString("username")}, Welcome to Green Bay,You have been registered as a user in the system click $html to activate your account account🥳."
        val doc = JsonObject.of("email", email, "code", uuid)
        getDatabase().save(Collections.USER_ACTIVATION_CODES.toString(), doc, {
            getEmailClient().sendMail(email, "Account Activation", body = mailBody, "", {
                rc.response().apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Activation mail sent successfully").encodePrettily())
                logger.info("emailTask() <--")
            }, {
                logger.error("Failed to send email to $email")
                rc.response().apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error sending mail to user try again").encodePrettily())
            })
            logger.info("emailTask() <--")
        }, {
            rc.response().apply {
                statusCode = INTERNAL_SERVER_ERROR.code()
                statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
            }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                .end(BaseUtils.getResponse("Error occurred activation email could not be sent").encodePrettily())
            logger.info("emailTask() <--")
        })
    }

    private fun getUsers(rc: RoutingContext) {
        logger.info("getUsers() -->")
        execute("getUsers", rc, { usr, body, response ->
            getDatabase().find(Collections.USER_TBL.toString(), JsonObject(), {
                logger.error("[updateUser]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Successful", it).encodePrettily())
            }, {
                logger.error("[updateUser] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }

    private fun updateUser(rc: RoutingContext) {
        execute("updateUser", rc, { usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email", email)
            if (body.containsKey("password")) {
                val encodedPass = getPasswordEncoder().encode(body.getString("password"))
                body.remove("password")
                body.put("password", encodedPass)
            }
            val replacement = JsonObject.of("\$set", body)
            getDatabase().findOneAndUpdate(Collections.USER_TBL.toString(), qry, replacement, {
                logger.error("[updateUser]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Successful", it).encodePrettily())
            }, {
                logger.error("[updateUser] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin", "user")
    }

    private fun deleteUser(rc: RoutingContext) {
        execute("deleteUser", rc, { usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email", email)
            getDatabase().findOneAndDelete(Collections.USER_TBL.toString(), qry, {
                logger.error("[deleteUser]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            }, {
                logger.error("[deleteUser] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }
}