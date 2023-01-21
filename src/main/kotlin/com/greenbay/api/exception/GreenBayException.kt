package com.greenbay.api.exception

class GreenBayException(
    message: String? = null, throwable: Exception? = null
) : BaseException(message, throwable)