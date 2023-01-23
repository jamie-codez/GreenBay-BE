package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.HashMap

open class AdminService : BaseService() {

    fun setAdminRoutes(router: Router) {
        router.post("/api/v1/admin/register").handler(this::registerAdmin)
        router.get("/api/v1/admin").handler(this::getAllAdmin)
    }

    private fun registerAdmin(rc: RoutingContext) {
        rc.request().bodyHandler {
            val body = it.toJsonObject()
            val bearerToken = rc.request().getHeader("access_token")
            val jwt = bearerToken.split(" ")[1]
            val isJwt = BaseUtils.isNotNull(jwt)
            val isBody = BaseUtils.isNotNull(body)
            if (!isJwt) {
                rc.response().apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader("content-type", "application/json")
                    .end(
                        JsonObject
                            .of(
                                "code", 503,
                                "message", "Access token not provide",
                                "payload", null
                            )
                            .encodePrettily()
                    )
            }
            if (!isBody) {
                rc.response().apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader("content-type", "application/json")
                    .end(
                        JsonObject
                            .of(
                                "code", 503,
                                "message", "Provide all fields",
                                "payload", null
                            )
                            .encodePrettily()
                    )
            }
            val jwtFields = BaseUtils.validateAdminJWT(jwt)
            val email = JsonObject.of("email", jwtFields["subject"])
            DatabaseUtils(vertx).findOne(Collections.ADMIN_TBL.toString(), email, JsonObject(),
                { usr ->
                    if (usr.isEmpty) {
                        rc.response().apply {
                            statusCode = OK.code()
                            statusMessage = OK.reasonPhrase()
                        }.putHeader("content-type", "application/json")
                            .end(
                                JsonObject
                                    .of(
                                        "code", 404,
                                        "message", "Chaperone does not exist",
                                        "payload", null
                                    )
                                    .encodePrettily()
                            )
                    } else {
                        addNewAdminUser(body, jwtFields, rc.response())
                    }
                },
                { thr ->
                    rc.response().apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader("content-type", "application/json")
                        .end(
                            JsonObject
                                .of(
                                    "code", 404,
                                    "message", thr.message,
                                    "payload", null
                                )
                                .encodePrettily()
                        )
                    BaseUtils.throwError(thr.message!!)
                })
        }
    }

    private fun getAllAdmin(rc: RoutingContext) {
        val accessToken = rc.request().getHeader("access_token").split(" ")[1]
        val notNull = BaseUtils.isNotNull(accessToken)
        if (!notNull) {
            rc.response().apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader("content-type", "application/json")
                .end(
                    JsonObject.of("code", 543, "message", "Access token missing", "payload", null).encodePrettily()
                )
        }else{

        }
    }

    private fun addNewAdminUser(body: JsonObject, jwtFields: HashMap<String, Any>, response: HttpServerResponse) {
        val roles = jwtFields["roles"] as List<*>
        val qry = JsonObject.of("email", body.getString("email"))
        DatabaseUtils(vertx).findOne(Collections.ADMIN_TBL.toString(), qry, JsonObject(), {
            if (!it.isEmpty) {
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader("content-type", "application/json")
                    .end(
                        JsonObject
                            .of(
                                "code", 543,
                                "message", "User already exists",
                                "payload", null
                            )
                            .encodePrettily()
                    )
            }
            if (!roles.contains("super_admin")) {
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader("content-type", "application/json")
                    .end(
                        JsonObject
                            .of(
                                "code", 543,
                                "message", "Action not permitted with current privellages",
                                "payload", null
                            )
                            .encodePrettily()
                    )
            }
            val admin =
                body.put("createdBy", jwtFields["subject"]).put("roles", arrayOf("admin")).put("activated", false)
            DatabaseUtils(vertx).save(Collections.ADMIN_TBL.toString(), admin, {
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader("content-type", "application/json")
                    .end(
                        JsonObject
                            .of(
                                "code", CREATED.code(),
                                "message", "Admin created successfully",
                                "payload", null
                            )
                            .encodePrettily()
                    )
            }, { thr ->
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader("content-type", "application/json")
                    .end(
                        JsonObject
                            .of(
                                "code", 543,
                                "message", thr.message,
                                "payload", null
                            )
                            .encodePrettily()
                    )
            })

        }, {
            response.apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader("content-type", "application/json")
                .end(
                    JsonObject
                        .of(
                            "code", 543,
                            "message", it.message,
                            "payload", null
                        )
                        .encodePrettily()
                )
            BaseUtils.throwError(it.message!!)
        })
    }

}