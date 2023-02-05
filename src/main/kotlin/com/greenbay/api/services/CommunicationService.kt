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

open class CommunicationService:PaymentService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    fun setCommunicationRoutes(router: Router){
        router.post("/api/v1/communication").handler(::createCommunication)
        router.get("/api/v1/communication").handler(::getAllCommunications)
        router.get("/api/v1/communication/:email").handler(::getMyCommunications)
        router.put("/api/v1/communication").handler(::updateCommunication)
        router.delete("/api/v1/communication").handler(::deleteCommunication)
        setPaymentsRoutes(router)
    }

    private fun createCommunication(rc:RoutingContext){
        logger.info("createCommunication() --> ")
        execute("createCommunication",rc,{usr, body, response ->
            getDatabase().save(Collections.COMMUNICATIONS.toString(),body,{
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Communication created").encodePrettily())
            },{
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        },"admin","super-admin")
    }

    private fun getAllCommunications(rc:RoutingContext){
        logger.info("getAllCommunications() --> ")
        execute("getAllCommunications",rc,{usr, body, response ->
            logger.info("[getAllCommunications]")
            getDatabase().find(Collections.COMMUNICATIONS.toString(), JsonObject(),{
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Communication created",it).encodePrettily())
            },{
                logger.error("[getAllCommunications] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        },"admin","super-admin")
    }

    private fun getMyCommunications(rc:RoutingContext){
        logger.info("getMyCommunications() --> ")
        execute("getMyCommunications",rc,{usr, body, response ->
            logger.info("[getMyCommunications]")
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email",email)
            getDatabase().find(Collections.COMMUNICATIONS.toString(), qry,{
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Communication created",it).encodePrettily())
            },{
                logger.error("[getAllCommunications] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        },"user")
    }

    private fun updateCommunication(rc:RoutingContext){
        logger.info("updateCommunication() --> ")
        execute("updateCommunication",rc,{usr, body, response ->
            logger.info("[updateCommunication]")
            val id = body.getString("_id")
            val qry = JsonObject.of("_id",id)
            val replacement = JsonObject.of("\$set",body)
            getDatabase().findOneAndUpdate(Collections.COMMUNICATIONS.toString(), qry,replacement,{
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful",it).encodePrettily())
            },{
                logger.error("[updateCommunication] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        },"admin","super-admin","user")
    }
    private fun deleteCommunication(rc:RoutingContext){
        logger.info("deleteCommunication() --> ")
        execute("deleteCommunication",rc,{usr, body, response ->
            logger.info("[deleteCommunication]")
            val id = body.getString("_id")
            val qry = JsonObject.of("_id",id)
            getDatabase().findOneAndDelete(Collections.COMMUNICATIONS.toString(), qry,{
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful",it).encodePrettily())
            },{
                logger.error("[deleteCommunication] ${it.message}")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error occurred try again").encodePrettily())
            })
        },"admin","super-admin","user")
    }

}