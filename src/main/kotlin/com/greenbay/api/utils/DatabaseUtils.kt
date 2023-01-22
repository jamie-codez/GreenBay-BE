package com.greenbay.api.utils

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

class DatabaseUtils(vertx: Vertx) {
    private lateinit var dbClient: MongoClient

    init {
        val config = JsonObject()
            .put("connection_string", System.getenv("GB_DB_CON_STRING"))
            .put("db_name", System.getenv("GB_DB_NAME"))
        dbClient = MongoClient.createShared(vertx, config)
    }

    private fun getClient() = dbClient

    fun save(collection: String, document: JsonObject, success: () -> Unit, fail: (throwable: Throwable) -> Unit) {
        getClient().save(collection, document) {
            if (it.succeeded()) {
                success.invoke()
            } else {
                fail.invoke(it.cause())
            }
        }
    }

    fun findOne(
        collection: String,
        query: JsonObject,
        fields: JsonObject,
        success: (result: JsonObject) -> Unit,
        fail: (throwable: Throwable) -> Unit
    ) {
        getClient().findOne(collection, query, fields) {
            if (it.succeeded()) {
                success.invoke(it.result())
            } else {
                fail.invoke(it.cause())
            }
        }
    }

    fun find(
        collection: String,
        query: JsonObject,
        success: (result: List<JsonObject>) -> Unit,
        fail: (throwable: Throwable) -> Unit
    ) {
        getClient().find(collection, query) {
            if (it.succeeded()) {
                success.invoke(it.result())
            } else {
                fail.invoke(it.cause())
            }
        }
    }

    fun findOneAndUpdate(
        collection: String,
        query: JsonObject,
        replacement: JsonObject,
        success: (result: JsonObject) -> Unit,
        fail: (throwable: Throwable) -> Unit
    ) {
        getClient().findOneAndUpdate(collection, query, replacement) {
            if (it.succeeded()) {
                success.invoke(it.result())
            } else {
                fail.invoke(it.cause())
            }
        }
    }

    fun findOneAndDelete(
        collection: String,
        query: JsonObject,
        success: (result: JsonObject) -> Unit,
        fail: (throwable: Throwable) -> Unit
    ) {
        getClient().findOneAndDelete(collection, query) {
            if (it.succeeded()) {
                success.invoke(it.result())
            } else {
                fail.invoke(it.cause())
            }
        }
    }
}