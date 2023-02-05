package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils.Companion.APPLICATION_JSON
import com.greenbay.api.utils.BaseUtils.Companion.CONTENT_TYPE
import com.greenbay.api.utils.BaseUtils.Companion.getResponse
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.Promise
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod.*
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler

class GreenBayService : AuthService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    val port = Integer.valueOf(System.getenv("GB_PORT"))
    override fun start(startFuture: Promise<Void>?) {
        super.start(startFuture)
        logger.info("start() -->")
        val router = Router.router(vertx)
        router.route().handler(CorsHandler.create().apply {
            allowedHeaders(setOf("Content-Type", "access-token", "refresh-token"))
            allowedMethods(setOf(POST, GET, PUT, DELETE))
        }).handler(BodyHandler.create())
        router.get("/api/v1/").handler(::ping)
        setAuthRoutes(router)
        vertx.createHttpServer().requestHandler(router)
            .listen(port){
                if (it.succeeded()){
                    logger.info("Server started successfully on port $port -->")
                    startFuture?.future()!!.succeeded()
                }else{
                    logger.error("Server failed to start -->")
                    startFuture?.future()!!.failed()
                }
            }
    }

    private fun ping(rc: RoutingContext) {
        rc.response().apply {
            statusCode = OK.code()
            statusMessage = OK.reasonPhrase()
        }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
            .end(getResponse("Server started successfully on port $port").encodePrettily())
    }
}