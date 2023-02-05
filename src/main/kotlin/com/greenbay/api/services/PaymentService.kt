package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils.Companion.APPLICATION_JSON
import com.greenbay.api.utils.BaseUtils.Companion.CONTENT_TYPE
import com.greenbay.api.utils.BaseUtils.Companion.execute
import com.greenbay.api.utils.BaseUtils.Companion.getResponse
import com.greenbay.api.utils.Collections
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class PaymentService:TenantService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    fun setPaymentsRoutes(router: Router){
        router.post("/api/v1/payments").handler(::createPayment)
        router.get("/api/v1/payments").handler(::getAllPayments)
        router.get("/api/v1/payments/:email").handler(::getMyPayments)
        router.put("/api/v1/payments/:email/:time").handler(::updatePayment)
        router.put("/api/v1/payments/:email/:_id").handler(::deletePayment)
        setTenantRoutes(router)
    }

    private fun createPayment(rc:RoutingContext){
        logger.info("createPayment() -->")
        execute("createPayment",rc,{usr, body, response ->
            getDatabase().save(Collections.PAYMENTS.toString(),body,{
                logger.info("[createPayment]")
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Payment created successfully").encodePrettily())
            },{
                logger.error("[createPayment]")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error creating payment try again").encodePrettily())
            })
        },"admin","super-admin","user")
    }
    private fun getAllPayments(rc:RoutingContext){
        logger.info("getAllPayment() -->")
        execute("getAllPayment",rc,{usr, body, response ->
            getDatabase().find(Collections.PAYMENTS.toString(), JsonObject(),{
                logger.info("[getAllPayment]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful",it).encodePrettily())
            },{
                logger.error("[getAllPayment]")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error getting payments try again").encodePrettily())
            })
        },"admin","super-admin")
    }

    private fun getMyPayments(rc:RoutingContext){
        logger.info("getMyPayments() -->")
        execute("getMyPayments",rc,{usr, body, response ->
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email",email)
            getDatabase().find(Collections.PAYMENTS.toString(), qry,{
                logger.info("[getMyPayments]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful",it).encodePrettily())
            },{
                logger.error("[getMyPayments]")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error getting payments try again").encodePrettily())
            })
        },"user")
    }
    private fun updatePayment(rc:RoutingContext){
        logger.info("updatePayment() -->")
        execute("updatePayment",rc,{usr, body, response ->
            val email = rc.request().getParam("email")
            val time = rc.request().getParam("time")
            val qry = JsonObject.of("email",email,"time",time)
            getDatabase().findOneAndUpdate(Collections.PAYMENTS.toString(), qry,body,{
                logger.info("[updatePayment]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful",it).encodePrettily())
            },{
                logger.error("[updatePayment]")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error updating payments try again").encodePrettily())
            })
        },"user")
    }
    private fun deletePayment(rc:RoutingContext){
        logger.info("deletePayment() -->")
        execute("deletePayment",rc,{usr, body, response ->
            val email = rc.request().getParam("email")
            val id = rc.request().getParam("_id")
            val qry = JsonObject.of("email",email,"_id",id)
            getDatabase().findOneAndDelete(Collections.PAYMENTS.toString(), qry,{
                logger.info("[deletePayment]")
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Successful",it).encodePrettily())
            },{
                logger.error("[deletePayment]")
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getResponse("Error deleting payments try again").encodePrettily())
            })
        },"admin","super-admin","user")
    }
}