package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.OK
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
            BaseUtils.adminValidation(jwt,rc.response())
        }
    }


}