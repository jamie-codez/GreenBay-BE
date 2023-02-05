package com.greenbay.api.services

import com.greenbay.api.utils.DatabaseUtils
import com.greenbay.api.utils.EmailUtils
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

open class BaseService : CoroutineVerticle() {

    fun getDatabase() = DatabaseUtils(vertx)
    fun getEmailClient() = EmailUtils(vertx)
    fun getPasswordEncoder() = BCryptPasswordEncoder()

}