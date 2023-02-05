package com.greenbay.api.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.greenbay.api.exception.GreenBayException
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.impl.logging.LoggerFactory
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
                    .end(getResponse("Invalid access token").encodePrettily())
            } else if (!checkHasRole(payload, *role)) {
                response.apply {
                    statusCode = FORBIDDEN.code()
                    statusMessage = FORBIDDEN.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Not enough permissions").encodePrettily())
            } else if (isExpired(jwt)) {
                response.apply {
                    statusCode = BAD_REQUEST.code()
                    statusMessage = BAD_REQUEST.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Access token expired").encodePrettily())
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
                    .end(this.getResponse(ex.message!!).encodePrettily())
            }
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
                    .end(getResponse("Request body too large: [${requestBody.encodePrettily().length}]").encodePrettily())
            } else {
                this.isPermitted(accessToken, *values, response = context.response()) { email, response ->
                    inject(JsonObject.of("email", email), requestBody, response)
                }
            }

        }

        fun getResponse(message: String, payload: JsonObject) = JsonObject.of("message", message, "payload", payload)
        fun getResponse(message: String, payload: List<JsonObject>) =
            JsonObject.of("message", message, "payload", payload)

        fun getResponse(message: String) = JsonObject.of("message", message)


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