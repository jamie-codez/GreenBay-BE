package com.greenbay.api.implementations

import com.greenbay.api.domain.AppUser
import com.greenbay.api.repos.AdminRepo
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

class AdminImplementation(vertx: Vertx) : AdminRepo {
    private val databaseUtils: DatabaseUtils = DatabaseUtils(vertx)
    override suspend fun addAdmin(appUser: AppUser) {
        databaseUtils.save(Collections.ADMIN_TBL.toString(), JsonObject.mapFrom(appUser), {

        }, {

        })
    }

    override suspend fun getAdmins(page: Int, pageSize: Int) {
        databaseUtils.find(Collections.ADMIN_TBL.toString(), JsonObject(), {

        }, {

        })
    }

    override suspend fun updateAdmin(email: String, appUser: AppUser) {
        databaseUtils.findOneAndUpdate(
            Collections.ADMIN_TBL.toString(),
            JsonObject.of("email", email),
            JsonObject.mapFrom(appUser),
            {

            },
            {

            })
    }

    override suspend fun deleteAdmin(email: String) {
        databaseUtils.findOneAndDelete(Collections.ADMIN_TBL.toString(), JsonObject.of("email", email), {

        }, {

        })
    }
}