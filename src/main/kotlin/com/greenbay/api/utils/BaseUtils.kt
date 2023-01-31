package com.greenbay.api.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.greenbay.api.exception.GreenBayException
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class BaseUtils {
    companion object {

        const val CONTENT_TYPE = "content-type"
        const val APPLICATION_JSON = "application/json"
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

        fun isExpired(jwt: String): Boolean {
            val decodedJWT = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))).build()
            val expiresAt = verifier.verify(decodedJWT).expiresAt as Long
            return System.currentTimeMillis() > expiresAt
        }

        fun verifyIsAdmin(jwt: String): Boolean {
            var isAdmin = false
            val decodedJWT = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))).build()
            val email = verifier.verify(decodedJWT).subject
            val qry = JsonObject.of("email", email)
            DatabaseUtils(Vertx.vertx()).findOne(Collections.ADMIN_TBL.toString(), qry, JsonObject(), {
                isAdmin = it.getJsonArray("roles").contains("ADMIN")
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

        fun checkHasRole(payload: JsonObject, role: String): Boolean =
            payload.getJsonArray("roles").contains(role)

        fun isValidJwt(jwt: String): Boolean {
            val decodedJwt = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))).build()
            return verifier.verify(decodedJwt).issuer == System.getenv("GB_JWT_ISSUER")
        }

        fun getReponse(message: String, payload: JsonObject) = JsonObject.of("message", message, "payload", payload)
        fun getReponse(message: String) = JsonObject.of("message", message)


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