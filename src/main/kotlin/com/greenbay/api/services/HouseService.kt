package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils
import com.greenbay.api.utils.BaseUtils.Companion.execute
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class HouseService : AppUserService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    fun setHouseRoutes(router: Router) {
        router.post("/api/v1/house").handler(this::createHouse)
        router.get("/api/v1/house").handler(this::getAllHouses)
        router.put("/api/v1/house/:houseId").handler(this::updateHouse)
        router.delete("/api/v1/house/:houseId").handler(this::deleteHouse)
    }

    private fun createHouse(rc: RoutingContext) {
        execute("addHouse", rc, { usr, body, response ->
            getDatabase().save(Collections.HOUSE_TBL.toString(), body, {
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("House created successfully").encodePrettily())
            }, {
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try agin").encodePrettily())
            })
        }, "admin", "admin")
    }

    private fun getAllHouses(rc: RoutingContext) {
        execute("getAllHouses", rc, { usr, body, response ->
            getDatabase().find(Collections.HOUSE_TBL.toString(), JsonObject(), {
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Successful", it).encodePrettily())
            }, {
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }

    private fun updateHouse(rc: RoutingContext) {
        execute("updateHouse", rc, { usr, body, response ->
            getDatabase().findOneAndUpdate(
                Collections.HOUSE_TBL.toString(),
                JsonObject.of("_id", body.getString("_id")),
                body,
                {
                    response.apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                        .end(BaseUtils.getResponse("Successful", it).encodePrettily())
                },
                {
                    response.apply {
                        statusCode = INTERNAL_SERVER_ERROR.code()
                        statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                    }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                        .end(BaseUtils.getResponse("Error occurred").encodePrettily())
                })
        }, "admin", "super-admin")
    }

    private fun deleteHouse(rc: RoutingContext) {
        execute("deleteHouse", rc, { usr, body, response ->
            getDatabase().findOneAndDelete(
                Collections.HOUSE_TBL.toString(),
                JsonObject.of("_id", body.getString("_id")),
                {
                    response.apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                        .end(BaseUtils.getResponse("Deleted successfully").encodePrettily())
                },
                {
                    response.apply {
                        statusCode = INTERNAL_SERVER_ERROR.code()
                        statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                    }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                        .end(BaseUtils.getResponse("Error occurred").encodePrettily())
                })
        }, "admin", "super-admin")
    }
}