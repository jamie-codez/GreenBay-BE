package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils
import com.greenbay.api.utils.BaseUtils.Companion.APPLICATION_JSON
import com.greenbay.api.utils.BaseUtils.Companion.CONTENT_TYPE
import com.greenbay.api.utils.BaseUtils.Companion.execute
import com.greenbay.api.utils.BaseUtils.Companion.getResponse
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.HashMap

open class AdminService : BaseService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    fun setAdminRoutes(router: Router) {
        router.post("/api/v1/admin/register").handler(this::registerAdmin)
        router.get("/api/v1/admin").handler(this::getAllAdmin)
    }

    private fun registerAdmin(rc: RoutingContext) {
        logger.info("registerAdmin() -->")
        execute("registerAdmin", rc, { usr, body, response ->
            DatabaseUtils(vertx).save(Collections.ADMIN_TBL.toString(), body, {
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Admin created successfully").encodePrettily())
            }, {
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error creating admin try again").encodePrettily())
            })
        }, "super-admin")
    }

    private fun getAllAdmin(rc: RoutingContext) {
        logger.info("getAllAdmin() -->")
        execute("getAllAdmin", rc, { usr, body, response ->
            DatabaseUtils(vertx).find(Collections.ADMIN_TBL.toString(), JsonObject(), {
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Success", it).encodePrettily())
            }, {
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }


}