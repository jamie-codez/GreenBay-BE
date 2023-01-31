package com.greenbay.api.implementations

import com.greenbay.api.domain.Role
import com.greenbay.api.exception.GreenBayException
import com.greenbay.api.repos.RoleRepo
import com.greenbay.api.utils.BaseUtils.Companion.APPLICATION_JSON
import com.greenbay.api.utils.BaseUtils.Companion.CONTENT_TYPE
import com.greenbay.api.utils.BaseUtils.Companion.getReponse
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import kotlin.math.min

class RoleRepoImplementation(vertx: Vertx) : RoleRepo {
    private val databaseUtils: DatabaseUtils

    init {
        databaseUtils = DatabaseUtils(vertx)
    }

    override fun createRole(role: Role, response: HttpServerResponse) {
        databaseUtils.save(Collections.ROLE_TBL.toString(), JsonObject.mapFrom(role), {
            if (it.isNotBlank()) {
                response.apply {
                    statusCode = CREATED.code()
                    statusMessage = CREATED.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getReponse("Role created successfully").encodePrettily())
            } else {
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getReponse("Error creating role").encodePrettily())
                throw GreenBayException("Save result might be empty")
            }
        }, {
            response.apply {
                statusCode = INTERNAL_SERVER_ERROR.code()
                statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .end(getReponse("Error creating role").encodePrettily())
            throw GreenBayException(it.message, it)
        })
    }

    override fun getRoles(page: Int, size: Int, query: String, response: HttpServerResponse) {
        val queryObj = JsonObject.of("roleName", query)
        databaseUtils.find(Collections.ROLE_TBL.toString(), queryObj, {
            val from = page * size
            val to = from + size
            val payload: List<JsonObject> = it.subList(from, min(to, it.size))
            response.apply {
                statusCode = OK.code()
                statusMessage = OK.reasonPhrase()
            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .end(getReponse("success", payload).put("page", page).encodePrettily())
        }, {
            response.apply {
                statusCode = INTERNAL_SERVER_ERROR.code()
                statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .end(getReponse("Error getting roles").encodePrettily())
            throw GreenBayException(it.message, it)
        })
    }

    override fun updateRole(replacement: Role, query: String, response: HttpServerResponse) {
        databaseUtils.findOneAndUpdate(Collections.ROLE_TBL.toString(),
            JsonObject.of("roleName", query),
            JsonObject.mapFrom(replacement),
            {
                if (!it.isEmpty) {
                    response.apply {
                        statusCode = OK.code()
                        statusMessage = OK.reasonPhrase()
                    }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .end(getReponse("Role updated successfully").encodePrettily())
                }
            },
            {
                response.apply {
                    statusCode = INTERNAL_SERVER_ERROR.code()
                    statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getReponse("Error updating role").encodePrettily())
                throw GreenBayException(it.message, it)
            })
    }

    override fun deleteRole(roleName: String, response: HttpServerResponse) {
        databaseUtils.findOneAndDelete(Collections.ROLE_TBL.toString(), JsonObject.of("roleName", roleName), {
            if (!it.isEmpty) {
                response.apply {
                    statusCode = OK.code()
                    statusMessage = OK.reasonPhrase()
                }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .end(getReponse("Role deleted successfully").encodePrettily())
            }
        }, {
            response.apply {
                statusCode = INTERNAL_SERVER_ERROR.code()
                statusMessage = INTERNAL_SERVER_ERROR.reasonPhrase()
            }.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                .end(getReponse("Error deleting role").encodePrettily())
            throw GreenBayException(it.message, it)
        })
    }
}