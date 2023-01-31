package com.greenbay.api.exception

import kotlin.Exception

open class BaseException(message: String? = null, exception: Throwable?) : RuntimeException(message, exception?.cause)