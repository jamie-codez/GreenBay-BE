package com.greenbay.api.utils

import com.greenbay.api.exception.GreenBayException
import io.vertx.core.json.JsonObject

class BaseUtils {
    companion object {
        @Throws(GreenBayException::class)
        fun isNotNull(jsonObject: JsonObject) {
            if (jsonObject.isEmpty || jsonObject == null) {
                throw GreenBayException("json is empty")
            }
        }

        @Throws(GreenBayException::class)
        fun isNotNull(string: String) {
            if (string.trim().isEmpty() || string.trim() == null) {
                throw GreenBayException("String is empty is empty")
            }
        }
    }
}