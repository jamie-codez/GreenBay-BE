package com.greenbay.api.utils

import com.greenbay.api.exception.GreenBayException
import io.vertx.core.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class BaseUtils {
    companion object {
        @Throws(GreenBayException::class)
        fun isNotNull(jsonObject: JsonObject) {
            if (jsonObject.isEmpty) {
                throw GreenBayException("json is empty")
            }
        }

        @Throws(GreenBayException::class)
        fun isNotNull(string: String) {
            if (string.trim().isEmpty()) {
                throw GreenBayException("String is empty is empty")
            }
        }

        @JvmStatic
        fun getOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(interceptor())
                .callTimeout(10000, TimeUnit.MICROSECONDS)
                .readTimeout(15000, TimeUnit.MICROSECONDS)
                .writeTimeout(15000, TimeUnit.MICROSECONDS)
                .connectTimeout(20000, TimeUnit.MICROSECONDS)
                .pingInterval(5000, TimeUnit.MICROSECONDS)
                .build()
        }

        @JvmStatic
        fun interceptor(): HttpLoggingInterceptor {
            return HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
                level = HttpLoggingInterceptor.Level.BODY
                level = HttpLoggingInterceptor.Level.HEADERS
            }
        }
    }
}