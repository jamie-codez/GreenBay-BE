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
        databaseUtils.find(Collections.USER_TBL.toString(),JsonObject(),{

        },{

        })
    }

    override suspend fun updateAppUser(email: String, appUser: AppUser) {
        databaseUtils.findOneAndUpdate(Collections.USER_TBL.toString(), JsonObject.of("email",email), JsonObject.mapFrom(appUser),{

        },{

        })
    }

    override suspend fun deleteAppUser(appUser: AppUser) {
        databaseUtils.findOneAndDelete(Collections.USER_TBL.toString(),JsonObject.of("email",appUser.email),{

        },{

        })
    }
}