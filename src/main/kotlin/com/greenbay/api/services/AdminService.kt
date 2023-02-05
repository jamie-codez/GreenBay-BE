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
        router.post("/api/v1/admin/").handler(::registerAdmin)
        router.get("/api/v1/admin").handler(::getAllAdmin)
        router.put("/api/v1/admin/").handler(::updateAdmin)
        router.delete("/api/v1/admin").handler(::deleteAdmin)
    }

    private fun registerAdmin(rc: RoutingContext) {
        logger.info("registerAdmin() -->")
        execute("registerAdmin", rc, { usr, body, response ->
            getDatabase().save(Collections.ADMIN_TBL.toString(), body, {
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
            getDatabase().find(Collections.ADMIN_TBL.toString(), JsonObject(), {
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

    private fun updateAdmin(rc: RoutingContext){
        logger.info("updateAdmin() -->")
        execute("updateAdmin",rc,{usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email",email)
            val replacement = JsonObject.of("\$set",body)

            getDatabase().findOneAndUpdate(Collections.ADMIN_TBL.toString(),qry,replacement,{
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Updated successfully",it).encodePrettily())
            },{
                logger.error("[updateAdmin] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        },"admin","super-admin")
    }

    private fun deleteAdmin(rc:RoutingContext){
        logger.info("deleteAdmin() -->")
        execute("deleteAdmin",rc,{usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email",email)
            getDatabase().findOneAndDelete(Collections.ADMIN_TBL.toString(),qry,{
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Deleted successfully").encodePrettily())
            },{
                logger.error("[deleteAdmin] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        },"super-admin")
    }

}