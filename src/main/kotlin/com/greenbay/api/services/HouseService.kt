package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class HouseService : AdminService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    fun setHouseRoutes(router: Router) {
        router.post("/api/v1/house").handler(this::createHouse)
        router.get("/api/v1/house").handler(this::getAllHouses)
        router.put("/api/v1/house/:houseId").handler(this::updateHouse)
        router.delete("/api/v1/house/:houseId").handler(this::deleteHouse)
    }

    private fun createHouse(rc: RoutingContext) {
        rc.request().bodyHandler {
            val body = it.toJsonObject()
            val jwt = rc.request().getHeader("Authorization").split(" ")[1]
            if (!BaseUtils.isNotNull(body)) rc.response().apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader("content-type", "application/json")
                .end(
                    JsonObject.of("code", 543, "message", "Body is empty", "payload", null)
                        .encodePrettily()
                )
            BaseUtils.adminValidation(jwt, rc.response())
            val qry = JsonObject.of("houseNo", body.getString("houseNo"))
            DatabaseUtils(vertx).findOne(Collections.HOUSE_TBL.toString(), qry, JsonObject(), { res ->
                if (res.isEmpty) {
                    DatabaseUtils(vertx).save(Collections.HOUSE_TBL.toString(), body, {
                        rc.response().apply {
                            statusCode = CREATED.code()
                            statusMessage = CREATED.reasonPhrase()
                        }.putHeader("content-type", "application/json")
                            .end(
                                JsonObject.of(
                                    "code", CREATED.code(),
                                    "message", "House created successfully",
                                    "payload", "Created successfully"
                                ).encodePrettily()
                            )
                    }, {
                        rc.response().apply {
                            statusCode = INTERNAL_SERVER_ERROR.code()
                            statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                        }.putHeader("content-type", "application/json")
                            .end(
                                JsonObject.of(
                                    "code", CREATED.code(),
                                    "message", "Error creating house",
                                    "payload", "Error creating house"
                                ).encodePrettily()
                            )
                    })
                } else {
                    rc.response().apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader("content-type", "application/json")
                        .end(
                            JsonObject
                                .of(
                                    "status",
                                    CONFLICT,
                                    "message",
                                    "House already exist",
                                    "payload",
                                    null
                                )
                                .encodePrettily()
                        )
                }
            }, {
                rc.response().apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader("content-type", "application/json")
                    .end(
                        JsonObject.of(
                            "code", INTERNAL_SERVER_ERROR.code(),
                            "message", "Error occurred try again",
                            "payload", "Error occurred try again"
                        ).encodePrettily()
                    )
            })
        }
    }

    private fun getAllHouses(rc: RoutingContext) {
        val authKey = rc.request().getHeader("Authorization").split(" ")[1]
        if (BaseUtils.isValidJwt(authKey)) {
            BaseUtils.verifyIsUserOrAdmin(authKey)

        } else {
            rc.response().apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader("content-type", "application/json")
                .end(
                    JsonObject.of("code", 543, "message", "Error occurred try again", "payload", null).encodePrettily()
                )
        }
    }

    private fun updateHouse(rc: RoutingContext) {

    }

    private fun deleteHouse(rc: RoutingContext) {

    }


}