package com.greenbay.api.services

import com.greenbay.api.utils.DatabaseUtils
import io.vertx.kotlin.coroutines.CoroutineVerticle

open class BaseService : CoroutineVerticle() {

    fun getDatabase() = DatabaseUtils(vertx)

}