package com.greenbay.api.repos

import com.greenbay.api.domain.House
import io.vertx.core.json.JsonObject

interface HouseRepo {
    suspend fun addHouse(house:House)
    suspend fun getAllHouses(page:Int,pageSize:Int)
    suspend fun updateHouse(houseNumber:String,replacement:JsonObject)
    suspend fun deleteHouse(houseNumber: String)
}