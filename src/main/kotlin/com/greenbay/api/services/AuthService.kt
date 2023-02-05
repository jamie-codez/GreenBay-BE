package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils.Companion.APPLICATION_JSON
import com.greenbay.api.utils.BaseUtils.Companion.CONTENT_TYPE
import com.greenbay.api.utils.BaseUtils.Companion.execute
import com.greenbay.api.utils.BaseUtils.Companion.generateJwt
import com.greenbay.api.utils.BaseUtils.Companion.getResponse
import com.greenbay.api.utils.Collections
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.buffer.Buffer
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

open class AuthService : TaskService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    fun setAuthRoutes(router: Router) {
        router.post("/api/v1/admin/login").handler(::adminLogin)
        router.post("/api/v1/user/login").handler(::userLogin)
        router.get("/api/v1/admin/logout/:email").handler(::adminLogout)
        router.get("/api/v1/user/logout/:email").handler(::userLogout)
        router.post("/api/v1/admin/password/reset").handler(::adminPasswordReset)
        router.post("/api/v1/user/password/reset").handler(::userPasswordReset)
        router.post("/api/v1/admin/reset/password/:email").handler(::resetAdminPassword)
        router.post("/api/v1/user/reset/password/:email").handler(::resetUserPassword)
        router.post("/api/v1/user/reset/page").handler(::sendPage)
        router.post("/api/v1/a/admin/reset/page").handler(::sendPage)
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
                if (!it.getBoolean("activated")) {
                    response.apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .end(getResponse("Account not activated,Check email to activate").encodePrettily())
                }
                if (matches) {
                    val tokens = generateJwt(it.getString("email"))
                    val doc = JsonObject.of("email", email, "refresh-token", tokens.getString("refresh-token"))
                    getDatabase().save(Collections.ADMIN_REFRESH_TOKEN_TBL.toString(), doc, {
                        response.apply {
                            statusCode = OK.code()
                            statusMessage = OK.reasonPhrase()
                        }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .end(getResponse("Login successful", tokens).encodePrettily())
                    }, {
                        response.apply {
                            statusCode = INTERNAL_SERVER_ERROR.code()
                            statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                        }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .end(getResponse("Error occurred try again").encodePrettily())
                    })
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
        logger.info("userLogin() -->")
        execute("userLogin", rc, { usr, body, response ->
            logger.info("[userLogin]")
            val email = body.getString("email")
            val password = body.getString("password")
            val qry = JsonObject.of("email", email)
            getDatabase().findOne(Collections.USER_TBL.toString(), qry, JsonObject(), {
                logger.info("[userLogin]")
                val matches = getPasswordEncoder().matches(password, it.getString("password"))
                if (!it.getBoolean("activated")) {
                    response.apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .end(getResponse("Account not activated,Check email to activate").encodePrettily())
                }
                if (matches) {
                    val tokens = generateJwt(it.getString("email"))
                    val doc = JsonObject.of("email", email, "refresh-token", tokens.getString("refresh-token"))
                    getDatabase().save(Collections.ADMIN_REFRESH_TOKEN_TBL.toString(), doc, {
                        response.apply {
                            statusCode = OK.code()
                            statusMessage = OK.reasonPhrase()
                        }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .end(getResponse("Login successful", tokens).encodePrettily())
                    }, {
                        response.apply {
                            statusCode = INTERNAL_SERVER_ERROR.code()
                            statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                        }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .end(getResponse("Error occurred try again").encodePrettily())
                    })
                } else {
                    response.apply {
                        statusCode = BAD_REQUEST.code()
                        statusMessage = BAD_REQUEST.reasonPhrase()
                    }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .end(getResponse("Invalid credentials").encodePrettily())
                }
            }, {
                logger.error("[userLogin] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        })
    }

    private fun adminLogout(rc: RoutingContext) {
        execute("adminLogout", rc, { usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email", email)
            getDatabase().findOne(Collections.ADMIN_REFRESH_TOKEN_TBL.toString(), qry,
                JsonObject(), {
                    if (!it.isEmpty) {
                        getDatabase().findOneAndDelete(Collections.ADMIN_REFRESH_TOKEN_TBL.toString(), qry, {
                            response.apply {
                                statusCode = OK.code()
                                statusMessage = OK.reasonPhrase()
                            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .end(getResponse("Logout successful").encodePrettily())
                        }, {
                            response.apply {
                                statusCode = INTERNAL_SERVER_ERROR.code()
                                statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .end(getResponse("An error occurred try again").encodePrettily())
                        })
                    } else {
                        response.apply {
                            statusCode = FORBIDDEN.code()
                            statusMessage = FORBIDDEN.reasonPhrase()
                        }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .end(getResponse("Something does not seem right").encodePrettily())
                    }
                }, {

                })
        }, "admin", "super-admin")
    }

    private fun userLogout(rc: RoutingContext) {
        logger.info("userLogout() -->")
        execute("userLogout", rc, { usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email", email)
            getDatabase().findOne(Collections.USER_REFRESH_TOKEN_TBL.toString(), qry,
                JsonObject(), {
                    if (!it.isEmpty) {
                        getDatabase().findOneAndDelete(Collections.USER_REFRESH_TOKEN_TBL.toString(), qry, {
                            response.apply {
                                statusCode = OK.code()
                                statusMessage = OK.reasonPhrase()
                            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .end(getResponse("Logout successful").encodePrettily())
                        }, {
                            response.apply {
                                statusCode = INTERNAL_SERVER_ERROR.code()
                                statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .end(getResponse("An error occurred try again").encodePrettily())
                        })
                    } else {
                        response.apply {
                            statusCode = FORBIDDEN.code()
                            statusMessage = FORBIDDEN.reasonPhrase()
                        }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .end(getResponse("Something does not seem right").encodePrettily())
                    }
                }, {

                })
        }, "admin", "super-admin")
    }
    private fun sendPage(rc: RoutingContext) {
        rc.response().setStatusCode(200).sendFile("src/main/resources/static/passwordreset.html")
    }

    private fun adminPasswordReset(rc: RoutingContext) {
        val body = rc.body().asJsonObject()
        getDatabase().findOne(Collections.USER_TBL.toString(),
            JsonObject().put("email", body.getString("email")),JsonObject(),{
                if (it.isEmpty) {
                    rc.response()
                        .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .setStatusCode(400)
                        .end(JsonObject().put("message", "Error occurred").encodePrettily())
                } else {
                    val encryptedPass = JsonObject()
                        .put("password", BCryptPasswordEncoder().encode(body.getValue("password").toString()))
                    getDatabase().findOneAndUpdate(Collections.ADMIN_TBL.toString(),
                        JsonObject().put("email", body.getString("email")),
                        JsonObject().put("\$set", encryptedPass),
                        {
                            rc.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(OK.code())
                                .setStatusMessage(OK.reasonPhrase())
                                .end(
                                    getResponse("Password reset successfully").encodePrettily()
                                )
                        },{
                            rc.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(400)
                                .end(getResponse("Error occurred try again").encodePrettily())
                        })
                }
            },{
                rc.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(INTERNAL_SERVER_ERROR.code())
                    .setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase())
                    .end(getResponse("Error occurred try again").encodePrettily())
            })

    }

    private fun userPasswordReset(rc: RoutingContext) {
        val email = rc.body().asJsonObject().getString("email")
        val password = rc.body().asJsonObject().getString("password")
        getDatabase().findOne(Collections.USER_TBL.toString(),
            JsonObject().put("email", email),JsonObject(),{
                if (it.isEmpty) {
                    rc.response()
                        .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .setStatusCode(400)
                        .end(JsonObject().put("message", "Error occurred").encodePrettily())
                } else {
                    val encryptedPass = JsonObject()
                        .put("password", BCryptPasswordEncoder().encode(password).toString())
                    getDatabase().findOneAndUpdate(Collections.USER_TBL.toString(),
                        JsonObject().put("email", email),
                        JsonObject().put("\$set", encryptedPass),
                        {
                            rc.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(OK.code())
                                .setStatusMessage(OK.reasonPhrase())
                                .end(
                                    getResponse("Password reset successfully").encodePrettily()
                                )
                        },{
                            rc.response()
                                .putHeader("content-type", "application/json")
                                .setStatusCode(INTERNAL_SERVER_ERROR.code())
                                .setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase())
                                .end(getResponse("Error occurred try again").encodePrettily())
                        })
                }
            },{
                rc.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(INTERNAL_SERVER_ERROR.code())
                    .setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase())
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
    }

    private fun resetUserPassword(rc: RoutingContext) {
            val reqBody = rc.body().asJsonObject()
            val email = rc.request().getParam("email")
            getDatabase().findOne(Collections.USER_TBL.toString(),
                JsonObject().put("email", email),JsonObject(),{
                    if (it.isEmpty) {
                        rc.response()
                            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .setStatusCode(400)
                            .end(JsonObject().put("message", "Error occurred").encodePrettily())
                    } else {
                        val encryptedPass = JsonObject()
                            .put("password", BCryptPasswordEncoder().encode(reqBody.getValue("password").toString()))
                        getDatabase().findOneAndUpdate(Collections.USER_TBL.toString(),
                            JsonObject().put("email", email),
                            JsonObject().put("\$set", encryptedPass),
                            {
                                rc.response()
                                    .putHeader("content-type", "application/json")
                                    .setStatusCode(OK.code())
                                    .setStatusMessage(OK.reasonPhrase())
                                    .end(
                                        getResponse("Password reset successfully").encodePrettily()
                                    )
                            },{
                                rc.response()
                                    .putHeader("content-type", "application/json")
                                    .setStatusCode(400)
                                    .end(getResponse("Error occurred try again").encodePrettily())
                            })
                    }
                },{
                    rc.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(INTERNAL_SERVER_ERROR.code())
                        .setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase())
                        .end(getResponse("Error occurred try again").encodePrettily())
                })
    }

    private fun resetAdminPassword(rc: RoutingContext) {
        rc.request().bodyHandler { handler: Buffer ->
            val reqBody = handler.toJsonObject()
            val email = rc.request().getParam("email")
            getDatabase().findOne(Collections.ADMIN_TBL.toString(),
                JsonObject().put("email", email),JsonObject(),{
                    if (it.isEmpty) {
                        rc.response()
                            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .setStatusCode(400)
                            .end(JsonObject().put("message", "Error occurred").encodePrettily())
                    } else {
                        val encryptedPass = JsonObject()
                            .put("password", BCryptPasswordEncoder().encode(reqBody.getValue("password").toString()))
                        getDatabase().findOneAndUpdate(Collections.ADMIN_TBL.toString(),
                            JsonObject().put("email", email),
                            JsonObject().put("\$set", encryptedPass),
                            {
                                rc.response()
                                    .putHeader("content-type", "application/json")
                                    .setStatusCode(OK.code())
                                    .setStatusMessage(OK.reasonPhrase())
                                    .end(
                                        getResponse("Password reset successfully").encodePrettily()
                                    )
                            },{
                                rc.response()
                                    .putHeader("content-type", "application/json")
                                    .setStatusCode(400)
                                    .end(getResponse("Error occurred try again").encodePrettily())
                            })
                    }
                },{
                    rc.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(INTERNAL_SERVER_ERROR.code())
                        .setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase())
                        .end(getResponse("Error occurred try again").encodePrettily())
                })
        }
    }


}