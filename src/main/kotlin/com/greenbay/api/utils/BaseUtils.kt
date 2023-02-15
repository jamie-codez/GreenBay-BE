package com.greenbay.api.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.greenbay.api.exception.GreenBayException
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*
import java.util.concurrent.TimeUnit

class BaseUtils {
    companion object {
        private val logger = LoggerFactory.getLogger(Companion::class.java.simpleName)
        const val CONTENT_TYPE = "content-type"
        const val APPLICATION_JSON = "application/json"
        const val BODY_LIMIT = 5_000
        const val URL_ENCODED = "url-encoded"
        val database = DatabaseUtils(Vertx.vertx())
        fun isNotNull(jsonObject: JsonObject): Boolean {
            if (jsonObject.isEmpty) {
                throwError("json is empty")
                return false
            }
            return true
        }

        fun isNotNull(string: String): Boolean {
            if (string.trim().isEmpty()) {
                throwError("String is empty")
                return false
            }
            return true
        }

        @Throws(GreenBayException::class)
        fun throwError(message: String) {
            throw GreenBayException(message)
        }


        fun validateAdminBody(jsonObject: JsonObject): Boolean {
            if (jsonObject.containsKey("firstName")
                && jsonObject.containsKey("lastName")
                && jsonObject.containsKey("email")
                && jsonObject.containsKey("phone")
                && jsonObject.containsKey("idNo")
                && jsonObject.containsKey("password")
            ) {
                return true
            }
            return false
        }

        fun generateJwt(email: String): JsonObject {
            val accessToken = JWT.create().withIssuer(System.getenv("GB_JWT_ISSUER")).withSubject(email)
                .withExpiresAt(Date(60 * 60 * 24 * 7 * 1000L * System.currentTimeMillis())).sign(
                    Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))
                )
            val refreshToken = JWT.create().withIssuer(System.getenv("GB_JWT_ISSUER")).withSubject(email)
                .withExpiresAt(Date(60 * 60 * 24 * 30 * 1000L * System.currentTimeMillis())).sign(
                    Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))
                )
            return JsonObject.of("access-token", accessToken, "refresh-token", refreshToken)
        }

        fun isExpired(jwt: String): Boolean {
            val decodedJWT = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET")))
                .withIssuer(System.getenv("GB_JWT_ISSUER")).build()
            val expiresAt: Date = verifier.verify(decodedJWT).expiresAt
            return System.currentTimeMillis() > expiresAt.time
        }

        fun verifyIsAdmin(jwt: String): Boolean {
            var isAdmin = false
            val decodedJWT = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET")))
                .withIssuer(System.getenv("GB_JWT_ISSUER")).build()
            val email = verifier.verify(decodedJWT).subject
            val qry = JsonObject.of("email", email)
            DatabaseUtils(Vertx.vertx()).findOne(Collections.ADMIN_TBL.toString(), qry, JsonObject(), {
                isAdmin = it.getJsonArray("roles").contains("admin")
            }, {
                isAdmin = false
                throwError(it.message!!)
            })
            return isAdmin
        }

        fun decodeJwt(jwt: String): JsonObject {
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET")))
                .withIssuer(System.getenv("GB_JWT_ISSUER")).build()
            val payload: String = verifier.verify(jwt).payload
            return JsonObject(payload)
        }

        fun checkHasRole(payload: JsonObject, vararg role: String): Boolean =
            payload.getJsonArray("roles").contains(role)

        fun isValidJwt(jwt: String): Boolean {
            val decodedJwt = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET")))
                .withIssuer(System.getenv("GB_JWT_ISSUER")).build()
            return verifier.verify(decodedJwt).issuer == System.getenv("GB_JWT_ISSUER")
        }

        fun isPermitted(
            jwt: String,
            vararg role: String,
            response: HttpServerResponse,
            task: (email: String, response: HttpServerResponse) -> Unit
        ) {
            val payload = decodeJwt(jwt)
            if (!isValidJwt(jwt)) {
                response.apply {
                    statusCode = BAD_REQUEST.code()
                    statusMessage = BAD_REQUEST.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse(FORBIDDEN.code(), "Invalid access token").encodePrettily())
            } else if (!checkHasRole(payload, *role)) {
                response.apply {
                    statusCode = FORBIDDEN.code()
                    statusMessage = FORBIDDEN.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse(UNAUTHORIZED.code(), "Not enough permissions").encodePrettily())
            } else if (isExpired(jwt)) {
                response.apply {
                    statusCode = BAD_REQUEST.code()
                    statusMessage = BAD_REQUEST.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse(UNAUTHORIZED.code(), "Access token expired").encodePrettily())
            } else {
                task(payload.getString("subject"), response)
            }
        }

        fun execute(
            task: String,
            context: RoutingContext,
            inject: (usr: JsonObject, body: JsonObject, response: HttpServerResponse) -> Unit,
            vararg values: String
        ) {
            logger.info("execute($task) -->")
            try {
                val requestBody = context.body().asJsonObject()
                val accessToken = context.request().getHeader("access-token")
                if (!accessToken.isNullOrEmpty()) {
                    bodyHandler(task, requestBody, accessToken, context, inject, *values)
                } else {
                    logger.error("Access-Token not found ()-> $requestBody")
                    context.response().apply {
                        statusCode = UNAUTHORIZED.code()
                        statusMessage = UNAUTHORIZED.reasonPhrase()
                    }
                }
            } catch (ex: Exception) {
                logger.error("[$task] ${ex.message}", ex)
                context.response().apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(this.getResponse(INTERNAL_SERVER_ERROR.code(), ex.message!!).encodePrettily())
            }
        }

        fun authExecute(
            task: String,
            requestBody: JsonObject,
            context: RoutingContext,
            inject: (usr: JsonObject, body: JsonObject, response: HttpServerResponse) -> Unit,
            vararg values: String
        ) {
            logger.info("executeAuth($task) -->")
            val response = context.response().apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
            try {
                val accessToken = context.request().getHeader("access-token")
                authBodyHandler(task, requestBody, accessToken, context, inject, *values)
                logger.info("executeAuth($task) <--")
            } catch (ex: Exception) {
                logger.error("executeAuth([${ex.message}]) <--")
                response.end(getResponse(INTERNAL_SERVER_ERROR.code(), ex.message!!).encodePrettily())
            }
        }


        fun authBodyHandler(
            task: String,
            body: JsonObject,
            accessToken: String,
            rc: RoutingContext,
            inject: (usr: JsonObject, body: JsonObject, response: HttpServerResponse) -> Unit,
            vararg values: String,
        ) {
            logger.info("authBodyHandler($task) -->")
            val response = rc.response().apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
            val size = body.encode().length
            if (size > BODY_LIMIT) {
                response.end(
                    getResponse(
                        REQUEST_ENTITY_TOO_LARGE.code(),
                        "Body too large. [${size / 1025}]"
                    ).encodePrettily()
                )
            } else {
                if (accessToken.isEmpty()) {
                    response.end(getResponse(BAD_REQUEST.code(), "Access Token missing").encodePrettily())
                    return
                }
                val claim = decodeJwtToken(accessToken)
                val email = claim.getString("email")
                val expires = claim.getString("expires")
                val jwtRoles = claim.getJsonArray("roles")
                val now = System.currentTimeMillis()
                if (now > expires.toLong()) {
                    response.end(getResponse(UNAUTHORIZED.code(), "Access token expired").encodePrettily())
                    return
                }
                getUser(JsonObject.of("email", email), { usr ->
                    val userRoles = usr.getJsonArray("roles")
                    if (!hasFields(body, *values)){
                        response.end(getResponse(NOT_FOUND.code(),"Some fields missing").encodePrettily())
                    }
                    if (!hasPermissions(jwtRoles,userRoles)){
                        response.end(getResponse(UNAUTHORIZED.code(),"Missing permissions").encodePrettily())
                    }
                    inject(usr,body, response)
                }, response)
            }
        }


        fun decodeJwtToken(jwt: String): JsonObject {
            try {
                val decodedJWT = JWT.decode(jwt)
                val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET")))
                    .withIssuer(System.getenv("GB_JWT_ISSUER")).build()
                val verifiedJWT = verifier.verify(decodedJWT)
                val email = verifiedJWT.subject
                val expires = verifiedJWT.expiresAt
                val roles = verifiedJWT.claims["roles"]
                return JsonObject.of("email", email, "expires", expires, "roles", roles)
            } catch (ex: Exception) {
                throw GreenBayException(ex.message, ex)
            }
        }

        fun getUser(qry: JsonObject, task: (usr: JsonObject) -> Unit, response: HttpServerResponse) {
            database.findOne(Collections.USER_TBL.toString(), qry, JsonObject(), {
                task(it)
            }, {
                response.end(getResponse(INTERNAL_SERVER_ERROR.code(), "Error occurred try again.").encodePrettily())
                throw GreenBayException(it.message, it)
            })
        }


        fun bodyHandler(
            task: String,
            requestBody: JsonObject,
            accessToken: String,
            context: RoutingContext,
            inject: (usr: JsonObject, body: JsonObject, response: HttpServerResponse) -> Unit,
            vararg values: String
        ) {
            logger.info("bodyHandler($task) -->")
            if (requestBody.encode().length > BODY_LIMIT) {
                context.response().apply {
                    statusCode = REQUEST_ENTITY_TOO_LARGE.code()
                    statusMessage = REQUEST_ENTITY_TOO_LARGE.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(
                        getResponse(
                            REQUEST_ENTITY_TOO_LARGE.code(),
                            "Request body too large: [${requestBody.encodePrettily().length}]"
                        ).encodePrettily()
                    )
            } else {
                this.isPermitted(accessToken, *values, response = context.response()) { email, response ->
                    inject(JsonObject.of("email", email), requestBody, response)
                }
            }

        }

        fun noAuthExecute(
            task: String,
            rc: RoutingContext,
            inject: (usr: JsonObject, body: JsonObject, response: HttpServerResponse) -> Unit,
            values: String
        ) {
            logger.info("noAuthExecute($task) -->")
            val response = rc.response().apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
            try {
                noAuthBodyHandler(task, rc.body().asJsonObject(), rc, inject, values)
                logger.info("noAuthExecute($task) <--")
            } catch (e: Exception) {
                logger.error("noAuthExecute(${e.message}) <--")
                response.end(getResponse(INTERNAL_SERVER_ERROR.code(), "Error occurred, try again").encodePrettily())
            }
        }

        /**
         * Body handler method that handles that do not need authentication i.e. login
         * @param task The action being undertaken
         * @param requestBody Request body from client
         * @param rc The routing context in this block
         * @param inject Code injected
         * @param values Params to check if fields are present
         * @author Jamie Omondi
         * @since 15/02/2023
         */
        private fun noAuthBodyHandler(
            task: String,
            requestBody: JsonObject,
            rc: RoutingContext,
            inject: (usr: JsonObject, body: JsonObject, response: HttpServerResponse) -> Unit,
            vararg values: String
        ) {
            logger.info("noAuthBodyHandler($task) -->")
            val response = rc.response().apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
            val bodySize = requestBody.encode().length
            if (bodySize > BODY_LIMIT) {
                response.end(
                    getResponse(
                        REQUEST_ENTITY_TOO_LARGE.code(),
                        "Request body too large:[$bodySize]KBs"
                    ).encodePrettily()
                )
                logger.info("noAuthBodyHandler($task) <--")

            } else {
                if (!hasFields(requestBody, *values)) {
                    response.end(getResponse(BAD_REQUEST.code(), "Some fields are missing").encodePrettily())
                    return
                }
                inject(JsonObject.of("email", requestBody.getString("email")), requestBody, response)
                logger.info("noAuthBodyHandler($task) <--")
            }
        }

        /**
         * Checks if the user has the specified role
         * @param body The role to be confirmed
         * @param values The fields to be checked in the body
         * @return State whether the body has those fields
         * @author Jamie Omondi
         * @since 15/02/2023
         */
        private fun hasFields(body: JsonObject, vararg values: String): Boolean {
            if (values.isEmpty())
                return true
            if (body.isEmpty)
                return false
            var result = true
            values.forEach {
                result = result && body.containsKey(it)
            }
            return result
        }

        /**
         * Checks if the user has the specified role
         * @param role The role to be confirmed
         * @param roles The roles of the user
         * @return State whether that user has those permissions
         * @author Jamie Omondi
         * @since 15/02/2023
         */
        fun hasPermissions(tokenRoles: JsonArray, userRoles: JsonArray): Boolean {
            var result = true
            tokenRoles.forEach {
                result = result && userRoles.contains(it)
            }
            return result
        }

        fun getResponse(code: Int, message: String, payload: JsonObject) =
            JsonObject.of("message", message, "payload", payload)

        fun getResponse(code: Int, message: String, payload: List<JsonObject>) =
            JsonObject.of("message", message, "payload", payload)

        fun getResponse(code: Int, message: String) = JsonObject.of("message", message)


        @JvmStatic
        fun getOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(interceptor())
                .callTimeout(10000, TimeUnit.MICROSECONDS)
                .readTimeout(15000, TimeUnit.MICROSECONDS)
                .writeTimeout(15000, TimeUnit.MICROSECONDS)
                .connectTimeout(20000, TimeUnit.MICROSECONDS)
                .pingInterval(5000, TimeUnit.MICROSECONDS)
                .build()
        }

        @JvmStatic
        fun interceptor(): HttpLoggingInterceptor {
            return HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
                level = HttpLoggingInterceptor.Level.BODY
                level = HttpLoggingInterceptor.Level.HEADERS
            }
        }
    }
}