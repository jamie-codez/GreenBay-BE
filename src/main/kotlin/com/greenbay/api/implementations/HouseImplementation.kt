package com.greenbay.api.implementations

import com.greenbay.api.domain.House
import com.greenbay.api.repos.HouseRepo
import com.greenbay.api.utils.Collections
import com.greenbay.api.utils.DatabaseUtils
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

class HouseImplementation(vertx: Vertx) : HouseRepo {
    private val databaseUtils: DatabaseUtils = DatabaseUtils(vertx)

    override suspend fun addHouse(house: House) {
        databaseUtils.save(Collections.HOUSE_TBL.toString(), JsonObject.mapFrom(house), {

        }, {

        })
    }

    override suspend fun getAllHouses(page: Int, pageSize: Int) {
        databaseUtils.find(Collections.HOUSE_TBL.toString(), JsonObject(), {

        }, {

        })
    }

    override suspend fun updateHouse(houseNumber: String, replacement: JsonObject) {
        databaseUtils.findOneAndUpdate(
            Collections.HOUSE_TBL.toString(),
            JsonObject.of("houseNumber", houseNumber),
            replacement,
            {

            },
            {

            })
    }

    override suspend fun deleteHouse(houseNumber: String) {
        databaseUtils.findOneAndDelete(Collections.HOUSE_TBL.toString(), JsonObject.of("houseNumber", houseNumber), {

        }, {

        })
    }
}