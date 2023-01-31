package com.greenbay.api.repos

import com.greenbay.api.domain.Role
import io.vertx.core.http.HttpServerResponse

interface RoleRepo {
    fun createRole(role: Role,response: HttpServerResponse)
    fun getRoles(page:Int,size:Int,query:String,response: HttpServerResponse)
    fun updateRole(replacement: Role,query: String,response: HttpServerResponse)
    fun deleteRole(roleName:String,response: HttpServerResponse)
}