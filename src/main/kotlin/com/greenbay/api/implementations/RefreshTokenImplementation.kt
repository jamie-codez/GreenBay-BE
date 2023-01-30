package com.greenbay.api.implementations

import com.greenbay.api.domain.RefreshToken
import com.greenbay.api.repos.RefreshTokenRepo
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

class RefreshTokenImplementation(vertx: Vertx) : RefreshTokenRepo {
    private val databaseUtils: DatabaseUtils = DatabaseUtils(vertx)
    override suspend fun save(refreshToken: RefreshToken) {
        databaseUtils.save(Collections.REFRESH_TOKEN_TBL.toString(), JsonObject.mapFrom(refreshToken), {

        }, {

        })
    }

    override suspend fun getRefreshToken(email: String, issuer: String) {
        databaseUtils.findOne(
            Collections.REFRESH_TOKEN_TBL.toString(),
            JsonObject.of("email", email, "issuer", issuer),
            JsonObject(),
            {

            },
            {

            })
    }

    override suspend fun updateRefreshToken(email: String, replacement: RefreshToken) {
        databaseUtils.findOneAndUpdate(Collections.REFRESH_TOKEN_TBL.toString(), JsonObject.of("email", email),
            JsonObject.mapFrom(replacement), {

            }, {

            }
        )
    }

    override suspend fun deleteRefreshToken(email: String) {
        databaseUtils.findOneAndDelete(Collections.REFRESH_TOKEN_TBL.toString(), JsonObject.of("email", email), {

        }, {

        })
    }
}