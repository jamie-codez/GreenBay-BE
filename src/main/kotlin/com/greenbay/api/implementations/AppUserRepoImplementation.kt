package com.greenbay.api.implementations

import com.greenbay.api.domain.AppUser
import com.greenbay.api.repos.UserRepo
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

class AppUserRepoImplementation(vertx: Vertx) :UserRepo {
    private val databaseUtils:DatabaseUtils = DatabaseUtils(vertx)
    override suspend fun saveUser(appUser: AppUser) {
        databaseUtils.save(Collections.USER_TBL.toString(), JsonObject.mapFrom(appUser),{

        },{

        })
    }

    override suspend fun getAppUsers(pageNumber: Int, pageSize: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAppUser(email: String, appUser: AppUser) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAppUser(appUser: AppUser) {
        TODO("Not yet implemented")
    }
}