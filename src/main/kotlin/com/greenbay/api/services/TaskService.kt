package com.greenbay.api.services

import com.greenbay.api.exception.GreenBayException
import com.greenbay.api.utils.BaseUtils
import com.greenbay.api.utils.Collections
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

open class TaskService:CommunicationService() {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    fun setTaskRoutes(router: Router){
        router.post("/api/v1/task").handler(::createTask)
        router.get("/api/v1/task").handler(::getAllTasks)
        router.get("/api/v1/task/:email").handler(::getMyTasks)
        router.put("/api/v1/task").handler(::updateTask)
        router.delete("/api/v1/task").handler(::deleteTask)
        setCommunicationRoutes(router)
    }

    private fun createTask(rc: RoutingContext){
        logger.info("createTask() --> ")
        BaseUtils.execute("createTask", rc, { usr, body, response ->
            getDatabase().save(Collections.TASKS.toString(), body, {
                logger.error("[createTask]")
                response.apply {
                    statusCode = HttpResponseStatus.CREATED.code()
                    statusMessage = HttpResponseStatus.CREATED.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Task created").encodePrettily())
            }, {
                logger.error("[createTask] ${it.message}")
                response.apply {
                    statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                    statusMessage = HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }

    private fun getAllTasks(rc: RoutingContext){
        logger.info("getAllTasks() --> ")
        BaseUtils.execute("getAllTasks", rc, { usr, body, response ->
            logger.info("[getAllTasks]")
            getDatabase().find(Collections.TASKS.toString(), JsonObject(), {
                response.apply {
                    statusCode = HttpResponseStatus.OK.code()
                    statusMessage = HttpResponseStatus.OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Successful", it).encodePrettily())
            }, {
                logger.error("[getAllTasks] ${it.message}")
                response.apply {
                    statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                    statusMessage = HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }

    private fun getMyTasks(rc: RoutingContext){
        logger.info("getMyTasks() --> ")
        BaseUtils.execute("getMyTasks", rc, { usr, body, response ->
            logger.info("[getMyTasks]")
            val email = rc.request().getParam("email")
            val qry = JsonObject.of("email", email)
            getDatabase().find(Collections.TASKS.toString(), qry, {
                response.apply {
                    statusCode = HttpResponseStatus.OK.code()
                    statusMessage = HttpResponseStatus.OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Communication created", it).encodePrettily())
            }, {
                logger.error("[getMyTasks] ${it.message}")
                response.apply {
                    statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                    statusMessage = HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
                throw GreenBayException(it.message,it)
            })
        }, "user")
    }

    private fun updateTask(rc: RoutingContext){
        logger.info("updateTask() --> ")
        BaseUtils.execute("updateTask", rc, { usr, body, response ->
            logger.info("[updateTask]")
            val id = body.getString("_id")
            val qry = JsonObject.of("_id", id)
            val replacement = JsonObject.of("\$set", body)
            getDatabase().findOneAndUpdate(Collections.TASKS.toString(), qry, replacement, {
                response.apply {
                    statusCode = HttpResponseStatus.OK.code()
                    statusMessage = HttpResponseStatus.OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Successful", it).encodePrettily())
            }, {
                logger.error("[updateTask] ${it.message}")
                response.apply {
                    statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                    statusMessage = HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }
    private fun deleteTask(rc: RoutingContext){
        logger.info("deleteTask() --> ")
        BaseUtils.execute("deleteTask", rc, { usr, body, response ->
            logger.info("[deleteTask]")
            val id = body.getString("_id")
            val qry = JsonObject.of("_id", id)
            getDatabase().findOneAndDelete(Collections.TASKS.toString(), qry, {
                response.apply {
                    statusCode = HttpResponseStatus.OK.code()
                    statusMessage = HttpResponseStatus.OK.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Successful", it).encodePrettily())
            }, {
                logger.error("[deleteTask] ${it.message}")
                response.apply {
                    statusCode = HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                    statusMessage = HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(BaseUtils.CONTENT_TYPE, BaseUtils.APPLICATION_JSON)
                    .end(BaseUtils.getResponse("Error occurred try again").encodePrettily())
            })
        }, "admin", "super-admin")
    }
}