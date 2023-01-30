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

        fun adminValidation(jwt: String, response: HttpServerResponse) {
            if (!isNotNull(jwt)) response.apply {
                statusCode = HttpResponseStatus.OK.code()
                statusMessage = HttpResponseStatus.OK.reasonPhrase()
            }.putHeader("content-type", "application/json")
                .end(
                    JsonObject.of("code", 543, "message", "Auth key empty", "payload", null)
                        .encodePrettily()
                )
            if (!isValidJwt(jwt)) response.apply {
                statusCode = HttpResponseStatus.OK.code()
                statusMessage = HttpResponseStatus.OK.reasonPhrase()
            }.putHeader("content-type", "application/json").end(
                JsonObject.of("code", 543, "message", "Invalid auth token", "payload", null).encodePrettily()
            )
            if (isExpired(jwt)) response.apply {
                statusCode = HttpResponseStatus.OK.code()
                statusMessage = HttpResponseStatus.OK.reasonPhrase()
            }.putHeader("content-type", "application/json").end(
                JsonObject.of("code", 543, "message", "Auth token expired", "payload", null).encodePrettily()
            )
            if (!verifyIsAdmin(jwt)) response.apply {
                statusCode = HttpResponseStatus.OK.code()
                statusMessage = HttpResponseStatus.OK.reasonPhrase()
            }.putHeader("content-type", "application/json").end(
                JsonObject.of("code", 543, "message", "Not authorized", "payload", null).encodePrettily()
            )
            return
        }

        fun userAndAdminValidation(jwt: String,response: HttpServerResponse){
            if (!isNotNull(jwt)) response.apply {
                statusCode = HttpResponseStatus.OK.code()
                statusMessage = HttpResponseStatus.OK.reasonPhrase()
            }.putHeader("content-type","application/json")
                .end(JsonObject.of("code",543,"message","Auth key empty","payload",null)
                    .encodePrettily())
            return
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

        fun getDataFromJwt(jwt: String): HashMap<String, Any> {
            val decodedJWt = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))).build()
            val claims = verifier.verify(decodedJWt).claims
            val expires = verifier.verify(decodedJWt).expiresAt
            val subject = verifier.verify(decodedJWt).subject
            val issuedAt = verifier.verify(decodedJWt).issuedAt
            val issuer = verifier.verify(decodedJWt).issuer
            val roles = claims["roles"] as List<*>
            val roleList = ArrayList<String>()
            roles.forEach {
                roleList.add(it.toString())
            }
            val map = HashMap<String, Any>()
            map["expires"] = expires
            map["subject"] = subject
            map["issuedAt"] = issuedAt
            map["roles"] = roleList
            map["issuer"] = issuer
            return map
        }

        fun isMine(jwt: String, collection: String, field: String): Boolean {
            var isMine = false
            val decodedJWT = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))).build()
            val email = verifier.verify(decodedJWT).subject
            val qry = JsonObject.of(field, email)
            DatabaseUtils(Vertx.vertx()).findOne(collection, qry, JsonObject(), {
                isMine = it.getString("email") == email
            }, {
                isMine = false
                throwError(it.message!!)
            })
            return isMine
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
        fun verifyIsUserOrAdmin(jwt: String): Boolean {
            var isUserOrAdmin = false
            val decodedJWT = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))).build()
            val email = verifier.verify(decodedJWT).subject
            val qry = JsonObject.of("email", email)
            DatabaseUtils(Vertx.vertx()).findOne(Collections.ADMIN_TBL.toString(), qry, JsonObject(), {
                isUserOrAdmin = it.getJsonArray("roles").contains("ADMIN") || it.getJsonArray("roles").contains("USER")
            }, {
                isUserOrAdmin = false
                throwError(it.message!!)
            })
            return isUserOrAdmin
        }

        fun isValidJwt(jwt: String): Boolean {
            val decodedJwt = JWT.decode(jwt)
            val verifier = JWT.require(Algorithm.HMAC256(System.getenv("GB_JWT_SECRET"))).build()
            return verifier.verify(decodedJwt).issuer == "greenbay.com"
        }

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