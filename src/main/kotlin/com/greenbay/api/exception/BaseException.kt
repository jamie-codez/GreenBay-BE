package com.greenbay.api.exception

import kotlin.Exception

open class BaseException(message: String? = null, exception: Exception?) : Exception(message, exception.cause)