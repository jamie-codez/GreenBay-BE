package com.greenbay.api.services

import com.greenbay.api.utils.BaseUtils
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.HashMap

open class AdminService : BaseService() {

    fun setAdminRoutes(router: Router) {
        router.post("/api/v1/admin/register").handler(this::registerAdmin)
        router.get("/api/v1/admin").handler(this::getAllAdmin)
    }

    private fun registerAdmin(rc: RoutingContext) {
    }

    private fun getAllAdmin(rc: RoutingContext) {

    }

    private fun addNewAdminUser(body: JsonObject, jwtFields: HashMap<String, Any>, response: HttpServerResponse) {

    }

}