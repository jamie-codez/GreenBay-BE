package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils.Companion.APPLICATION_JSON
import com.greenbay.api.utils.BaseUtils.Companion.CONTENT_TYPE
import com.greenbay.api.utils.BaseUtils.Companion.execute
import com.greenbay.api.utils.BaseUtils.Companion.getResponse
import com.greenbay.api.utils.Collections
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

open class TenantService : HouseService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    fun setTenantRoutes(router: Router) {
        router.post("/api/v1/tenants").handler(::createTenant)
        router.get("/api/v1/tenants").handler(::getAllTenants)
        router.put("/api/v1/tenants/:email").handler(::updateTenant)
        router.delete("/api/v1/tenants/:email").handler(::deleteTenant)
        setHouseRoutes(router)
    }

    private fun createTenant(rc: RoutingContext) {
        logger.info("createTenant() -->")
        execute("createTenant", rc, { usr, body, response ->
            getDatabase().save(Collections.TENANTS.toString(), body, {
                logger.info("[createTenant]")
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Tenant created successfully").encodePrettily())
            }, {
                logger.error("[createTenant] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error creating tenant try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }

    private fun getAllTenants(rc: RoutingContext) {
        execute("getAllTenants", rc, { usr, body, response ->
            getDatabase().find(Collections.TENANTS.toString(), JsonObject(), {
                logger.info("[createTenant]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful", it).encodePrettily())
            }, {
                logger.error("[createTenant] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error creating tenant try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }


    @GreenBayTask("updateTenant")
    private fun updateTenant(rc: RoutingContext) {
        execute("updateTenant", rc, { usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email", email)
            val replacement = JsonObject.of("\$set", body)
            getDatabase().findOneAndUpdate(Collections.TENANTS.toString(), qry, replacement, {
                logger.info("[updateTenant]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful", it).encodePrettily())
            }, {
                logger.error("[updateTenant] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error updating tenant try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }

    private fun deleteTenant(rc: RoutingContext) {
        execute("deleteTenant", rc, { usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email", email)
            getDatabase().findOneAndDelete(Collections.TENANTS.toString(), qry, {
                logger.info("[deleteTenant]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful", it).encodePrettily())
            }, {
                logger.error("[deleteTenant] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error deleting tenant try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }

}